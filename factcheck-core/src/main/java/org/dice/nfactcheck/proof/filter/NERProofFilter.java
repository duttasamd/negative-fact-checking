package org.dice.nfactcheck.proof.filter;

import java.io.IOException;
import java.util.*;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.nlp.ner.NamedEntityTagNormalizer;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;

public class NERProofFilter {
    private final String classifierPath	= "edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz";
    private CRFClassifier<CoreLabel> classifier;

    public NERProofFilter() {
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

    public void filterProofs(NEvidence evidence){
        String wildcardNERTag = evidence.getModel().wildcardNERTag;
        System.out.println(wildcardNERTag);

        Set<ComplexProof> proofs = evidence.getComplexProofs();
        Set<ComplexProof> toRemoveComplexProofs = new HashSet<ComplexProof>();

        for (ComplexProof proof : proofs) {
            NComplexProof nproof = (NComplexProof) proof;
            List<String> probableInterestWords = this.getProbableInterestWords(proof.getProofPhrase(), wildcardNERTag);
            
            if(probableInterestWords.contains(evidence.getModel().getSubjectLabel("en"))) {
                probableInterestWords.remove(evidence.getModel().getSubjectLabel("en"));
            }

            if(probableInterestWords.size() == 0) {
                toRemoveComplexProofs.add(proof);
                // System.out.println("No proof");
            } else {
                nproof.setProbableWildcardWords(probableInterestWords);
                System.out.println();

                for (String element : probableInterestWords) {
                    System.out.println("Element : " + element);
                }
            }
        }

        evidence.removeComplexProofs(toRemoveComplexProofs);
    }

    private List<String> getProbableInterestWords(String string, String wildcardNERTag) {
        List<String> probableInterestElements = new ArrayList<String>();

        for ( List<CoreLabel> sentence : ((List<List<CoreLabel>>) classifier.classify(string)) ) {
            boolean keepGoing = false;
            boolean commaEncountered = false;
            boolean isList = false;
            Queue<String> interestElementsQueue = new LinkedList<String>();

            for ( CoreLabel word : sentence ) {
                System.out.print(word.word() + " [" + word.get(AnswerAnnotation.class) + "] ");
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
            System.out.println();
        }
        
        return probableInterestElements;
    }

    public void test() {
        List<String> strList = new ArrayList<String>();
        strList.add("Jacco Eltingh (born 29 August 1970 in Heerde, Netherlands) and Paul Haarhuis (born 19 February 1966 in Eindhoven, Netherlands) first played on the main circuit from 1988 to 1999 (Eltingh) and from 1989 to 2003 (Haarhuis)");
        strList.add("Wesley Clark (born 1944), 2004 presidential contender and North Atlantic Treaty Organization (NATO) Commander; born in Chicago under the name Wesley Kanne but a graduate of Hall High School in Little Rock Bill Clinton, 42nd President of the United States and previously Governor of Arkansas, lived in the city.");
        strList.add("She was Ambassador of the United States to Barbados, Dominica, St Lucia from 1994 to 1998, and to Antigua, Grenada, St. Vincent, and St. Christopher-Nevis-Anguilla from 1995 to 1998, under Bill Clinton. Biography Jeanette W. Hyde was born in 1938.");
        strList.add("Likewise , one would say that Immanuel Kant was born in Königsberg in 1724 , not in Kaliningrad -LRB- Калининград -RRB- , as it has been called since 1946 .");
        strList.add("It is a tribute by Buckethead to Michael Jackson after hearing the news of his death.");
        strList.add("In 2009, Michael Jackson (1958–2009) died in a rented mansion in Holmby Hills.");

        String wildcardNERTag = NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PLACE;

        for (String string : strList) {   
            List<String> probableInterestElements = this.getProbableInterestWords(string, wildcardNERTag);
            System.out.println("===");
            System.out.println(string);
            System.out.println();
            for (String element : probableInterestElements) {
                System.out.println(element);
            }    
        }

        
    }
}
