package org.dice.nfactcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.dice.factcheck.ml.feature.fact.impl.DependencyParseFeature;
import org.dice.factcheck.nlp.stanford.CoreNLPClient;
import org.dice.factcheck.nlp.stanford.impl.CoreNLPLocalClient;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;
import org.dice.nfactcheck.patterns.ClosestPredicate;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class NDependencyParseFeature extends DependencyParseFeature {
    @Override
	public void extractFeature(ComplexProof proof, Evidence evidence) {

		NComplexProof nproof = (NComplexProof) proof;
		
		List<Pattern> patterns = evidence.getBoaPatterns();
		List<String> predicates = new ArrayList<>();
		float score = (float) 0.0;
		String patternString = "";
		List<TypedDependency> tdl = null;

		for ( Pattern p : patterns ) {
			predicates.add(p.getNormalized().trim());
		}

		System.out.println("Proof Phrase :  " + proof.getProofPhrase());
		Annotation doc = evidence.getModel().corenlpClient.corefAnnotation(proof.getProofPhrase());
		String resolvedString = applyCorefResolution(doc);
		doc = evidence.getModel().corenlpClient.corefAnnotation(resolvedString);
		// System.out.println("Resolved : " + resolvedString);
		// System.out.println();

		List<String> igonreTdList = new ArrayList<>();
		igonreTdList.add("case");
		igonreTdList.add("det");
		igonreTdList.add("compound");
		igonreTdList.add("cc");
		// igonreTdList.add("conj:and");
		// igonreTdList.add("conj:or");

		for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
			if(sentence.get(CoreAnnotations.TextAnnotation.class).toLowerCase().contains(patternString) 
				// && sentence.get(CoreAnnotations.TextAnnotation.class).split(" ").length<30
				)
			{
				List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
				Tree tree = parser.parse(tokens);
				TreebankLanguagePack tlp = new PennTreebankLanguagePack();
				GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
				GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
				tdl = gs.typedDependenciesEnhanced();


				List<String> subLabels = new ArrayList<String>();
				List<String> objLabels = new ArrayList<String>();

				for (String str : proof.getModel().getSubjectLabel("en").split(" ")) {
					subLabels.add(str.toLowerCase());
				}

				if(nproof.getProbableWildcardWords() != null) {
					objLabels.addAll(nproof.getProbableWildcardWords());
					for (String wildcardword : nproof.getProbableWildcardWords()) {
						System.out.println(wildcardword);
					}
				}

				List<IndexedWord> subjectMatches = new ArrayList<>();
				List<String> wildcardMatches = new ArrayList<>();

				Iterator<TypedDependency> it = tdl.iterator();

				Map<String, IndexedWord> dependencyIndices = new HashMap<String, IndexedWord>();
				Map<String, Double> probableWildcardScore = new HashMap<String, Double>();

				System.out.println(resolvedString);
				while(it.hasNext())
				{
					TypedDependency td = it.next();
					if(igonreTdList.contains(td.reln().toString())) {
						continue;
					}
					System.out.println(td.toString());

					if(isMatch(td.gov().value().toString().toLowerCase(), predicates)) {
						// System.out.println("Predicate match.");

						if(isMatch(td.dep().value().toString().toLowerCase(), subLabels)) {
							System.out.println("P Subject match.");
							// System.out.println(td.toString());
							subjectMatches.add(td.gov());
						} else {
							String match = findMatch(td.dep().value().toString().toLowerCase(), nproof.getProbableWildcardWords());
							if(match != null) {
								System.out.println("P S Object match. " + match);
								// System.out.println(td.toString());
								dependencyIndices.put(match, td.gov());
								probableWildcardScore.put(match, 0.0);
							}
						}
					} else if(isMatch(td.dep().value().toString().toLowerCase(), predicates)) {
						// System.out.println("Predicate match.");
						if(isMatch(td.gov().value().toString().toLowerCase(), subLabels)) {
							System.out.println("P Subject match.");
							// System.out.println(td.toString());
							subjectMatches.add(td.dep());
						} else {
							String match = findMatch(td.gov().value().toString().toLowerCase(), nproof.getProbableWildcardWords());
							if(match != null) {
								System.out.println("P S Object match. " + match);
								// System.out.println(td.toString());
								dependencyIndices.put(match, td.dep());
								probableWildcardScore.put(match, 0.0);
							}
						}
					} else if(isMatch(td.gov().value().toString().toLowerCase(), subLabels)) {
						System.out.println("Subject match.");
						// System.out.println(td.toString());
						String match = findMatch(td.dep().value().toString().toLowerCase(), nproof.getProbableWildcardWords());
						if(match != null) {
							System.out.println("Object match. " + match);
							// System.out.println(td.toString());
							dependencyIndices.put(match, td.dep());
							probableWildcardScore.put(match, 0.0);
							wildcardMatches.add(match);
						}
					} else if(isMatch(td.dep().value().toString().toLowerCase(), subLabels)) {
						System.out.println("Subject match.");
						// System.out.println(td.toString());
						String match = findMatch(td.gov().value().toString().toLowerCase(), nproof.getProbableWildcardWords());
						if(match != null) {
							System.out.println("Object match. " + match);
							// System.out.println(td.toString());
							dependencyIndices.put(match, td.dep());
							probableWildcardScore.put(match, 0.0);
							wildcardMatches.add(match);
						}
					}
				}

				if(dependencyIndices.size() > 0) {
					for (String probableWildcard : dependencyIndices.keySet()) {
						probableWildcardScore.replace(probableWildcard, 
							probableWildcardScore.get(probableWildcard) + 0.5);

						if(subjectMatches.contains(dependencyIndices.get(probableWildcard))) {
							probableWildcardScore.replace(probableWildcard, 
								probableWildcardScore.get(probableWildcard) + 0.5);
						}

						if(wildcardMatches.contains(probableWildcard)) {
							probableWildcardScore.replace(probableWildcard, 
								probableWildcardScore.get(probableWildcard) + 0.25);
						}
					}
					String maxScoreMatch = findMaxScoreMatch(probableWildcardScore);

					nproof.setMostProbableWildcard(maxScoreMatch);
					nproof.setMostProbableWildcardScore(probableWildcardScore.get(maxScoreMatch));
				} else {
					// System.out.println("Remove : [Dependency] " + nproof.getProofPhrase());
					nproof.setIsToRemove(true);
				}
			}
		}

	}

	private boolean isMatch(String string1, List<String> strings) {
		boolean isMatch = false;

		for (String string : strings) {
			if(string1.contains(string) || string.contains(string1)) {
				isMatch = true;
				break;
			}
		}
		return isMatch;
	}

	private String findMatch(String string1, List<String> strings) {
		for (String string : strings) {
			if(string1.toLowerCase().contains(string.toLowerCase())) {
				return string;
			} 
		}

		for (String string : strings) {
			if(string.toLowerCase().contains(string1.toLowerCase())) {
				return string;
			} 
		}
		
		return null;
	}

	private Set<String> findMatches(String string1, List<String> strings) {
		Set<String> matches = new HashSet<>();

		for (String string : strings) {
			if(string1.toLowerCase().contains(string.toLowerCase())
				|| string.toLowerCase().contains(string1.toLowerCase())) {
				matches.add(string);
			}
		}

		return matches;
	}

	private String applyCorefResolution(Annotation doc)
	{
		Map<Integer, CorefChain> corefs = doc.get(CorefChainAnnotation.class);
		List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

		List<String> resolved = new ArrayList<String>();

		for (CoreMap sentence : sentences) {
			//System.out.println(sentence);

			List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

			for (CoreLabel token : tokens) {

				Integer corefClustId= token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
				CorefChain chain = null;
				if(corefs!=null)
					chain = corefs.get(corefClustId);

				if(chain==null){
					resolved.add(token.word());
				}else{

					int sentINdx = chain.getRepresentativeMention().sentNum -1;
					CoreMap corefSentence = sentences.get(sentINdx);
					List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);
					String newwords = "";
					CorefMention reprMent = chain.getRepresentativeMention();
					if (token.index() <= reprMent.startIndex || token.index() >= reprMent.endIndex) {

						for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
							CoreLabel matchedLabel = corefSentenceTokens.get(i - 1); 
							resolved.add(matchedLabel.word().replace("'s", ""));
							newwords += matchedLabel.word() + " ";
						}
					}

					else {
						resolved.add(token.word());
					}
				}
			}
		}
		String resolvedStr ="";
		//System.out.println();
		for (String str : resolved) {
			resolvedStr+=str+" ";
			//System.out.println(str);
		}

		return resolvedStr;

	}

	private String findMaxScoreMatch(Map<String, Double> scoreMap) {
		String highestScoreMatch = null;
		double highestScore = 0.0;

		for (String match : scoreMap.keySet()) {
			if(scoreMap.get(match) > highestScore) {
				highestScore = scoreMap.get(match);
				highestScoreMatch = match;
			}
		}

		return highestScoreMatch;
	} 


	public void extractFeature2(ComplexProof proof, Evidence evidence) {
		NComplexProof nproof = (NComplexProof) proof;

		List<String> skipRelationList = new ArrayList<>();
		skipRelationList.add("punct");
		skipRelationList.add("det");
		skipRelationList.add("case");
		skipRelationList.add("cc");
		
		List<Pattern> patterns = evidence.getBoaPatterns();
		List<String> predicates = new ArrayList<>();
		for ( Pattern p : patterns ) {
			predicates.add(p.getNormalized().trim());
		}

		System.out.println("Proof Phrase :  " + proof.getProofPhrase());
		Annotation doc = evidence.getModel().corenlpClient.corefAnnotation(proof.getProofPhrase());
		String resolvedString = applyCorefResolution(doc);
		doc = evidence.getModel().corenlpClient.corefAnnotation(resolvedString);

		boolean markToRemove = true;

		int count = 1;
		// System.out.println("Proof : " + nproof.getProofPhrase());

		for (String string : nproof.getProbableWildcardWords()) {
			System.out.println(string);
		}

		for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
			// System.out.println("Sentence : " + Integer.toString(count++));

			SemanticGraph graph = sentence.get(BasicDependenciesAnnotation.class);

			// System.out.println(graph.toString());
			Set<IndexedWord> predicateMatchSet = new HashSet<IndexedWord>();

            for (SemanticGraphEdge edge : graph.edgeIterable()) {
				// System.out.println(edge.toString());
				if(skipRelationList.contains(edge.getRelation().toString())){ 
					// System.out.println("Skip");
					continue;
				}

                if(isMatch(edge.getGovernor().value(), predicates)) {
					// System.out.println("Predicate match : " + edge.getGovernor().value()
						// + " - " + Integer.toString(edge.getGovernor().index()));
                    predicateMatchSet.add(edge.getGovernor());
                }
            }

			if(!predicateMatchSet.isEmpty()) {
				// System.out.println("DO NOT REMOVE");
				markToRemove = false;
			} else {
				continue;
			}

			// FOREACH PREDICATE
			// PREDICATE SUBJECT MATCH
			// PREDICATE OBJECT MATCH
			if(!nproof.getIsSubjectWildcard()) {
				// System.out.println("Object Wildcard");
				String subject = evidence.getModel().getSubjectLabel("en");
				List<String> subjects = new ArrayList<>();
				subjects.add(subject);
				nproof.setMatchedSubject(subject);


				IndexedWord closestMatchPredicate = null;
				int lowestLevelSubjectPredicateMatch = 100;

				for (IndexedWord predicate : predicateMatchSet) {
					Map<Integer, Set<String>> subjectPredicateDependencyMap = checkPredicateDependency(graph, predicate, subjects);

					// System.out.println(subject + " " + predicate.value() + 
					// 		" dependency map size (1) : " + Integer.toString(subjectPredicateDependencyMap.size()));

					if(!subjectPredicateDependencyMap.isEmpty()) {
						List<Integer> levelList =  new ArrayList<>(subjectPredicateDependencyMap.keySet());
						Collections.sort(levelList);
						if(levelList.get(0) < lowestLevelSubjectPredicateMatch) {
							lowestLevelSubjectPredicateMatch = levelList.get(0);
							closestMatchPredicate = predicate;
						}
					}
				}

				if(closestMatchPredicate != null) {
					// System.out.println("Subject Predicate Dependency found. " + Integer.toString(lowestLevelSubjectPredicateMatch));
					nproof.setSubjectPredicateMatch(true);
					nproof.setSubjectPredicateLevelDistance(lowestLevelSubjectPredicateMatch);
					nproof.setSubjectPredicateIndexDistance(closestMatchPredicate.index());
					
				} else {
					// System.out.println("Skip");
					markToRemove = true;
					continue;
				}

				Map<IndexedWord, Map<String, Entry<Integer, Integer>>> predicateObjectScoreMap
					= new HashMap<>();

				for (IndexedWord predicate : predicateMatchSet) {
					// System.out.println("Predicate match : " + predicate.value()
					// 	+ " - " + Integer.toString(predicate.index()));

					Map<Integer, Set<String>> predicateObjectDependencyMap 
						= checkPredicateDependency(graph, predicate, nproof.getProbableWildcardWords());

					// System.out.println("Predicate object dependency map size : " 
					// 	+ Integer.toString(predicateObjectDependencyMap.size()));

					// for (Integer level : predicateObjectDependencyMap.keySet()) {
					// 	System.out.print("Level : " + Integer.toString(level) + " ");
					// 	for (String object : predicateObjectDependencyMap.get(level)) {
					// 		System.out.print(object + " ");
					// 	}
					// 	System.out.println();
					// }


					if(!predicateObjectDependencyMap.isEmpty()) {
						Map<String, Entry<Integer, Integer>> objectScoreMap = new HashMap<>();
						List<Integer> levelList =  new ArrayList<>(predicateObjectDependencyMap.keySet());
						Collections.sort(levelList);

						for (Integer level : levelList) {
							Set<String> objectList = predicateObjectDependencyMap.get(level);

							for (String object : objectList) {
								if(objectScoreMap.containsKey(object)) {
									Entry<Integer, Integer> entry = objectScoreMap.get(object);
									objectScoreMap.replace(object, entry, Map.entry(entry.getKey(), entry.getValue() + 1));
								} else {
									objectScoreMap.put(object, Map.entry(level, 1));
								}
							}
						}
						predicateObjectScoreMap.put(predicate, objectScoreMap);
					}
				}
			
				// System.out.println("Set Object Scores : ");
				setObjectScores(nproof, closestMatchPredicate, predicateObjectScoreMap);
			}

			if(nproof.isSubjectObjectSamePredicateMatch()
				&& nproof.getPredicateObjectLevelDistance() <= 5
				&& nproof.getSubjectPredicateLevelDistance() <= 5) {
				markToRemove = false;
				break;
			} else {
				markToRemove = true;
				nproof.setSubjectPredicateMatch(false);
				nproof.setPredicateObjectMatch(false);
				nproof.setSubjectObjectSamePredicateMatch(false);
			}
		}

		// System.out.println("REMOVE");
		if(markToRemove) {
			nproof.setIsToRemove(true);
		} else {
			if(nproof.isSubjectPredicateMatch()) {
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE, 1.0);
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_LEVEL_DISTANCE, (double) nproof.getSubjectPredicateLevelDistance());
			} else {
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_LEVEL_DISTANCE, 100.0);
			}

			if(nproof.isPredicateObjectMatch()) {
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT, 1.0);
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT_LEVEL_DISTANCE, (double) nproof.getPredicateObjectLevelDistance());
			} else {
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT_LEVEL_DISTANCE, 100.0);
			}

			if(nproof.isSubjectObjectSamePredicateMatch()) {
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_OBJECT, 1.0);
			}
		}

		// System.out.println("Object = " + nproof.getMatchedObject() + " " + Integer.toString(nproof.getPredicateObjectLevelDistance()));

		// System.out.println(proof.getProofPhrase());
		// System.out.println("Best object match : " + nproof.getMatchedObject()
		// 	+ " [" + Integer.toString(nproof.getPredicateObjectLevelDistance()) + "] "
		// 	+ " Subject Predicate distance : " + Integer.toString(nproof.getSubjectPredicateLevelDistance())
		// 	+ " Both match : " + Boolean.toString(nproof.isSubjectObjectSamePredicateMatch())
		// 	+ " To REMOVE : " + Boolean.toString(nproof.getIsToRemove())
		// );
	}


	// private Map<Integer, Set<String>> checkPredicateObjectDependency(SemanticGraph graph, IndexedWord predicate, List<String> objects) {		
	// 	// System.out.println("Checking : " + predicate.value() + " - " + Integer.toString(predicate.index()));
		
	// 	int currentLevel = 0;
	// 	Map<Integer, Set<String>> dependencyMap = new HashMap<>();
	// 	Set<IndexedWord> indexSet = new HashSet<>();
	// 	List<String> skipTags = new ArrayList<>();
	// 	skipTags.add("DT");
	// 	skipTags.add("LS");
	// 	skipTags.add("PRP");
	// 	skipTags.add("SYM");
	// 	skipTags.add("TO");
	// 	skipTags.add("WP");
	// 	skipTags.add("WP$");
	// 	skipTags.add("WRB");
	// 	skipTags.add("IN");
	// 	skipTags.add(",");
		
	// 	indexSet.add(predicate);

	// 	while(!indexSet.isEmpty()) {
	// 		for (IndexedWord indexedWord : indexSet) {
	// 			// System.out.println(indexedWord.word() + indexedWord.tag());
	// 			if(skipTags.contains(indexedWord.tag())) {
	// 				// System.out.println("Skipping");
	// 				continue;
	// 			}
	// 			Set<String> matches = findMatches(indexedWord.value(), objects);
	// 			if(!matches.isEmpty()) {
	// 				// System.out.println("Matches : " + Integer.toString(matches.size()));
	// 				if(!dependencyMap.containsKey(currentLevel)) {
	// 					dependencyMap.put(currentLevel, matches);
	// 				} else {
	// 					dependencyMap.get(currentLevel).addAll(matches);
	// 				}
	// 			}
	// 		}

	// 		// System.out.println("Add next level : " + Integer.toString(currentLevel + 1));

	// 		if(currentLevel > 5) {
	// 			break;
	// 		}

	// 		currentLevel++;

	// 		Set<IndexedWord> children = new HashSet<>();
	// 		for (IndexedWord indexedWord : indexSet) {
	// 			children.addAll(graph.getChildren(indexedWord));
	// 			children.addAll(graph.getParents(indexedWord));
	// 		}

	// 		indexSet.clear();
	// 		indexSet.addAll(children);
	// 		children.clear();
	// 	}

	// 	return dependencyMap;
	// }

	private Map<Integer, Set<String>> checkPredicateDependency(SemanticGraph graph, IndexedWord predicate, List<String> words) {
		// System.out.println("Check predicate subject dependency. " + predicate.value() + Integer.toString(predicate.index()));
		int currentLevel = 0;
		Map<Integer, Set<String>> dependencyMap = new HashMap<>();
		Set<IndexedWord> parentsSet = new HashSet<>();
		Set<IndexedWord> childrenSet = new HashSet<>();

		List<String> skipTags = new ArrayList<>();
		skipTags.add("DT");
		skipTags.add("LS");
		skipTags.add("PRP");
		skipTags.add("SYM");
		skipTags.add("TO");
		skipTags.add("WP");
		skipTags.add("WP$");
		skipTags.add("WRB");
		skipTags.add("IN");
		skipTags.add(",");
		skipTags.add("CC");

		parentsSet.add(predicate);
		childrenSet.add(predicate);
		
		while(!parentsSet.isEmpty()) {
			for (IndexedWord indexedWord : parentsSet) {
				// System.out.println(indexedWord.word() + indexedWord.tag());
				if(skipTags.contains(indexedWord.tag())) {
					// System.out.println("Skipping");
					continue;
				}
				Set<String> matches = findMatches(indexedWord.value(), words);
				if(!matches.isEmpty()) {
					// System.out.println("Matches : " + Integer.toString(matches.size()));
					if(!dependencyMap.containsKey(currentLevel)) {
						dependencyMap.put(currentLevel, matches);
					} else {
						dependencyMap.get(currentLevel).addAll(matches);
					}
				} else {
					// System.out.println("No match");
				}
			}

			currentLevel++;
			if(currentLevel > 10) {
				break;
			}

			Set<IndexedWord> children = new HashSet<>();
			for (IndexedWord indexedWord : parentsSet) {
				children.addAll(graph.getParents(indexedWord));
			}
			
			parentsSet.clear();
			parentsSet.addAll(children);
			children.clear();
		}

		while(!childrenSet.isEmpty()) {
			for (IndexedWord indexedWord : childrenSet) {
				// System.out.println(indexedWord.word() + indexedWord.tag());
				if(skipTags.contains(indexedWord.tag())) {
					// System.out.println("Skipping");
					continue;
				}
				Set<String> matches = findMatches(indexedWord.value(), words);
				if(!matches.isEmpty()) {
					// System.out.println("Matches : " + Integer.toString(matches.size()));
					if(!dependencyMap.containsKey(currentLevel)) {
						dependencyMap.put(currentLevel, matches);
					} else {
						dependencyMap.get(currentLevel).addAll(matches);
					}
				} else {
					// System.out.println("No match");
				}
			}

			currentLevel++;

			if(currentLevel > 10) {
				break;
			}

			Set<IndexedWord> children = new HashSet<>();
			for (IndexedWord indexedWord : childrenSet) {
				children.addAll(graph.getChildren(indexedWord));
			}
			
			childrenSet.clear();
			childrenSet.addAll(children);
			children.clear();
		}

		return dependencyMap;
	}

	private void setObjectScores(NComplexProof nproof, IndexedWord subjectMatchPredicate, 
		Map<IndexedWord, Map<String, Entry<Integer, Integer>>> predicateObjectScoreMap) {
		
		String bestMatchObject = null;
		int bestMatchCount = 0;
		int bestMatchLevel = 100;

		Map<String, Entry<Integer, Integer>> bestMatchObjectScoreMap = null;
		if(subjectMatchPredicate != null) {
			// System.out.println("Subject Match Predicate");
			bestMatchObjectScoreMap = predicateObjectScoreMap.get(subjectMatchPredicate);
		} else {
			// System.out.println("Subject Does NOT Match Predicate");
		}

		if(bestMatchObjectScoreMap == null || bestMatchObjectScoreMap.isEmpty()) {
			// System.out.println("Best match map is null or empty.");
			bestMatchObjectScoreMap = new HashMap<>();
			for (IndexedWord predicate : predicateObjectScoreMap.keySet()) {
				Map<String, Entry<Integer, Integer>> predicateMatchObjectScoreMap = predicateObjectScoreMap.get(predicate);
				for (String object : predicateMatchObjectScoreMap.keySet()) {
					if(bestMatchObjectScoreMap.containsKey(object)) {
						Entry<Integer, Integer> entry = bestMatchObjectScoreMap.get(object);
						if(entry.getValue() > predicateMatchObjectScoreMap.get(object).getValue()) {
							bestMatchObjectScoreMap.replace(object, entry, predicateMatchObjectScoreMap.get(object));
						} else if (entry.getValue() == predicateMatchObjectScoreMap.get(object).getValue()
							&& entry.getKey() < predicateMatchObjectScoreMap.get(object).getKey())
						{
							bestMatchObjectScoreMap.replace(object, entry, predicateMatchObjectScoreMap.get(object));
						}
					} else {
						bestMatchObjectScoreMap.put(object, predicateMatchObjectScoreMap.get(object));
					}
				}
			}
		} else {
			nproof.setSubjectObjectSamePredicateMatch(true);
		}
		

		for (String object : bestMatchObjectScoreMap.keySet()) {
			if(bestMatchObject == null 
				|| bestMatchCount < bestMatchObjectScoreMap.get(object).getValue()) {
				bestMatchCount = bestMatchObjectScoreMap.get(object).getValue();
				bestMatchObject = object;
				bestMatchLevel = bestMatchObjectScoreMap.get(object).getKey();
			}
		}

		nproof.setMatchedObject(bestMatchObject);
		nproof.setPredicateObjectLevelDistance(bestMatchLevel);
		
		if(bestMatchObject != null) {
			nproof.setPredicateObjectMatch(true);
		}
	}

	public void test() {
		// String str = "Built in 1917 by Dr. H. S. Garrett, in this house the 42nd President of the United States Bill Clinton spent the first four years of his life, having been born on August 19, 1946, at Julia Chester Hospital in Hope, Arkansas.";
		String str = "Zak Bagans, host of the Ghost Adventures television show, owns a Haunted Museum in Las Vegas, Nevada where he displays artifacts relating to murderer Charles Manson, suicide doctor Jack Kevorkian and the death of Michael Jackson, among others.";
		
		List<String> subjects = new ArrayList<>();
		subjects.add("Michael Jackson");

		List<String> objects = new ArrayList<>();
		objects.add("Las Vegas, Navada");
		objects.add("Las Vegas");

		List<String> skipRelationList = new ArrayList<>();
		skipRelationList.add("punct");
		skipRelationList.add("det");
		skipRelationList.add("case");
		skipRelationList.add("cc");

		List<String> predicates = new ArrayList<>();
		predicates.add("born");
		predicates.add("birth");

		CoreNLPClient coreNLPClient = CoreNLPLocalClient.getCoreNLPClient();

		Annotation doc = coreNLPClient.corefAnnotation(str);
		String resolvedString = applyCorefResolution(doc);
		doc = coreNLPClient.corefAnnotation(resolvedString);
		


		for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
			SemanticGraph graph = sentence.get(BasicDependenciesAnnotation.class);
			// System.out.println(graph.toString());

			Set<IndexedWord> predicateMatchSet = new HashSet<IndexedWord>();

            for (SemanticGraphEdge edge : graph.edgeIterable()) {
				// System.out.println(edge.toString());
				if(skipRelationList.contains(edge.getRelation().toString())){
					continue;
				}

                if(isMatch(edge.getGovernor().value(), predicates)) {
                    predicateMatchSet.add(edge.getGovernor());
                }
            }

			for (IndexedWord predicate : predicateMatchSet) {
				System.out.println("Predicate : " + predicate.word());
			}

			IndexedWord closestMatchPredicate = null;
			int lowestLevelSubjectPredicateMatch = 100;

			for (IndexedWord predicate : predicateMatchSet) {
				Map<Integer, Set<String>> subjectPredicateDependencyMap = checkPredicateDependency(graph, predicate, subjects);

				// System.out.println(subject + " " + predicate.value() + 
				// 		" dependency map size (1) : " + Integer.toString(subjectPredicateDependencyMap.size()));

				if(!subjectPredicateDependencyMap.isEmpty()) {
					List<Integer> levelList =  new ArrayList<>(subjectPredicateDependencyMap.keySet());
					Collections.sort(levelList);
					if(levelList.get(0) < lowestLevelSubjectPredicateMatch) {
						lowestLevelSubjectPredicateMatch = levelList.get(0);
						closestMatchPredicate = predicate;
					}
				}
			}

			if(closestMatchPredicate != null) {
				System.out.println("Matched subject-predicate : " + closestMatchPredicate.word());
			}
		}

	}


}
