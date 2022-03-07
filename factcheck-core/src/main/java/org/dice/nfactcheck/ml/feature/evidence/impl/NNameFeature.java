package org.dice.nfactcheck.ml.feature.evidence.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;
import org.dice.nfactcheck.patterns.ClosestPredicate;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NNameFeature extends AbstractEvidenceFeature {

    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        String uri = evidence.getModel().getPropertyUri().replace("http://dbpedia.org/ontology/", "");
        System.out.println(uri);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.PROPERTY_NAME, uri);

        double subjectWildcardFeature = 0.0;

        if(ClosestPredicate.getWildcardType(evidence.getModel().getPropertyUri()).equals("subject")) {
            subjectWildcardFeature = 1.0;
        }

        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.IS_SUBJECT_WILDCARD, subjectWildcardFeature);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.FACTCHECK_SCORE, nevidence.getFactcheckScore());

        // System.out.println("Property : " + uri);
    } 
}
