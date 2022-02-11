package org.dice.nfactcheck.ml.feature.evidence.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.model.DefactoModel;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

public class NERWildcardMatchFeature extends AbstractEvidenceFeature {
    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        DefactoModel model = evidence.getModel();
        String[] objectLabels = model.getObjectLabel("en").split("[\\s*\\.,]");
        AbstractStringMetric sw_metric = new SmithWaterman();
        AbstractStringMetric lev_metric = new Levenshtein();

        List<Entry<String, Integer>> wilcardCountList =((NEvidence) evidence).getWildcardCountList();

        int limit = 5;

        if(wilcardCountList.size() < 5) {
            limit = wilcardCountList.size();
        }

        int score = 0;
        for(int i=0; i<limit; i++) {
            String nerTag = wilcardCountList.get(i).getKey();
            
            for (String objectPart : objectLabels) {
                if(!(objectPart.isEmpty() || objectPart.isBlank()) && nerTag.contains(objectPart)) {
                    score += (5 - i);
                    break;
                }
            }
        }

        evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_SCORE, score);
        if(score > 0) {
            evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH, 1);
        } else {
            evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH, 0);
        }

        boolean isPerfectMatch = false;

        for (ComplexProof proof: evidence.getComplexProofs()) {
            if(proof.getProofPhrase().contains(model.getObjectLabel("en"))) {
                isPerfectMatch = true;
                break;
            }
        }

        if(isPerfectMatch) {
            evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_PERFECT_MATCH, 1);
        } else {
            evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_PERFECT_MATCH, 0);
        }


        double bestSWSimilarity = 0.0;
        double totalNormalizedSWSimilarity = 0.0;

        double bestLevSimilarity = 0.0;
        double totalNormalizedLevSimilarity = 0.0;

        int count = 0;

        for (ComplexProof proof: evidence.getComplexProofs()) {
            NComplexProof nproof = (NComplexProof) proof;

            String matchedObject = nproof.getMatchedObject();
            String inputObject = evidence.getModel().getObjectLabel("en");

            if(matchedObject != null) {
                double swSimilarity = sw_metric.getSimilarity(matchedObject, inputObject);
                double levSimilarity = lev_metric.getSimilarity(matchedObject, inputObject);

                if(swSimilarity > bestSWSimilarity) {
                    bestSWSimilarity = swSimilarity;
                }

                totalNormalizedSWSimilarity += swSimilarity;

                if(levSimilarity > bestLevSimilarity) {
                    bestLevSimilarity = levSimilarity;
                }

                totalNormalizedLevSimilarity += levSimilarity;
                count++;
            }
            
        }
        
        totalNormalizedSWSimilarity /= count;
        totalNormalizedLevSimilarity /= count;
        
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_BEST_LEV_SCORE, bestLevSimilarity);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_NORMALIZED_LEV_SCORE, totalNormalizedLevSimilarity);

        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_BEST_SW_SCORE, bestSWSimilarity);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_NORMALIZED_SW_SCORE, totalNormalizedSWSimilarity);
    }
}
