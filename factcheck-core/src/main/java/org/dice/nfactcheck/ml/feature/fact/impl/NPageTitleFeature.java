/**
 * 
 */
package org.dice.nfactcheck.ml.feature.fact.impl;

import java.util.HashSet;
import java.util.Set;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NPageTitleFeature implements FactFeature {

    AbstractStringMetric metric = new SmithWaterman();

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        NComplexProof nProof = (NComplexProof) proof;

        Set<String> subjectLabels = null;
        Set<String> objectLabels  = null;
        
        if(nProof.getIsSubjectWildcard()) {
            subjectLabels = new HashSet<>();
            subjectLabels.add(nProof.getMatchedSubject());
            objectLabels = proof.getModel().getObjectLabels();
            objectLabels.addAll(proof.getModel().getObjectAltLabels());
        } else {
            objectLabels = new HashSet<>();
            objectLabels.add(nProof.getMatchedObject());
            subjectLabels = proof.getModel().getSubjectLabels();
            subjectLabels.addAll(proof.getModel().getSubjectAltLabels());
        }

        String pageTitle = proof.getWebSite().getTitle();
        float subjectSimilarity = 0f;
        float objectSimilarity = 0f;
        
        for ( String label : subjectLabels) {
            float sim = metric.getSimilarity(pageTitle, label);
            if ( sim >= subjectSimilarity ) subjectSimilarity = sim; 
        }
        
        for ( String label : objectLabels) {
            float sim = metric.getSimilarity(pageTitle, label);
            if ( sim >= objectSimilarity ) objectSimilarity = sim; 
        }

        // System.out.println("titlesubject : " + Double.toString(subjectSimilarity));
        // System.out.println("titleobject : " + Double.toString(objectSimilarity));
        
        proof.getFeatures().setValue(NAbstractFactFeatures.PAGE_TITLE_SUBJECT, subjectSimilarity);
        proof.getFeatures().setValue(NAbstractFactFeatures.PAGE_TITLE_OBJECT, objectSimilarity);
    }
}
