/**
 * 
 */
package org.dice.nfactcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NSubjectObjectSimilarityFeature implements FactFeature {
	
    Levenshtein lev	= new Levenshtein();

    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {    	
        NComplexProof nProof = (NComplexProof) proof;

        String subjectLabel = evidence.getModel().getSubjectLabel(proof.getLanguage()).toLowerCase();
    	String objectLabel = evidence.getModel().getObjectLabel(proof.getLanguage()).toLowerCase();

        if(nProof.getIsSubjectWildcard()) {
            if(nProof.getMatchedObject() != null) {
                // System.out.println("Comparing : " + nProof.getMatchedObject().toLowerCase() + " : " +  objectLabel);
                proof.getFeatures().setValue(NAbstractFactFeatures.QUERY_SUBOROBJ_SIMILARITY, lev.getSimilarity(nProof.getMatchedObject().toLowerCase(), objectLabel));
            }
        } else {
            if(nProof.getMatchedSubject() != null) {
                // System.out.println("Comparing : " + nProof.getMatchedSubject().toLowerCase() + " : " +  subjectLabel);
                proof.getFeatures().setValue(NAbstractFactFeatures.QUERY_SUBOROBJ_SIMILARITY, lev.getSimilarity(nProof.getMatchedSubject().toLowerCase(), subjectLabel));
            }
        }
    }
}
