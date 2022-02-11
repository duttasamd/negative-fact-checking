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
            System.out.println("Subject Wildcard");
            if(nProof.getMatchedObject() != null) {
                System.out.println("Comparing : " + nProof.getMatchedObject().toLowerCase() + " : " +  objectLabel);
                proof.getFeatures().setValue(NAbstractFactFeatures.QUERY_SUBOROBJ_SIMILARITY, lev.getSimilarity(nProof.getMatchedObject().toLowerCase(), objectLabel));
                objectLabel = nProof.getMatchedObject();
            }
        } else {
            System.out.println("Subject Wildcard");
            if(nProof.getMatchedSubject() != null) {
                System.out.println("Comparing : " + nProof.getMatchedSubject().toLowerCase() + " : " +  subjectLabel);
                proof.getFeatures().setValue(NAbstractFactFeatures.QUERY_SUBOROBJ_SIMILARITY, lev.getSimilarity(nProof.getMatchedSubject().toLowerCase(), subjectLabel));
                subjectLabel = nProof.getMatchedSubject();
            }
        }
    
        List<String> subjectLabels = Arrays.asList(subjectLabel.toLowerCase().split("[\\s*\\.,]"));
        List<String> objectLabels = Arrays.asList(objectLabel.toLowerCase().split("[\\s*\\.,]"));

        List<Pattern> patterns = evidence.getBoaPatterns();
		List<String> predicates = new ArrayList<>();
        
		for ( Pattern p : patterns ) {
			predicates.add(p.getNormalized().toLowerCase().trim());
		}

        String[] tokens = proof.getNormalizedProofPhrase().split("[\\s*\\.,]");

        int minIndexSubject = 100;
        int minIndexPredicate = 100;
        int minIndexObject = 100;

        int index = 0;
        for (String token : tokens) {
            if(token.isEmpty() || token.isBlank() ) {
                continue;
            }

            if(subjectLabels.contains(token.toLowerCase()) && index < minIndexSubject) {
                minIndexSubject = index;
            }

            if(predicates.contains(token.toLowerCase()) && index < minIndexPredicate) {
                minIndexPredicate = index;
            }

            if(objectLabels.contains(token.toLowerCase()) && index < minIndexObject) {
                minIndexObject= index;
            }
            index++;
        }

        proof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_SUBJECT_PREDICATE_INDEX_DISTANCE, Math.abs(minIndexSubject - minIndexPredicate));
        proof.getFeatures().setValue(NAbstractFactFeatures.DEPENDENCY_PREDICATE_OBJECT_INDEX_DISTANCE, Math.abs(minIndexObject - minIndexPredicate));
    }
}
