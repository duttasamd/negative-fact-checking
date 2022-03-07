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

import org.aksw.defacto.Constants;
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
import org.dice.nfactcheck.proof.extract.CorefResolution;

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

public class NDependencyParseFeatureOld extends DependencyParseFeature {
    // @Override
	// public void extractFeature(ComplexProof proof, Evidence evidence) {
	// 	// NComplexProof nproof = (NComplexProof) proof;

	// 	// List<String> skipRelationList = new ArrayList<>();
	// 	// skipRelationList.add("punct");
	// 	// skipRelationList.add("det");
	// 	// skipRelationList.add("case");
	// 	// skipRelationList.add("cc");

	// 	// List<Pattern> patterns = evidence.getBoaPatterns();
	// 	// // Store possible predicates in a list to match with predicates in the sentence
	// 	// List<String> predicates = new ArrayList<>();
	// 	// for ( Pattern p : patterns ) {
	// 	// 	predicates.add(p.getNormalized().trim());
	// 	// }

	// 	// Annotation doc = evidence.getModel().corenlpClient.corefAnnotation(proof.getProofPhrase());
	// 	// String resolvedString = applyCorefResolution(doc);
	// 	// doc = evidence.getModel().corenlpClient.corefAnnotation(resolvedString);

	// 	// boolean markToRemove = true;

	// 	// int count = 1;

	// 	// System.out.println("Probable Wildcards : ");
	// 	// for (String string : nproof.getProbableWildcardWords()) {
	// 	// 	System.out.println(string);
	// 	// }

	// 	// for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
	// 	// 	SemanticGraph graph = sentence.get(BasicDependenciesAnnotation.class);
	// 	// 	Set<IndexedWord> predicateMatchSet = new HashSet<IndexedWord>();

	// 	// 	for (SemanticGraphEdge edge : graph.edgeIterable()) {
	// 	// 		if(skipRelationList.contains(edge.getRelation().toString())){
	// 	// 			continue;
	// 	// 		}

	// 	// 		// predicate matches graph. Store as indexed word to use the index later to score relations
    //     //         if(isMatch(edge.getGovernor().value(), predicates)) {
    //     //             predicateMatchSet.add(edge.getGovernor());
    //     //         }
    //     //     }

	// 	// 	if(!predicateMatchSet.isEmpty()) {
	// 	// 		markToRemove = false;
	// 	// 	} else {
	// 	// 		continue;
	// 	// 	}

	// 	// 	for (IndexedWord predicate : predicateMatchSet) {
	// 	// 		// FOREACH PREDICATE
	// 	// 		// PREDICATE SUBJECT MATCH
	// 	// 		// PREDICATE OBJECT MATCH

	// 	// 		//FIND MATCHING SUBJECT
	// 	// 		if(nproof.getIsSubjectWildcard()) {

	// 	// 		} else {
	// 	// 			String subject = evidence.getModel().getSubjectLabel("en");
	// 	// 		}
	// 	// 	}
	// 	// }
	// }

	// private boolean isMatch(String string1, List<String> strings) {
	// 	boolean isMatch = false;

	// 	if(Constants.STOP_WORDS.contains(string1)) {
	// 		return false;
	// 	}

	// 	for (String string : strings) {
	// 		if(string1.contains(string) || string.contains(string1)) {
	// 			isMatch = true;
	// 			break;
	// 		}
	// 	}
	// 	return isMatch;
	// }

	// private String findMatch(String string1, List<String> strings) {
	// 	for (String string : strings) {
	// 		if(string1.toLowerCase().contains(string.toLowerCase())) {
	// 			return string;
	// 		} 
	// 	}

	// 	for (String string : strings) {
	// 		if(string.toLowerCase().contains(string1.toLowerCase())) {
	// 			return string;
	// 		} 
	// 	}
		
	// 	return null;
	// }

	// private Set<String> findMatches(String string1, List<String> strings) {
	// 	Set<String> matches = new HashSet<>();

	// 	for (String string : strings) {
	// 		if(string1.toLowerCase().contains(string.toLowerCase())
	// 			|| string.toLowerCase().contains(string1.toLowerCase())) {
	// 			matches.add(string);
	// 		}
	// 	}

	// 	return matches;
	// }

	
	// private String findMaxScoreMatch(Map<String, Double> scoreMap) {
	// 	String highestScoreMatch = null;
	// 	double highestScore = 0.0;

	// 	for (String match : scoreMap.keySet()) {
	// 		if(scoreMap.get(match) > highestScore) {
	// 			highestScore = scoreMap.get(match);
	// 			highestScoreMatch = match;
	// 		}
	// 	}

	// 	return highestScoreMatch;
	// } 


	// public void extractFeature2(ComplexProof proof, Evidence evidence) {
	// 	NComplexProof nproof = (NComplexProof) proof;

	// 	List<String> skipRelationList = new ArrayList<>();
	// 	skipRelationList.add("punct");
	// 	skipRelationList.add("det");
	// 	skipRelationList.add("case");
	// 	skipRelationList.add("cc");
		
	// 	List<Pattern> patterns = evidence.getBoaPatterns();
	// 	List<String> predicates = new ArrayList<>();
	// 	for ( Pattern p : patterns ) {
	// 		predicates.add(p.getNormalized().trim());
	// 	}

	// 	// System.out.println("Extract 2 Proof Phrase :  " + proof.getProofPhrase());
	// 	Annotation doc = evidence.getModel().corenlpClient.corefAnnotation(proof.getProofPhrase());
	// 	String resolvedString = CorefResolution.applyCorefResolution(doc);
	// 	doc = evidence.getModel().corenlpClient.corefAnnotation(resolvedString);

	// 	boolean markToRemove = true;

	// 	int count = 1;

	// 	for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
	// 		// System.out.println("Sentence : " + Integer.toString(count++));

	// 		SemanticGraph graph = sentence.get(BasicDependenciesAnnotation.class);

	// 		// System.out.println(graph.toString());
	// 		Set<IndexedWord> predicateMatchSet = new HashSet<IndexedWord>();

	// 		// System.out.println("Vertex set : ");
	// 		for(IndexedWord word : graph.vertexSet()) {
	// 			// System.out.println(word.value() + " " + Double.toString(word.pseudoPosition()));
    //             if(isMatch(word.value(), predicates)) {
    //                 predicateMatchSet.add(word);
    //             }
	// 		}

	// 		if(!predicateMatchSet.isEmpty()) {
	// 			// System.out.println("DO NOT REMOVE");
	// 			markToRemove = false;
	// 		} else {
	// 			continue;
	// 		}

	// 		// FOREACH PREDICATE
	// 		// PREDICATE SUBJECT MATCH
	// 		// PREDICATE OBJECT MATCH
	// 		if(!nproof.getIsSubjectWildcard()) {
	// 			System.out.println("Object Wildcard");
	// 			String subject = evidence.getModel().getSubjectLabel("en");

	// 			List<Entry<String, Integer>> subjects = SubjectObjectExtractor.extract(subject, nproof.getProofPhrase());

	// 			nproof.setMatchedSubject(subject);


	// 			IndexedWord closestMatchPredicate = null;
	// 			int lowestLevelSubjectPredicateMatch = 100;

	// 			for (IndexedWord predicate : predicateMatchSet) {
	// 				// System.out.println("Check Predicate dependency : " + predicate.word() + " - " + Integer.toString(predicate.index()));
	// 				Map<Integer, Set<Entry<String, Integer>>> subjectPredicateDependencyMap = checkPredicateDependency(graph, predicate, subjects);

	// 				// System.out.println(subject + " " + predicate.value() + 
	// 				// 		" dependency map size (1) : " + Integer.toString(subjectPredicateDependencyMap.size()));

	// 				if(!subjectPredicateDependencyMap.isEmpty()) {
	// 					List<Integer> levelList =  new ArrayList<>(subjectPredicateDependencyMap.keySet());
	// 					Collections.sort(levelList);
	// 					if(levelList.get(0) < lowestLevelSubjectPredicateMatch) {
	// 						// Entry<String,Integer> subject = subjectPredicateDependencyMap.get(levelList.get(0));
	// 						lowestLevelSubjectPredicateMatch = levelList.get(0);
	// 						closestMatchPredicate = predicate;
	// 					}
	// 				}
	// 			}

	// 			if(closestMatchPredicate != null) {
	// 				// System.out.println("Subject Predicate Dependency found. " + Integer.toString(lowestLevelSubjectPredicateMatch));
	// 				nproof.setSubjectPredicateMatch(true);
	// 				nproof.setSubjectPredicateLevelDistance(lowestLevelSubjectPredicateMatch);
	// 				nproof.setSubjectPredicateIndexDistance(closestMatchPredicate.index());
					
	// 			} else {
	// 				// System.out.println("Skip");
	// 				markToRemove = true;
	// 				continue;
	// 			}

	// 			Map<IndexedWord, Map<String, Entry<Integer, Integer>>> predicateObjectScoreMap
	// 				= new HashMap<>();

	// 			for (IndexedWord predicate : predicateMatchSet) {

	// 				Map<Integer, Set<Entry<String, Integer>>> predicateObjectDependencyMap 
	// 					= checkPredicateDependency(graph, predicate, nproof.getProbableWildcardWords());


	// 				if(!predicateObjectDependencyMap.isEmpty()) {
	// 					Map<String, Entry<Integer, Integer>> objectScoreMap = new HashMap<>();
	// 					List<Integer> levelList =  new ArrayList<>(predicateObjectDependencyMap.keySet());
	// 					Collections.sort(levelList);

	// 					for (Integer level : levelList) {
	// 						Set<Entry<String, Integer>> objectList = predicateObjectDependencyMap.get(level);

	// 						for (Entry<String, Integer> object : objectList) {
	// 							if(objectScoreMap.containsKey(object.getKey())) {
	// 								Entry<Integer, Integer> entry = objectScoreMap.get(object.getKey());
	// 								objectScoreMap.replace(object.getKey(), entry, Map.entry(entry.getKey(), entry.getValue() + 1));
	// 							} else {
	// 								objectScoreMap.put(object.getKey(), Map.entry(level, 1));
	// 							}
	// 						}
	// 					}
	// 					predicateObjectScoreMap.put(predicate, objectScoreMap);
	// 				}
	// 			}
			
	// 			// System.out.println("Set Object Scores : ");
	// 			setObjectScores(nproof, closestMatchPredicate, predicateObjectScoreMap);
	// 		} else {
	// 			System.out.println("Subject Wildcard");
	// 			String object = evidence.getModel().getObjectLabel("en");
	// 			List<Entry<String, Integer>> objects = SubjectObjectExtractor.extract(object, nproof.getProofPhrase());

	// 			nproof.setMatchedObject(object);
				

	// 			IndexedWord closestMatchPredicate = null;
	// 			int lowestLevelPredicateObjectMatch = 100;

	// 			for (IndexedWord predicate : predicateMatchSet) {
	// 				Map<Integer, Set<Entry<String, Integer>>> predicateObjectDependencyMap = checkPredicateDependency(graph, predicate, objects);

	// 				// System.out.println(object + " " + predicate.value() + 
	// 				// 	" dependency map size (1) : " + Integer.toString(predicateObjectDependencyMap.size()));

	// 				if(!predicateObjectDependencyMap.isEmpty()) {
	// 					List<Integer> levelList =  new ArrayList<>(predicateObjectDependencyMap.keySet());
	// 					Collections.sort(levelList);
	// 					if(levelList.get(0) < lowestLevelPredicateObjectMatch) {
	// 						lowestLevelPredicateObjectMatch = levelList.get(0);
	// 						closestMatchPredicate = predicate;
	// 					}
	// 				}
	// 			}

	// 			if(closestMatchPredicate != null) {
	// 				// System.out.println("Subject Predicate Dependency found. " + Integer.toString(lowestLevelSubjectPredicateMatch));
	// 				nproof.setSubjectPredicateMatch(true);
	// 				nproof.setSubjectPredicateLevelDistance(lowestLevelPredicateObjectMatch);
	// 				nproof.setSubjectPredicateIndexDistance(closestMatchPredicate.index());
					
	// 			} else {
	// 				// System.out.println("Skip");
	// 				markToRemove = true;
	// 				continue;
	// 			}

	// 			Map<IndexedWord, Map<String, Entry<Integer, Integer>>> subjectPredicateScoreMap
	// 				= new HashMap<>();

	// 			for (IndexedWord predicate : predicateMatchSet) {
	// 				// System.out.println("Predicate match : " + predicate.value()
	// 				// 	+ " - " + Integer.toString(predicate.index()));

	// 				Map<Integer, Set<String>> subjectPredicateDependencyMap 
	// 					= checkPredicateDependency(graph, predicate, nproof.getProbableWildcardWords());

	// 				// System.out.println("Predicate object dependency map size : " 
	// 				// 	+ Integer.toString(predicateObjectDependencyMap.size()));

	// 				// for (Integer level : predicateObjectDependencyMap.keySet()) {
	// 				// 	System.out.print("Level : " + Integer.toString(level) + " ");
	// 				// 	for (String object : predicateObjectDependencyMap.get(level)) {
	// 				// 		System.out.print(object + " ");
	// 				// 	}
	// 				// 	System.out.println();
	// 				// }


	// 				if(!subjectPredicateDependencyMap.isEmpty()) {
	// 					Map<String, Entry<Integer, Integer>> subjectScoreMap = new HashMap<>();
	// 					List<Integer> levelList =  new ArrayList<>(subjectPredicateDependencyMap.keySet());
	// 					Collections.sort(levelList);

	// 					for (Integer level : levelList) {
	// 						Set<String> subjectList = subjectPredicateDependencyMap.get(level);

	// 						for (String subject : subjectList) {
	// 							if(subjectScoreMap.containsKey(subject)) {
	// 								Entry<Integer, Integer> entry = subjectScoreMap.get(subject);
	// 								subjectScoreMap.replace(subject, entry, Map.entry(entry.getKey(), entry.getValue() + 1));
	// 							} else {
	// 								subjectScoreMap.put(subject, Map.entry(level, 1));
	// 							}
	// 						}
	// 					}
	// 					subjectPredicateScoreMap.put(predicate, subjectScoreMap);
	// 				}
	// 			}
			
	// 			// System.out.println("Set Object Scores : ");
	// 			setObjectScores(nproof, closestMatchPredicate, subjectPredicateScoreMap);
	// 		}

	// 		if(nproof.isSubjectObjectSamePredicateMatch()
	// 			&& nproof.getPredicateObjectLevelDistance() <= 5
	// 			&& nproof.getSubjectPredicateLevelDistance() <= 5) {
	// 			markToRemove = false;
	// 			break;
	// 		} else {
	// 			markToRemove = true;
	// 			nproof.setSubjectPredicateMatch(false);
	// 			nproof.setPredicateObjectMatch(false);
	// 			nproof.setSubjectObjectSamePredicateMatch(false);
	// 		}
	// 	}

	// 	// System.out.println("REMOVE");
	// 	if(markToRemove) {
	// 		nproof.setIsToRemove(true);
	// 	} else {
	// 		if(nproof.isSubjectPredicateMatch()) {
	// 			nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE, 1.0);
	// 			nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_LEVEL_DISTANCE, (double) nproof.getSubjectPredicateLevelDistance());
	// 		} else {
	// 			nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_LEVEL_DISTANCE, 100.0);
	// 		}

	// 		if(nproof.isPredicateObjectMatch()) {
	// 			nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT, 1.0);
	// 			nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT_LEVEL_DISTANCE, (double) nproof.getPredicateObjectLevelDistance());
	// 		} else {
	// 			nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT_LEVEL_DISTANCE, 100.0);
	// 		}

	// 		if(nproof.isSubjectObjectSamePredicateMatch()) {
	// 			nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_OBJECT, 1.0);
	// 		}
	// 	}

	// 	System.out.println("Object = " + nproof.getMatchedObject() + " " + Integer.toString(nproof.getPredicateObjectLevelDistance()));
	// 	System.out.println("Subject = " + nproof.getMatchedSubject() + " " + Integer.toString(nproof.getPredicateObjectLevelDistance()));

	// 	System.out.println(proof.getProofPhrase());
	// 	System.out.println("Best subject match : " + nproof.getMatchedSubject()
	// 		+ " [" + Integer.toString(nproof.getPredicateObjectLevelDistance()) + "] "
	// 		+ " Subject Predicate distance : " + Integer.toString(nproof.getSubjectPredicateLevelDistance())
	// 		+ " Both match : " + Boolean.toString(nproof.isSubjectObjectSamePredicateMatch())
	// 		+ " To REMOVE : " + Boolean.toString(nproof.getIsToRemove())
	// 	);
	// }

	// private Entry<String, Integer> checkPredicateDependency(SemanticGraph graph, IndexedWord predicate, List<Entry<String, Integer>> words) {
	// 	int currentLevel = 0;
	// 	Set<IndexedWord> parentsSet = new HashSet<>();
	// 	Set<IndexedWord> childrenSet = new HashSet<>();

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
	// 	skipTags.add("CC");

	// 	parentsSet.add(predicate);
	// 	childrenSet.add(predicate);
		
	// 	while(!parentsSet.isEmpty()) {
	// 		for (IndexedWord indexedWord : parentsSet) {
	// 			// System.out.println("P " + indexedWord.word() + " " + indexedWord.tag() + " - " + Double.toString(indexedWord.pseudoPosition()));
	// 			if(skipTags.contains(indexedWord.tag())) {
	// 				continue;
	// 			}
				
	// 			Entry<String, Integer> match = findIndexedMatch(indexedWord, words);
	// 			if(match != null) {
	// 				return match;
	// 			} 
	// 		}

	// 		currentLevel++;
	// 		if(currentLevel > 10) {
	// 			break;
	// 		}

	// 		Set<IndexedWord> children = new HashSet<>();
	// 		for (IndexedWord indexedWord : parentsSet) {
	// 			children.addAll(graph.getParents(indexedWord));
	// 		}
			
	// 		parentsSet.clear();
	// 		parentsSet.addAll(children);
	// 		children.clear();
	// 	}

	// 	while(!childrenSet.isEmpty()) {
	// 		for (IndexedWord indexedWord : childrenSet) {
	// 			// System.out.println("C " + indexedWord.word() + " " + indexedWord.tag() + " - " + Double.toString(indexedWord.pseudoPosition()));
	// 			if(skipTags.contains(indexedWord.tag())) {
	// 				// System.out.println("Skipping");
	// 				continue;
	// 			}
	// 			Entry<String, Integer> match = findIndexedMatch(indexedWord, words);
	// 			if(match != null) {
	// 				return match;
	// 			}
	// 		}

	// 		currentLevel++;

	// 		if(currentLevel > 10) {
	// 			break;
	// 		}

	// 		Set<IndexedWord> children = new HashSet<>();
	// 		for (IndexedWord indexedWord : childrenSet) {
	// 			children.addAll(graph.getChildren(indexedWord));
	// 		}
			
	// 		childrenSet.clear();
	// 		childrenSet.addAll(children);
	// 		children.clear();
	// 	}

	// 	return null;
	// }

	// private Entry<String, Integer> findIndexedMatch(IndexedWord indexedWord, List<Entry<String, Integer>> words) {
	// 	Entry<String, Integer> match = null;
	// 	// System.out.println("Index matching : " + indexedWord.word() + " " + Double.toString(indexedWord.pseudoPosition()));
	// 	double lowestDelta = 100;

	// 	for (Entry<String, Integer> word : words) {
	// 		// System.out.println("Matching with : " + word.getKey() + " " + Integer.toString(word.getValue()));
	// 		if(word.getKey().toLowerCase().contains(indexedWord.word().toLowerCase())) {
	// 			if(indexedWord.pseudoPosition() >= word.getValue() 
	// 				&& (indexedWord.pseudoPosition() - word.getValue()) < lowestDelta ) {
	// 				lowestDelta = indexedWord.pseudoPosition() - word.getValue();
	// 				match = word;
	// 			}

	// 			if(lowestDelta == 0) {
	// 				break;
	// 			}
	// 		}
	// 	}

	// 	return match;
	// }

	// private void setObjectScores(NComplexProof nproof, IndexedWord subjectMatchPredicate, 
	// 	Map<IndexedWord, Map<String, Entry<Integer, Integer>>> predicateObjectScoreMap) {
		
	// 	String bestMatchObject = null;
	// 	int bestMatchCount = 0;
	// 	int bestMatchLevel = 100;

	// 	Map<String, Entry<Integer, Integer>> bestMatchObjectScoreMap = null;
	// 	if(subjectMatchPredicate != null) {
	// 		// System.out.println("Subject Match Predicate");
	// 		bestMatchObjectScoreMap = predicateObjectScoreMap.get(subjectMatchPredicate);
	// 	} else {
	// 		// System.out.println("Subject Does NOT Match Predicate");
	// 	}

	// 	if(bestMatchObjectScoreMap == null || bestMatchObjectScoreMap.isEmpty()) {
	// 		// System.out.println("Best match map is null or empty.");
	// 		bestMatchObjectScoreMap = new HashMap<>();
	// 		for (IndexedWord predicate : predicateObjectScoreMap.keySet()) {
	// 			Map<String, Entry<Integer, Integer>> predicateMatchObjectScoreMap = predicateObjectScoreMap.get(predicate);
	// 			for (String object : predicateMatchObjectScoreMap.keySet()) {
	// 				if(bestMatchObjectScoreMap.containsKey(object)) {
	// 					Entry<Integer, Integer> entry = bestMatchObjectScoreMap.get(object);
	// 					if(entry.getValue() > predicateMatchObjectScoreMap.get(object).getValue()) {
	// 						bestMatchObjectScoreMap.replace(object, entry, predicateMatchObjectScoreMap.get(object));
	// 					} else if (entry.getValue() == predicateMatchObjectScoreMap.get(object).getValue()
	// 						&& entry.getKey() < predicateMatchObjectScoreMap.get(object).getKey())
	// 					{
	// 						bestMatchObjectScoreMap.replace(object, entry, predicateMatchObjectScoreMap.get(object));
	// 					}
	// 				} else {
	// 					bestMatchObjectScoreMap.put(object, predicateMatchObjectScoreMap.get(object));
	// 				}
	// 			}
	// 		}
	// 	} else {
	// 		nproof.setSubjectObjectSamePredicateMatch(true);
	// 	}
		

	// 	for (String object : bestMatchObjectScoreMap.keySet()) {
	// 		if(bestMatchObject == null 
	// 			|| bestMatchCount < bestMatchObjectScoreMap.get(object).getValue()) {
	// 			bestMatchCount = bestMatchObjectScoreMap.get(object).getValue();
	// 			bestMatchObject = object;
	// 			bestMatchLevel = bestMatchObjectScoreMap.get(object).getKey();
	// 		}
	// 	}

	// 	nproof.setMatchedObject(bestMatchObject);
	// 	nproof.setPredicateObjectLevelDistance(bestMatchLevel);
		
	// 	if(bestMatchObject != null) {
	// 		nproof.setPredicateObjectMatch(true);
	// 	}
	// }

	// public void test() {
	// 	// String str = "Built in 1917 by Dr. H. S. Garrett, in this house the 42nd President of the United States Bill Clinton spent the first four years of his life, having been born on August 19, 1946, at Julia Chester Hospital in Hope, Arkansas.";
	// 	String str = "Zak Bagans, host of the Ghost Adventures television show, owns a Haunted Museum in Las Vegas, Nevada where he displays artifacts relating to murderer Charles Manson, suicide doctor Jack Kevorkian and the death of Michael Jackson, among others.";
		
	// 	List<String> subjects = new ArrayList<>();
	// 	subjects.add("Michael Jackson");

	// 	List<String> objects = new ArrayList<>();
	// 	objects.add("Las Vegas, Navada");
	// 	objects.add("Las Vegas");

	// 	List<String> skipRelationList = new ArrayList<>();
	// 	skipRelationList.add("punct");
	// 	skipRelationList.add("det");
	// 	skipRelationList.add("case");
	// 	skipRelationList.add("cc");

	// 	List<String> predicates = new ArrayList<>();
	// 	predicates.add("born");
	// 	predicates.add("birth");

	// 	CoreNLPClient coreNLPClient = CoreNLPLocalClient.getCoreNLPClient();

	// 	Annotation doc = coreNLPClient.corefAnnotation(str);
	// 	String resolvedString = CorefResolution.applyCorefResolution(doc);
	// 	doc = coreNLPClient.corefAnnotation(resolvedString);
		


	// 	// for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
	// 	// 	SemanticGraph graph = sentence.get(BasicDependenciesAnnotation.class);
	// 	// 	// System.out.println(graph.toString());

	// 	// 	Set<IndexedWord> predicateMatchSet = new HashSet<IndexedWord>();

    //     //     for (SemanticGraphEdge edge : graph.edgeIterable()) {
	// 	// 		// System.out.println(edge.toString());
	// 	// 		if(skipRelationList.contains(edge.getRelation().toString())){
	// 	// 			continue;
	// 	// 		}

    //     //         if(isMatch(edge.getGovernor().value(), predicates)) {
    //     //             predicateMatchSet.add(edge.getGovernor());
    //     //         }
    //     //     }

	// 	// 	for (IndexedWord predicate : predicateMatchSet) {
	// 	// 		System.out.println("Predicate : " + predicate.word());
	// 	// 	}

	// 	// 	IndexedWord closestMatchPredicate = null;
	// 	// 	int lowestLevelSubjectPredicateMatch = 100;

	// 	// 	for (IndexedWord predicate : predicateMatchSet) {
	// 	// 		Map<Integer, Set<String>> subjectPredicateDependencyMap = checkPredicateDependency(graph, predicate, subjects);

	// 	// 		// System.out.println(subject + " " + predicate.value() + 
	// 	// 		// 		" dependency map size (1) : " + Integer.toString(subjectPredicateDependencyMap.size()));

	// 	// 		if(!subjectPredicateDependencyMap.isEmpty()) {
	// 	// 			List<Integer> levelList =  new ArrayList<>(subjectPredicateDependencyMap.keySet());
	// 	// 			Collections.sort(levelList);
	// 	// 			if(levelList.get(0) < lowestLevelSubjectPredicateMatch) {
	// 	// 				lowestLevelSubjectPredicateMatch = levelList.get(0);
	// 	// 				closestMatchPredicate = predicate;
	// 	// 			}
	// 	// 		}
	// 	// 	}

	// 	// 	if(closestMatchPredicate != null) {
	// 	// 		System.out.println("Matched subject-predicate : " + closestMatchPredicate.word());
	// 	// 	}
	// 	// }

	// }
}
