package org.dice.nfactcheck.ml.feature.fact;

import java.util.HashSet;
import java.util.Set;

import org.dice.nfactcheck.evidence.NComplexProof;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.fact.impl.*;

public class NFactFeatureExtraction {

    public static Set<FactFeature> factFeatures = new HashSet<FactFeature>();
    
    static {
        NFactFeatureExtraction.factFeatures.add(new NEndOfSentenceCharacterFeature());
        NFactFeatureExtraction.factFeatures.add(new NPageTitleFeature());
        NFactFeatureExtraction.factFeatures.add(new NSubjectObjectSimilarityFeature());
        NFactFeatureExtraction.factFeatures.add(new NTokenDistanceFeature());
        NFactFeatureExtraction.factFeatures.add(new NTotalOccurrenceFeature());
        NFactFeatureExtraction.factFeatures.add(new NPropertyFeature());
        NWordnetExpansionFeature.init();
        NFactFeatureExtraction.factFeatures.add(new NWordnetExpansionFeature());
        NFactFeatureExtraction.factFeatures.add(new NClassFeature());
    }
    
    /**
     * 
     * @param evidence
     */
    public void extractFeatureForFact(NEvidence evidence) {
        int count = 0;
        int totalProofs = evidence.getComplexProofs().size();
        // score the collected evidence with every feature extractor defined
        for (ComplexProof proof : evidence.getComplexProofs() ) {
            for ( FactFeature feature : NFactFeatureExtraction.factFeatures ) {
                feature.extractFeature(proof, evidence);
            }
            System.out.println("Proof : " + Integer.toString(++count) + "/" + Integer.toString(totalProofs));
        }

        WildcardCountFeature.extractFeature(evidence);

        for (ComplexProof proof : evidence.getComplexProofs() ) {
            NAbstractFactFeatures.nfactFeatures.add(proof.getFeatures());
        }
    }
}
