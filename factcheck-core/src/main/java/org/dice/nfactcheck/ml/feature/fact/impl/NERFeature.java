package org.dice.nfactcheck.ml.feature.fact.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.aksw.defacto.nlp.ner.NamedEntityTagNormalizer;
import org.aksw.defacto.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.defacto.nlp.sbd.StanfordNLPSentenceBoundaryDisambiguation;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;
import org.dice.nfactcheck.ml.feature.fact.NFactFeatureExtraction;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;

public class NERFeature implements FactFeature {

    private final String classifierPath	= "edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz";
	private CRFClassifier<CoreLabel> classifier;

    public NERFeature() {
        try {
            this.classifier = CRFClassifier.getClassifier(classifierPath);
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        NComplexProof nproof = (NComplexProof) proof;
        String wildcardNERTag = evidence.getModel().wildcardNERTag;

        if(wildcardNERTag == null) {
            return;
        }

        List<String> probableInterestElements = new ArrayList<String>();

        boolean isList = false;

        for ( List<CoreLabel> sentence : ((List<List<CoreLabel>>) classifier.classify(proof.getProofPhrase())) ) {
            boolean keepGoing = false;
            boolean commaEncountered = false;
            
            Queue<String> interestElementsQueue = new LinkedList<String>();

            for ( CoreLabel word : sentence ) {
                // System.out.print(word.word() + " [" + word.get(AnswerAnnotation.class) + "] ");
                if(!keepGoing && interestElementsQueue.size() > 0) {
                    StringBuilder sb = new StringBuilder();

                    while(!interestElementsQueue.isEmpty()) {
                        String element = interestElementsQueue.poll();

                        if(element.equals(",") && !isList && !interestElementsQueue.isEmpty()) {
                            sb.append("\b, ");
                        } else if(element.equals(",") && isList) {
                            probableInterestElements.add(sb.toString().trim());
                            sb.setLength(0);
                        } else if(!(element.equals(",") && interestElementsQueue.isEmpty())) {
                            sb.append(element + " ");
                        }
                    }

                    if(sb.length() > 0) {
                        probableInterestElements.add(sb.toString().trim());
                    }

                    commaEncountered = false;
                    isList = false;
                }

                String normalizedTag = NamedEntityTagNormalizer.NAMED_ENTITY_TAG_MAPPINGS.get(word.get(AnswerAnnotation.class));
                
                if(wildcardNERTag.equals(normalizedTag)) {
                    interestElementsQueue.add(word.word());
                    keepGoing = true;
                } else if(keepGoing && word.word().equals(",")) {
                    interestElementsQueue.add(",");
                    if(commaEncountered) {
                        isList = true;
                    }

                    commaEncountered = true;
                } else {
                    keepGoing = false;
                }
            }
        
            if(interestElementsQueue.size() > 0) {
                StringBuilder sb = new StringBuilder();

                while(!interestElementsQueue.isEmpty()) {
                    String element = interestElementsQueue.poll();

                    if(element.equals(",") && !isList && !interestElementsQueue.isEmpty()) {
                        sb.append("\b, ");
                    } else if(element.equals(",") && isList) {
                        probableInterestElements.add(sb.toString().trim());
                        sb.setLength(0);
                    } else if(!(element.equals(",") && interestElementsQueue.isEmpty())) {
                        sb.append(element + " ");
                    }
                }

                if(sb.length() > 0) {
                    probableInterestElements.add(sb.toString().trim());
                }

                commaEncountered = false;
                isList = false;
            }
        }
        
        if(probableInterestElements.contains(evidence.getModel().getSubjectLabel("en"))) {
            probableInterestElements.removeAll(
                Arrays.asList(evidence.getModel().getSubjectLabel("en").split(" "))
            );
        }

        if(probableInterestElements.size() == 0) {
            // System.out.println("Remove : [Interest word] " + nproof.getProofPhrase());
            nproof.setIsToRemove(true);
        } else {
            nproof.setProbableWildcardWords(probableInterestElements);
            nproof.getFeatures().setValue(NAbstractFactFeatures.NER_MATCH, 1.0);
            nproof.getFeatures().setValue(NAbstractFactFeatures.NER_MATCH_COUNT, (double) probableInterestElements.size());
        }

        if(isList) {
            nproof.getFeatures().setValue(NAbstractFactFeatures.NER_NOT_LIST, 0.0);
        } else {
            nproof.getFeatures().setValue(NAbstractFactFeatures.NER_NOT_LIST, 1.0);
        }

    }

    private List<String> getProbableInterestWords(String string, String wildcardNERTag) {
        
        List<String> probableInterestElements = new ArrayList<String>();

        for ( List<CoreLabel> sentence : ((List<List<CoreLabel>>) classifier.classify(string)) ) {
            boolean keepGoing = false;
            boolean commaEncountered = false;
            boolean isList = false;
            Queue<String> interestElementsQueue = new LinkedList<String>();

            for ( CoreLabel word : sentence ) {
                // System.out.print(word.word() + " [" + word.get(AnswerAnnotation.class) + "] ");
                if(!keepGoing && interestElementsQueue.size() > 0) {
                    StringBuilder sb = new StringBuilder();

                    while(!interestElementsQueue.isEmpty()) {
                        String element = interestElementsQueue.poll();

                        if(element.equals(",") && !isList && !interestElementsQueue.isEmpty()) {
                            sb.append("\b, ");
                        } else if(element.equals(",") && isList) {
                            probableInterestElements.add(sb.toString().trim());
                            sb.setLength(0);
                        } else if(!(element.equals(",") && interestElementsQueue.isEmpty())) {
                            sb.append(element + " ");
                        }
                    }

                    if(sb.length() > 0) {
                        probableInterestElements.add(sb.toString().trim());
                    }

                    commaEncountered = false;
                    isList = false;
                }

                String normalizedTag = NamedEntityTagNormalizer.NAMED_ENTITY_TAG_MAPPINGS.get(word.get(AnswerAnnotation.class));
                
                if(wildcardNERTag.equals(normalizedTag)) {
                    interestElementsQueue.add(word.word());
                    keepGoing = true;
                } else if(keepGoing && word.word().equals(",")) {
                    interestElementsQueue.add(",");
                    if(commaEncountered) {
                        isList = true;
                    }

                    commaEncountered = true;
                } else {
                    keepGoing = false;
                }
            }
        
            if(interestElementsQueue.size() > 0) {
                StringBuilder sb = new StringBuilder();

                while(!interestElementsQueue.isEmpty()) {
                    String element = interestElementsQueue.poll();

                    if(element.equals(",") && !isList && !interestElementsQueue.isEmpty()) {
                        sb.append("\b, ");
                    } else if(element.equals(",") && isList) {
                        probableInterestElements.add(sb.toString().trim());
                        sb.setLength(0);
                    } else if(!(element.equals(",") && interestElementsQueue.isEmpty())) {
                        sb.append(element + " ");
                    }
                }

                if(sb.length() > 0) {
                    probableInterestElements.add(sb.toString().trim());
                }

                commaEncountered = false;
                isList = false;
            }
        }
        
        return probableInterestElements;
    }

}


