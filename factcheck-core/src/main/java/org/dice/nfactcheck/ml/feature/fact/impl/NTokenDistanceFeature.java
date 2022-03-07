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
public class NTokenDistanceFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.Feature#extractFeature(org.aksw.defacto.evidence.Evidence)
     */
    @Override
    public void extractFeature(ComplexProof complexProof, Evidence evidence) {
        complexProof.getFeatures().setValue(NAbstractFactFeatures.TOKEN_DISTANCE, complexProof.getProofPhrase().split(" ").length);
        
        String normalCaseProof = complexProof.getProofPhrase();
		String[] patternParts = normalCaseProof.split(" ");
		
		int tokenCount = patternParts.length;
		int characterCount = normalCaseProof.length();
		// the first entry is always empty so remove it, regex does not fit 100% 
		int upperCaseCharacterCount = normalCaseProof.split("(?=\\p{Lu})").length - 1; 
		
		// all characters minus the number of whitespaces divided by number of tokens
		// number of whitespaces is one smaller then token count
		double averageTokenLength = tokenCount > 0 ? (double)(characterCount - (tokenCount - 1)) / (double) tokenCount : 0;
		
		// count the number of all digits in a pattern
		int digitCount = 0;
		int nonAlphaSpaceCharacterCount = 0;
		for (int i = 0; i < normalCaseProof.length(); i++) {
			
			if (Character.isDigit(normalCaseProof.charAt(i))) digitCount++;
			if (!StringUtils.isAlphaSpace(normalCaseProof.charAt(i)+"")) nonAlphaSpaceCharacterCount++;
		}
		
		complexProof.getFeatures().setValue(NAbstractFactFeatures.CHARACTER_COUNT, Double.valueOf(characterCount));
		complexProof.getFeatures().setValue(NAbstractFactFeatures.UPPERCASE_LETTER_COUNT, Double.valueOf(upperCaseCharacterCount));
		complexProof.getFeatures().setValue(NAbstractFactFeatures.AVERAGE_TOKEN_LENGHT, averageTokenLength);
		complexProof.getFeatures().setValue(NAbstractFactFeatures.DIGIT_COUNT, Double.valueOf(digitCount));
		complexProof.getFeatures().setValue(NAbstractFactFeatures.COMMA_COUNT, Double.valueOf(StringUtils.countMatches(normalCaseProof, ",")));
		complexProof.getFeatures().setValue(NAbstractFactFeatures.NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS, Double.valueOf(nonAlphaSpaceCharacterCount));
    }
    
    public static void main(String[] args) {
		
    	System.out.println(StringUtils.countMatches("This , ,is ,a test", ","));
	}
}
