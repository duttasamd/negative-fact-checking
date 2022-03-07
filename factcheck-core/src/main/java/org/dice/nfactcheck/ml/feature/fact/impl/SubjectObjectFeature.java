package org.dice.nfactcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.nlp.ner.NamedEntityTagNormalizer;
import org.aksw.defacto.wordnet.WordNetExpansion;
import org.dice.factcheck.nlp.stanford.CoreNLPClient;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;
import org.dice.nfactcheck.patterns.ClosestPredicate;
import org.dice.nfactcheck.proof.extract.CorefResolution;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;

public class SubjectObjectFeature {

    private CoreNLPClient coreNLP = null;
    private CRFClassifier<CoreLabel> classifier = null;

    public void extract(NComplexProof proof) {
        if(this.coreNLP == null) {
            this.coreNLP = proof.getModel().corenlpClient;
        }

        // System.out.println("In so extractor");

        Annotation doc = proof.getModel().corenlpClient.corefAnnotation(proof.getProofPhrase());
		String corefResolvedString = CorefResolution.applyCorefResolution(doc);

        if(proof.getModel().wildcardNERTags != null) {
            this.extractWildcardsWithNER(proof, corefResolvedString);
        } else {
            this.extractWildcardsWithPatten(proof, corefResolvedString);
        }

        // System.out.println("wildcard extracted");

        if(proof.getIsToRemove()) {
            return;
        }

        extract(proof, corefResolvedString);
    }

    public void extractWildcardsWithNER(NComplexProof proof, String corefResolvedString) {
        Set<String> wildcardNERTags = proof.getModel().wildcardNERTags;

        if(wildcardNERTags == null) {
            return;
        }

        String igonoreWord = proof.getIsSubjectWildcard() ? proof.getModel().getObjectLabel("en") : proof.getModel().getSubjectLabel("en");

        classifier = proof.getModel().corenlpClient.getNERClassifier();

        List<Entry<String, Integer>> probableWildcards = new ArrayList<Entry<String, Integer>>();

        List<List<CoreLabel>> nerResolvedList = classifier.classify(corefResolvedString);
        proof.setNerResolvedProofPhrase(nerResolvedList);
        // System.out.println("NER Resolved");
        boolean isList = false;

        for ( List<CoreLabel> sentence : nerResolvedList) {
            boolean keepGoing = false;
            boolean commaEncountered = false;

            Queue<Entry<String, Integer>> interestElementsQueue = new LinkedList<Entry<String, Integer>>();
            int index = 1;

            for ( CoreLabel word : sentence ) {
                // System.out.println("Word : " + word.word() + " NER " + word.get(AnswerAnnotation.class));
                
                String normalizedTag = NamedEntityTagNormalizer.NAMED_ENTITY_TAG_MAPPINGS.get(word.get(AnswerAnnotation.class));

                if(wildcardNERTags.contains(normalizedTag) 
                    && !Constants.TEAM_SKIP_WORDS.contains(word.word())
                    && !igonoreWord.contains(word.word())) {
                    interestElementsQueue.add(Map.entry(word.word(), index));
                    keepGoing = true;
                } else if(keepGoing && word.word().equals(",")) {
                    interestElementsQueue.add(Map.entry(word.word(), index));
                    if(commaEncountered) {
                        isList = true;
                    }
                    commaEncountered = true;
                } else {
                    keepGoing = false;
                }

                if(!keepGoing && interestElementsQueue.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    int lastIndex = -1;
                    while(!interestElementsQueue.isEmpty()) {
                        Entry<String, Integer> element = interestElementsQueue.poll();

                        if(lastIndex < 0) {
                            lastIndex = element.getValue();
                        }

                        if(!element.getKey().equals(",")) {
                            sb.append(element.getKey() + " ");
                        } else if(!isList && interestElementsQueue.isEmpty()) {
                            sb.append("\b, ");
                        }
                    }

                    if(sb.length() > 0) {
                        // System.out.println("-> " + sb.toString().trim());
                        probableWildcards.add(Map.entry(sb.toString().trim(), lastIndex));
                    }

                    commaEncountered = false;
                    isList = false;
                }

                index++;
            }


            if(interestElementsQueue.size() > 0) {
                StringBuilder sb = new StringBuilder();
                int lastIndex = -1;
                while(!interestElementsQueue.isEmpty()) {
                    Entry<String, Integer> element = interestElementsQueue.poll();

                    if(lastIndex < 0) {
                        lastIndex = element.getValue();
                    }

                    if(!element.getKey().equals(",")) {
                        sb.append(element.getKey() + " ");
                    } else if(!isList && interestElementsQueue.isEmpty()) {
                        sb.append("\b, ");
                    }
                }

                if(sb.length() > 0) {
                    // System.out.println("-> " + sb.toString().trim());
                    probableWildcards.add(Map.entry(sb.toString().trim(), lastIndex));
                }

                commaEncountered = false;
                isList = false;
            }
        }

        if(probableWildcards.size() == 0) {
            proof.setIsToRemove(true);
        } else {
            proof.setProbableWildcardWords(probableWildcards);

            proof.getFeatures().setValue(NAbstractFactFeatures.NER_MATCH, 1.0);
            proof.getFeatures().setValue(NAbstractFactFeatures.NER_MATCH_COUNT, (double) probableWildcards.size());

            if(isList) {
                proof.getFeatures().setValue(NAbstractFactFeatures.NER_NOT_LIST, 0.0);
            } else {
                proof.getFeatures().setValue(NAbstractFactFeatures.NER_NOT_LIST, 1.0);
            }
        }
    }
    
    public void extractWildcardsWithPatten(NComplexProof proof, String corefResolvedString) {
        WordNetExpansion wordnet = new WordNetExpansion(Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "WORDNET_DICTIONARY"));

        String[] strTokens = corefResolvedString.split(" ");
        String patternString = proof.getModel().getObjectLabel("en");

        if(proof.getIsSubjectWildcard()) {
            patternString = proof.getModel().getSubjectLabel("en");
        }

        int patternSize = patternString.split(" ").length;

        if(strTokens.length <= patternSize) {
            proof.setIsToRemove(true);
        } else {
            double maxSimilarity = 0;
            String matchString = null;
            List<Entry<String,Integer>> probableWildcards = new ArrayList<>();

            Queue<String> candidateSubstringQueue = new LinkedList<String>();
            int currentIndex = -1;
            int bestMatchIndex = -1;

            do {
                StringBuilder candidateStringBuilder = new StringBuilder();
                candidateSubstringQueue.poll();

                while(candidateSubstringQueue.size() < patternSize) {
                    currentIndex++;
                    candidateSubstringQueue.add(strTokens[currentIndex]);
                }

                boolean skip = false;

                skip = Constants.STOP_WORDS.contains(strTokens[currentIndex].toLowerCase());

                if(!skip) {
                    skip = Constants.STOP_WORDS.contains(strTokens[currentIndex - patternSize + 1]);
                }

                if(!skip) {
                    for (String string : candidateSubstringQueue) {
                        candidateStringBuilder.append(string + " ");
                    }
                    
                    double similarity = wordnet.getExpandedJaccardSimilarity(candidateStringBuilder.toString().trim(), patternString);
                    double fractionalMatch = SubjectObjectFeature.getFractionalMatch(candidateStringBuilder.toString().trim(), patternString);

                    // System.out.println("Matching : " + candidateStringBuilder.toString().trim() + " vs " + patternString);
                    // System.out.println("Similarity : " + Double.toString(similarity));

                    if(fractionalMatch >= 0.5) {
                        if(similarity > maxSimilarity) {
                            matchString = candidateStringBuilder.toString().trim();
                            maxSimilarity = similarity;
                            bestMatchIndex = currentIndex;
                        }
                    }
                }
            } while (candidateSubstringQueue.size() <= patternSize && currentIndex < strTokens.length - 1);

            if(maxSimilarity > 0.6) {
                probableWildcards.add(Map.entry(matchString, bestMatchIndex));
                if(!proof.getIsSubjectWildcard()) {
                    proof.setProbableWildcardWords(probableWildcards);
                    proof.setMostProbableWildcard(matchString);
                    proof.setMatchedObject(matchString);

                    proof.getFeatures().setValue(NAbstractFactFeatures.NER_MATCH, 0.0);
                    proof.getFeatures().setValue(NAbstractFactFeatures.NER_MATCH_COUNT, (double) probableWildcards.size());

                    proof.getFeatures().setValue(NAbstractFactFeatures.NER_NOT_LIST, 1.0);
                    
                }
            } else {
                proof.setIsToRemove(true);
            }            
        }
    }

    private static double getFractionalMatch(String needle, String haystack) {
        String[] tokens = needle.split("[\\s*\\.,]");

        int count = 0;

        for (String token : tokens) {
            if(haystack.toLowerCase().contains(token.toLowerCase())) {
                count++;
            }
        }

        return (double) count / (double) tokens.length;
    }

    public void extract(NComplexProof proof, String haystack) {
        String needle = null;
        List<List<CoreLabel>> nerResolved = null;
        String subjectNer = null;

        if(proof.getIsSubjectWildcard()) {
            needle = proof.getModel().getObjectLabel("en");
        } else {
            needle = proof.getModel().getSubjectLabel("en");
            nerResolved = proof.getNerResolvedProofPhrase();
            subjectNer = ClosestPredicate.getSubjectNerTag(proof.getModel().getPropertyUri());
        }

        // System.out.println("Extracting : " + needle + "\nFrom :" + haystack);
        List<Entry<String, Integer>> matchList = new ArrayList<>();

        boolean keepGoing = false;
        StringBuilder sb = new StringBuilder();

		Annotation doc = coreNLP.corefAnnotation(haystack);

        int lastLevel = -1;

        if(subjectNer != null && nerResolved != null) {
            for (List<CoreLabel> nerResolvedSentence : nerResolved) {
                int index = 0;
                for (CoreLabel token : nerResolvedSentence) {        
                    index++;
                    String normalizedTag = NamedEntityTagNormalizer.NAMED_ENTITY_TAG_MAPPINGS.get(token.get(AnswerAnnotation.class));
                    // System.out.println(token.word() + " -> Normalized : " + normalizedTag + " == " + subjectNer);
                    // System.out.println("Token : " + token.word());
                    if(normalizedTag.equals(subjectNer)) {
                        // System.out.println("Tag match");
                        // keepGoing = true;
                        sb.append(token.word() + " ");
                        if(lastLevel < 0) {
                            lastLevel = index;
                            // System.out.println("Setting last level : " + Integer.toString(lastLevel));
                        }
                    } else {
                        // System.out.println("No ner match");
                        // keepGoing = false;
                        String matchedSubject = sb.toString().trim();
                        if(sb.length() > 0 && SubjectObjectFeature.getFractionalMatch(needle, matchedSubject.toLowerCase()) > 0) {
                            // System.out.println("Adding + " + matchedSubject + " " + Integer.toString(lastLevel));
                            matchList.add(Map.entry(sb.toString(), lastLevel));
                        }
                        
                        sb.setLength(0);
                        lastLevel = -1;
                    }
                }
            }
        } else {
            for (CoreLabel token : doc.get(TokensAnnotation.class)) {
                if(Constants.STOP_WORDS.contains(token.word())) {
                    continue;
                }
    
                if(keepGoing && !needle.contains(token.word())) {
                    keepGoing = false;
                    
                    if(sb.length() > 0) {
                        matchList.add(Map.entry(sb.toString(), lastLevel));
                        sb.setLength(0);
                    }

                    lastLevel = -1;
    
                    continue;
                }
    
                if(needle.contains(token.word())) {
                    // System.out.println("Matches index " + Integer.toString(token.index()));
                    keepGoing = true;
                    sb.append(token.word() + " ");
                    if(lastLevel < 0) {
                        lastLevel = token.index();
                    }
                }
            }    
        }
        
        if(sb.length() > 0) {
            matchList.add(Map.entry(sb.toString(), lastLevel));
        }
        
        if(matchList.size() == 0) {
            proof.setIsToRemove(true);
        } else {
            if(proof.getIsSubjectWildcard()) {
                proof.setIndexedObjectList(matchList);
            } else {
                // System.out.println("Setting subject list");
                proof.setIndexedSubjectList(matchList);
            }
        }
    }

    public static void main(String[] args) {
        // String haystack = "Bob Dylan (born Robert Zimmerman in Duluth), musician, singer-songwriter, Rock and Roll Hall of Famer, winner of 2016 Nobel Prize in Literature. Dylan was born in Utah.";
        // String needle = "Bob Dylan";

        // SubjectObjectExtractor exre

        // for (Entry<String, Integer> entry : extract(needle, haystack)) {
        //     System.out.println(entry.getKey() + " - " + Integer.toString(entry.getValue()));
        // }
    }
}
