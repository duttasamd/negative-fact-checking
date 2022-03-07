/**
 * 
 */
package org.dice.nfactcheck.ml.feature.evidence.impl;

import java.util.HashSet;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NProofFeature extends AbstractEvidenceFeature {
    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.Feature#extractFeature(org.aksw.defacto.evidence.Evidence)
     */
    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        // how many boa pattern did we find
        evidence.getFeatures().setValue(NAbstractEvidenceFeatures.NUMBER_OF_PROOFS, evidence.getComplexProofs().size());
        
        double scorePositives = 1D;
        
        // on how many sites did we find a boa pattern
        Set<String> proofWebsites = new HashSet<String>();
        for ( ComplexProof p : evidence.getComplexProofs() ) {
            if ( p.getScore() >= Defacto.DEFACTO_CONFIG.getDoubleSetting("evidence", "CONFIRMATION_THRESHOLD") ) {
                proofWebsites.add(p.getWebSite().getUrl());
                scorePositives *= ( 1D - p.getScore() ); 
            }
        }

        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.NUMBER_OF_ACCEPTABLE_PROOFS, proofWebsites.size());
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOTAL_ACCEPTABLE_PROOFS_SCORE, 1D - scorePositives);

        // System.out.println("Num proofs : " + Integer.toString(evidence.getComplexProofs().size()));
        // System.out.println("Num acceptable proofs : " + Integer.toString(proofWebsites.size()));
        // System.out.println("Acceptable proofs score : " + Double.toString(scorePositives));
    }
}
