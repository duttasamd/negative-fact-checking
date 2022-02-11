package org.dice.nfactcheck.ml.feature.fact.impl;

import java.util.*;
import java.util.Map.Entry;

import org.aksw.defacto.evidence.ComplexProof;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;


public class WildcardCountFeature {
    public static void extractFeature(NEvidence evidence) {
        Set<ComplexProof> proofs = evidence.getComplexProofs();
        HashMap<String, Integer> nerCountMap = new HashMap<String, Integer>();

        for (ComplexProof proof : proofs) {
            NComplexProof nproof = (NComplexProof) proof;
            if(nproof.getProbableWildcardWords() != null && nproof.getProbableWildcardWords().size() > 0) {
                for (String probableWildcard : nproof.getProbableWildcardWords()) {
                    if(nerCountMap.containsKey(probableWildcard)) {
                        nerCountMap.replace(probableWildcard, nerCountMap.get(probableWildcard) + 1);
                    } else {
                        nerCountMap.put(probableWildcard, 1);
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

        int limit = 5;

        if(entries.size() < 5) {
            limit = entries.size();
        }

        // for(int i=0; i<entries.size(); i++) {
        //     String nerTag = entries.get(i).getKey();
        //     System.out.print(nerTag + Integer.toString(entries.get(i).getValue()) + "-" + Integer.toString(5 - i) + ", ");
        // }
        // System.out.println();

        for (ComplexProof proof : proofs) {
            int nerScore = 0;
            int count = 0;
            for(int i=0; i<limit; i++) {
                String nerTag = entries.get(i).getKey();
                // System.out.println(nerTag);
                double getFractionalMatch = getFractionalMatch(nerTag, proof.getProofPhrase());
                if(getFractionalMatch > 0.4) {
                    count++;
                    nerScore += (5 - i);
                }
            }

            // System.out.println("Wildcard score : " + Integer.toString(nerScore));
            // System.out.println("Wildcard count : " + Integer.toString(count));

            proof.getFeatures().setValue(NAbstractFactFeatures.WILDCARD_SCORE, nerScore);
            proof.getFeatures().setValue(NAbstractFactFeatures.WILDCARD_COUNT, count);

            // if(proof.getFeatures().stringValue(NAbstractFactFeatures.CLASS) == "acceptable") {
            //     System.out.println("[" + Integer.toString(nerScore) + "|" + Integer.toString(count) + "] " + proof.getProofPhrase());
            // }
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
