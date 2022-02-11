package org.dice.nfactcheck.ml.feature.evidence.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NTotalHitCountFeature extends AbstractEvidenceFeature {

    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOTAL_HIT_COUNT_FEATURE, evidence.getTotalHitCount());
    }
}
