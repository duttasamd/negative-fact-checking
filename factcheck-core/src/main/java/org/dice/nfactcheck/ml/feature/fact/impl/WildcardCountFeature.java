package org.dice.nfactcheck.ml.feature.fact.impl;

import java.util.*;
import java.util.Map.Entry;

import org.aksw.defacto.evidence.ComplexProof;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;


public class WildcardCountFeature {
    public static void extractFeature(NEvidence evidence) {
        Set<ComplexProof> proofs = evidence.getComplexProofs();
        HashMap<String, Integer> nerCountMap = new HashMap<String, Integer>();

        for (ComplexProof proof : proofs) {
            NComplexProof nproof = (NComplexProof) proof;
            if(nproof.getProbableWildcardWords() != null && nproof.getProbableWildcardWords().size() > 0) {
                for (Entry<String,Integer> probableWildcard : nproof.getProbableWildcardWords()) {
                    if(nerCountMap.containsKey(probableWildcard.getKey())) {
                        nerCountMap.replace(probableWildcard.getKey(), nerCountMap.get(probableWildcard.getKey()) + 1);
                    } else {
                        nerCountMap.put(probableWildcard.getKey(), 1);
                    }
                }
            }
        }

        List<Entry<String,Integer>> entries 
            = new ArrayList<Entry<String,Integer>>(nerCountMap.entrySet());

        Collections.sort(entries, new Comparator<Entry<String,Integer>>() {

            public int compare(Entry<String,Integer> o1, Entry<String,Integer> o2) {
                // for descending order
                return o2.getValue() - o1.getValue();
            }
        });

        evidence.setWildcardCountList(entries);
        Levenshtein lev = new Levenshtein();

        for (ComplexProof proof : proofs) {
            NComplexProof nproof = (NComplexProof) proof;
            double wildcardScore = 0;
            int count = 0;

            int limit = 5;
            if(entries.size() < 5) {
                limit = entries.size();
            }

            for(int i=0; i<limit; i++) {
                String wildcardTag = entries.get(i).getKey();
                double fractionalMatch = 0.0;

                if(nproof.getIsSubjectWildcard()) {
                    fractionalMatch = getFractionalMatch(nproof.getMatchedSubject(), wildcardTag);
                    wildcardScore += lev.getSimilarity(nproof.getMatchedSubject(), wildcardTag) * entries.get(i).getValue();
                } else {
                    fractionalMatch = getFractionalMatch(nproof.getMatchedObject(), wildcardTag);
                    wildcardScore += lev.getSimilarity(nproof.getMatchedObject(), wildcardTag) * entries.get(i).getValue();
                }                

                if(fractionalMatch > 0.5) {
                    count++;
                }
            }

            wildcardScore /= limit;

            proof.getFeatures().setValue(NAbstractFactFeatures.WILDCARD_SCORE, wildcardScore);
            proof.getFeatures().setValue(NAbstractFactFeatures.WILDCARD_COUNT, count);
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
}
