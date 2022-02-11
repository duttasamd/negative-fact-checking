package org.dice.nfactcheck.ml.feature.evidence.impl;

import java.util.List;
import java.util.Map;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NPageRankFeature extends AbstractEvidenceFeature {
    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
//        int pageRankSumConfirming = 0;
//        int pageRankSumNonConfirming = 0;
//        int numberOfConfirmingWebsites = 0;
//        int numberOfNonConfirmingWebsites = 0;
        
        double sumScore = 0D;
        double maxScore = 0D;
        
        // check all websites for each pattern
        for ( Map.Entry<Pattern, List<WebSite>> patternToWebSites : evidence.getWebSites().entrySet()) {
            for ( WebSite website : patternToWebSites.getValue() ) {

                // unassigned is 11 so don't use it in the sum
                //if ( website.getPageRank() >= 0 && website.getPageRank() < Defacto.DEFACTO_CONFIG.getIntegerSetting("evidence", "UNASSIGNED_PAGE_RANK") ) {
                	if ( website.getSearchRank() >= 0 ) {
//                    if ( website.getScore() > Constants.CONFIRMATION_THRESHOLD ) {
//                        
//                        pageRankSumConfirming += website.getPageRank();
//                        numberOfConfirmingWebsites++;
//                    }
//                    else {
//                        
//                        pageRankSumNonConfirming += website.getPageRank();
//                        numberOfNonConfirmingWebsites++;
//                    }
                    
                    website.setPageRankScore(website.getScore() * website.getSearchRank());
                    
                    maxScore = Math.max(maxScore, website.getPageRankScore());
                    sumScore += website.getPageRankScore();
                }
            }
        }
        
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.PAGE_RANK_MAX, maxScore);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.PAGE_RANK_SUM, sumScore);
        
//        Double numberOfConfirmingWebsitesAverage = (double) pageRankSumConfirming / (double) numberOfConfirmingWebsites;
//        evidence.getFeatures().setValue(AbstractFeature.PAGE_RANK_CONFIRMING_FEATURE, 
//                !numberOfConfirmingWebsitesAverage.isNaN() && !numberOfConfirmingWebsitesAverage.isInfinite() ? numberOfConfirmingWebsitesAverage : 0D);
//        
//        Double numberOfNonConfirmingWebsitesAverage = (double) pageRankSumNonConfirming / (double) numberOfNonConfirmingWebsites;
//        evidence.getFeatures().setValue(AbstractFeature.PAGE_RANK_NON_CONFIRMING_FEATURE, 
//                !numberOfNonConfirmingWebsitesAverage.isNaN() && !numberOfNonConfirmingWebsitesAverage.isInfinite() ? numberOfNonConfirmingWebsitesAverage : 0D);
    }
}
