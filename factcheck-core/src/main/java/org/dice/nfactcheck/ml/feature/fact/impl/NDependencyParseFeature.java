package org.dice.nfactcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.aksw.defacto.Constants;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;
import org.dice.nfactcheck.pojo.DependencyMatch;
import org.dice.nfactcheck.proof.extract.CorefResolution;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class NDependencyParseFeature {
    
	public void extractFeature(ComplexProof proof, Evidence evidence) {
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

		Annotation doc = evidence.getModel().corenlpClient.corefAnnotation(proof.getProofPhrase());
		String resolvedString = CorefResolution.applyCorefResolution(doc);
		doc = evidence.getModel().corenlpClient.corefAnnotation(resolvedString);

		boolean markToRemove = true;

        for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
            SemanticGraph graph = sentence.get(BasicDependenciesAnnotation.class);
			Set<IndexedWord> predicateMatchSet = new HashSet<IndexedWord>();

			for(IndexedWord word : graph.vertexSet()) {
                if(isMatch(word.value(), predicates)) {
                    predicateMatchSet.add(word);
                }
			}

            if(!predicateMatchSet.isEmpty()) {
				markToRemove = false;
			} else {
				continue;
			}

            if(!nproof.getIsSubjectWildcard()) {
				String subject = nproof.getModel().getSubjectLabel("en");
				List<Entry<String, Integer>> subjects = nproof.getIndexedSubjectList();

            	DependencyMatch closestMatch = null;
				int lowestLevelSubjectPredicateMatch = 100;

				for (IndexedWord predicate : predicateMatchSet) {
					DependencyMatch subjectPredicateDependency = checkPredicateDependency(graph, predicate, subjects);

					if(subjectPredicateDependency != null) {
						if(closestMatch == null) {
							lowestLevelSubjectPredicateMatch = subjectPredicateDependency.getLevel();
                            closestMatch = subjectPredicateDependency;
						} else {
							Levenshtein lev = new Levenshtein();
							if(lev.getSimilarity(subjectPredicateDependency.getMatch(), subject)
								> lev.getSimilarity(closestMatch.getMatch(), subject)) {
								lowestLevelSubjectPredicateMatch = subjectPredicateDependency.getLevel();
								closestMatch = subjectPredicateDependency;
							} else if(subjectPredicateDependency.getMatch().equals(closestMatch.getMatch())
								&& subjectPredicateDependency.getLevel() < lowestLevelSubjectPredicateMatch) {
								lowestLevelSubjectPredicateMatch = subjectPredicateDependency.getLevel();
								closestMatch = subjectPredicateDependency;
							}
						}
					}
				}

				if(closestMatch != null) {
					nproof.setSubjectPredicateMatch(true);
					nproof.setSubjectPredicateLevelDistance(lowestLevelSubjectPredicateMatch);
					nproof.setSubjectPredicateIndexDistance((int) Math.abs(closestMatch.getIndex() - closestMatch.getPredicate().pseudoPosition()));
					nproof.setMatchedSubject(closestMatch.getMatch());

					DependencyMatch predicateObjectMatch = checkPredicateDependency(graph, closestMatch.getPredicate(), nproof.getProbableWildcardWords());

					if(predicateObjectMatch != null) {
						nproof.setPredicateObjectMatch(true);
						nproof.setPredicateObjectLevelDistance(predicateObjectMatch.getLevel());
						nproof.setPredicateObjectIndexDistance((int) Math.abs(predicateObjectMatch.getIndex() - predicateObjectMatch.getPredicate().pseudoPosition()));
						nproof.setMatchedObject(predicateObjectMatch.getMatch());

						break;
					} else {
						markToRemove = true;
					}
				} else {
					markToRemove = true;
				}
            } else {
				String object = nproof.getModel().getObjectLabel("en");
				List<Entry<String, Integer>> objects = nproof.getIndexedObjectList();

                DependencyMatch closestMatch = null;

				int lowestLevelPredicateObjectMatch = 100;

				for (IndexedWord predicate : predicateMatchSet) {
					// System.out.println("Check Predicate dependency : " + predicate.word() + " - " + Integer.toString(predicate.index()));

					DependencyMatch objectPredicateDependency = checkPredicateDependency(graph, predicate, objects);

					if(objectPredicateDependency != null) {
                        if(objectPredicateDependency.getLevel() < lowestLevelPredicateObjectMatch) {
                            lowestLevelPredicateObjectMatch = objectPredicateDependency.getLevel();
                            closestMatch = objectPredicateDependency;
                        } else if(objectPredicateDependency.getLevel() == lowestLevelPredicateObjectMatch) {
							// System.out.println("Same level match");
							Levenshtein lev = new Levenshtein();

							if(lev.getSimilarity(objectPredicateDependency.getMatch(), object)
								> lev.getSimilarity(closestMatch.getMatch(), object)) {
								lowestLevelPredicateObjectMatch = objectPredicateDependency.getLevel();
								closestMatch = objectPredicateDependency;
							}
						}
					}
				}

				if(closestMatch != null) {
					nproof.setPredicateObjectMatch(true);
					nproof.setPredicateObjectLevelDistance(lowestLevelPredicateObjectMatch);
					nproof.setPredicateObjectIndexDistance((int) Math.abs(closestMatch.getIndex() - closestMatch.getPredicate().pseudoPosition()));
					nproof.setMatchedObject(closestMatch.getMatch());

					DependencyMatch subjectPredicateMatch = checkPredicateDependency(graph, closestMatch.getPredicate(), nproof.getProbableWildcardWords());

					if(subjectPredicateMatch != null) {
						nproof.setSubjectPredicateMatch(true);
						nproof.setSubjectPredicateLevelDistance(subjectPredicateMatch.getLevel());
						nproof.setSubjectPredicateIndexDistance((int) Math.abs(subjectPredicateMatch.getIndex() - subjectPredicateMatch.getPredicate().pseudoPosition()));
						nproof.setMatchedSubject(subjectPredicateMatch.getMatch());
					} else {
						markToRemove = true;
					}
				} else {
					markToRemove = true;
				}
			}
        }

		if(markToRemove) {
			nproof.setToRemove(true);
		} else {
			if(nproof.isSubjectPredicateMatch()) {
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_LEVEL_DISTANCE, (double) nproof.getSubjectPredicateLevelDistance());
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_INDEX_DISTANCE, (double) nproof.getSubjectPredicateIndexDistance());
			} else {
				// System.out.println("No subject match.");
				nproof.setToRemove(true);
			}

			if(nproof.isPredicateObjectMatch()) {
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT_LEVEL_DISTANCE, (double) nproof.getPredicateObjectLevelDistance());
				nproof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT_INDEX_DISTANCE, (double) nproof.getPredicateObjectIndexDistance());
			} else {
				// System.out.println("No object match.");
				nproof.setToRemove(true);
			}
		}

	}

	private boolean isMatch(String string1, List<String> strings) {
		boolean isMatch = false;

		if(Constants.STOP_WORDS.contains(string1)) {
			return false;
		}

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

	private DependencyMatch checkPredicateDependency(SemanticGraph graph, IndexedWord predicate, List<Entry<String, Integer>> words) {
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

		Set<IndexedWord> parentsSet = new HashSet<>();
		Set<IndexedWord> childrenSet = new HashSet<>();

		parentsSet.add(predicate);
		childrenSet.add(predicate);
		int currentLevel = 0;

		while(currentLevel < 10) {
			currentLevel++;
			if(!parentsSet.isEmpty()) {
				for (IndexedWord indexedWord : parentsSet) {
					if(skipTags.contains(indexedWord.tag())) {
						continue;
					}
					Entry<String, Integer> match = findIndexedMatch(indexedWord, words);
					if(match != null) {
						return new DependencyMatch(match.getKey(), match.getValue(), currentLevel, predicate);
					} 
				}

				Set<IndexedWord> children = new HashSet<>();
				for (IndexedWord indexedWord : parentsSet) {
					children.addAll(graph.getParents(indexedWord));
				}
				
				parentsSet.clear();
				parentsSet.addAll(children);
				children.clear();
			}

			if(!childrenSet.isEmpty()) {
				for (IndexedWord indexedWord : childrenSet) {
					if(skipTags.contains(indexedWord.tag())) {
						continue;
					}
					Entry<String, Integer> match = findIndexedMatch(indexedWord, words);
					if(match != null) {
						return new DependencyMatch(match.getKey(), match.getValue(), currentLevel, predicate);
					}
				}

				Set<IndexedWord> children = new HashSet<>();
				for (IndexedWord indexedWord : childrenSet) {
					children.addAll(graph.getChildren(indexedWord));
				}
				
				childrenSet.clear();
				childrenSet.addAll(children);
				children.clear();
			}
		}

		return null;
	}

	private Entry<String, Integer> findIndexedMatch(IndexedWord indexedWord, List<Entry<String, Integer>> words) {
		Entry<String, Integer> match = null;
		double lowestDelta = 100;

		for (Entry<String, Integer> word : words) {
			if(word.getKey().toLowerCase().contains(indexedWord.word().toLowerCase())) {
				if(indexedWord.pseudoPosition() >= word.getValue() 
					&& (indexedWord.pseudoPosition() - word.getValue()) < lowestDelta ) {
					lowestDelta = indexedWord.pseudoPosition() - word.getValue();
					match = word;
				}

				if(lowestDelta == 0) {
					break;
				}
			}
		}

		return match;
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
		//
	}


}
