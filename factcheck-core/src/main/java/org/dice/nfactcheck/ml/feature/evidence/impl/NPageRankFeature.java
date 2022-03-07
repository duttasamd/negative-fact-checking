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
                if ( website.getSearchRank() >= 0 ) {
                    
                    website.setPageRankScore(website.getScore() * website.getSearchRank());
                    
                    maxScore = Math.max(maxScore, website.getPageRankScore());
                    sumScore += website.getPageRankScore();
                }
            }
        }
        
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.PAGE_RANK_MAX, maxScore);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.PAGE_RANK_SUM, sumScore);
        
        // System.out.println("Page ranks : " + Double.toString(maxScore) + " >==> " + Double.toString(sumScore));
    }
}
