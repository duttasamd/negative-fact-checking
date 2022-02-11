package org.dice.nfactcheck.ml.feature.fact.impl;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.impl.WordnetExpensionFeature;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;

public class NWordnetExpansionFeature extends WordnetExpensionFeature {
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        // if(proof.getClass() == NComplexProof.class) {
        //     NComplexProof nProof = (NComplexProof) proof;

        //     if(nProof.getProbableWildcardWords() == null 
        //         || nProof.getProbableWildcardWords().size() == 0)
        //         return;
        // }
        
        double similarity = 0;
        
        for ( Pattern pattern : evidence.getBoaPatterns() ) {
        	similarity = Math.max(similarity, wordnetExpansion.getExpandedJaccardSimilarity(proof.getProofPhrase(), pattern.getNormalized()));
        }
        
        if ( Double.isInfinite(similarity) || Double.isNaN(similarity) ) proof.getFeatures().setValue(NAbstractFactFeatures.WORDNET_EXPANSION, 0D);
        else proof.getFeatures().setValue(NAbstractFactFeatures.WORDNET_EXPANSION, similarity);
    }
}
