package org.dice.nfactcheck.ml.feature.evidence.impl;

import java.util.List;
import java.util.Map.Entry;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.model.DefactoModel;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;
import org.dice.nfactcheck.patterns.ClosestPredicate;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

public class NERWildcardMatchFeature extends AbstractEvidenceFeature {
    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        DefactoModel model = evidence.getModel();
        AbstractStringMetric sw_metric = new SmithWaterman();
        AbstractStringMetric lev_metric = new Levenshtein();

        List<Entry<String, Integer>> wilcardCountList =((NEvidence) evidence).getWildcardCountList();

        int limit = 5;

        if(wilcardCountList.size() < 5) {
            limit = wilcardCountList.size();
        }

        boolean isSubjectWildcard = ClosestPredicate.getWildcardType(model.getPropertyUri()).equals("subject");

        String wildcardPositionOriginalLabel = null;
        if(isSubjectWildcard) {
            wildcardPositionOriginalLabel = model.getSubjectLabel("en");
        } else {
            wildcardPositionOriginalLabel = model.getObjectLabel("en");
        }
        int score = 0;
        for(int i=0; i<limit; i++) {
            String wildcardWord = wilcardCountList.get(i).getKey();
            double levSimilarity = lev_metric.getSimilarity(wildcardWord, wildcardPositionOriginalLabel);
            if(levSimilarity > 0.5) {
                score += levSimilarity * wilcardCountList.get(i).getValue();
            }
        }
        evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_SCORE, score);
        if(score > 0) {
            evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH, 1);
        } else {
            evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH, 0);
        }

        int perfectMatchCount = 0;

        for (ComplexProof proof: evidence.getComplexProofs()) {
            NComplexProof nproof = (NComplexProof) proof;

            if(nproof.getIsSubjectWildcard()) {
                if(proof.getProofPhrase().contains(model.getSubjectLabel("en"))) {
                    perfectMatchCount++;
                    break;
                }
            } else {
                if(proof.getProofPhrase().contains(model.getObjectLabel("en"))) {
                    perfectMatchCount++;
                    break;
                }
            }
        }

        if(perfectMatchCount > 0) {
            evidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_PERFECT_MATCH, perfectMatchCount);
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

            if(proof.getScore() < 0.5) {
                continue;
            }

            String matched = null;
            String input = null;

            if(isSubjectWildcard) {
                matched = nproof.getMatchedObject();
                input = evidence.getModel().getObjectLabel("en");
            } else {
                matched = nproof.getMatchedSubject();
                input = evidence.getModel().getSubjectLabel("en");
            }

            if(matched != null) {
                double swSimilarity = sw_metric.getSimilarity(matched, input);
                double levSimilarity = lev_metric.getSimilarity(matched, input);

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

        if(count > 0) {
            totalNormalizedSWSimilarity /= count;
            totalNormalizedLevSimilarity /= count;
        }

        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_BEST_LEV_SCORE, bestLevSimilarity);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_NORMALIZED_LEV_SCORE, totalNormalizedLevSimilarity);

        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_BEST_SW_SCORE, bestSWSimilarity);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.WILDCARD_MATCH_NORMALIZED_SW_SCORE, totalNormalizedSWSimilarity);
    }
}
