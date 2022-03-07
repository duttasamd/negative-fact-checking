/**
 * 
 */
package org.dice.nfactcheck.ml.feature.fact.impl;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NTotalOccurrenceFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        int numberOfOccurrences = 0;
        
        for ( ComplexProof complexProof : evidence.getComplexProofs())
            if ( complexProof.getNormalizedProofPhrase().equals(proof.getNormalizedProofPhrase()) ) numberOfOccurrences++;

        proof.getFeatures().setValue(NAbstractFactFeatures.TOTAL_OCCURRENCE, numberOfOccurrences);
    }
}
