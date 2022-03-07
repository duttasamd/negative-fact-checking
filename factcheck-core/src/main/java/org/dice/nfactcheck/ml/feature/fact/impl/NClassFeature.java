/**
 * 
 */
package org.dice.nfactcheck.ml.feature.fact.impl;

import java.util.List;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.aksw.defacto.model.DefactoModel;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NClassFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof, java.util.Set)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        DefactoModel model = proof.getModel();
        // System.out.println("Extracting class feature");
        String proofPhrase = proof.getProofPhrase();

        if(model.getObjectLabel("en").length() > 3 && proofPhrase.contains(model.getObjectLabel("en")) && proofPhrase.contains(model.getSubjectLabel("en"))) {
            // System.out.println("Acceptable : " + proof.getProofPhrase());
            proof.getFeatures().setValue(NAbstractFactFeatures.CLASS, "acceptable");
        } else {
            String[] subjectParts = model.getSubjectLabel("en").split("[\\s*\\.,]");
            String[] objectParts = model.getObjectLabel("en").split("[\\s*\\.,]");

            boolean isAcceptable = false;

            for (String subjectPart : subjectParts) {
                if(subjectPart.length() > 3 && proofPhrase.toLowerCase().contains(subjectPart.toLowerCase())) {
                    // System.out.println("Subject Lookup : " + subjectPart);
                    isAcceptable = true;
                    break;
                }
            }

            if(isAcceptable) {
                isAcceptable = false;
                for (String objectPart : objectParts) {
                    // System.out.println("Object Lookup : " + objectPart);
                    if(!(objectPart.isEmpty() || objectPart.isBlank()) && objectPart.length() > 3 && proofPhrase.toLowerCase().contains(objectPart.toLowerCase())) {
                        // System.out.println("Acceptable");
                        isAcceptable = true;
                        break;
                    }
                }
            }

            if(isAcceptable) {
                // System.out.println("Acceptable : " + proof.getProofPhrase());
                proof.getFeatures().setValue(NAbstractFactFeatures.CLASS, "acceptable");
            } else {
                // System.out.println("Unacceptable : " + proof.getProofPhrase());
                proof.getFeatures().setValue(NAbstractFactFeatures.CLASS, "unacceptable");
            }
        }

        NComplexProof nproof = (NComplexProof) proof;
        if(nproof.getIsSubjectWildcard()) {
            proof.getFeatures().setValue(NAbstractFactFeatures.IS_SUBJECT_WILDCARD, 1.0);
        } else {
            proof.getFeatures().setValue(NAbstractFactFeatures.IS_SUBJECT_WILDCARD, 0.0);
        }

    }
}
