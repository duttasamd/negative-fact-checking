package org.dice.nfactcheck.ml.feature.evidence.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NNameFeature extends AbstractEvidenceFeature {

    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.MODEL_NAME, evidence.getModel().getName());
        String uri = evidence.getModel().getPropertyUri().replace("http://dbpedia.org/ontology/", "");
/*    	if ( uri.equals("office") ) uri = "leaderName";
    	evidence.getFeatures().setValue(AbstractEvidenceFeature.PROPERTY_NAME, uri);*/
    }
}
