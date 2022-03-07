/**
 * 
 */
package org.dice.nfactcheck.ml.feature.fact.impl;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.apache.commons.lang3.StringUtils;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NEndOfSentenceCharacterFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        proof.getFeatures().setValue(NAbstractFactFeatures.END_OF_SENTENCE_DOT, StringUtils.countMatches(proof.getProofPhrase(), ".") );
        proof.getFeatures().setValue(NAbstractFactFeatures.END_OF_SENTENCE_EXCLAMATION_MARK, StringUtils.countMatches(proof.getProofPhrase(), "!"));
        proof.getFeatures().setValue(NAbstractFactFeatures.END_OF_SENTENCE_QUESTION_MARK, StringUtils.countMatches(proof.getProofPhrase(), "?"));
    }
}
