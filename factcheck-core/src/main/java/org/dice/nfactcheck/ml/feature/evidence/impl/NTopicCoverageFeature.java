package org.dice.nfactcheck.ml.feature.evidence.impl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.dice.factcheck.topicterms.Word;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NTopicCoverageFeature extends AbstractEvidenceFeature {
    
    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        int totalNumberOfConfirmingWebSites = 0;
        int totalNumberOfNonConfirmingWebSites = 0;
        float totalTopicCoverageConfirming = 0;
        float totalTopicCoverageNonConfirming = 0;
        
        double sumScore = 0D;
        double maxScore = 0D;
        
        // go threw all websearch results and calculate the topic coverage for each page
        for ( List<WebSite> websites : evidence.getWebSites().values() ) {
            for ( WebSite website : websites) {

                List<Word> topicTermsInWebSite = new ArrayList<Word>(evidence.getTopicTerms().get(website.getLanguage()));
                topicTermsInWebSite.retainAll(website.getOccurringTopicTerms());

                website.setTopicCoverageScore(website.getScore() * ((float) topicTermsInWebSite.size() / (float) evidence.getTopicTerms().size()));
                
                maxScore = Math.max(maxScore, website.getTopicCoverageScore());
                sumScore += website.getTopicCoverageScore();
                
                
//                if ( website.getScore() > Constants.CONFIRMATION_THRESHOLD ) {
//                    
//                    totalTopicCoverageConfirming += (float) topicTermsInWebSite.size() / (float) evidence.getTopicTerms().size();
//                    totalNumberOfConfirmingWebSites++;
//                }
//                else {
//
//                    totalTopicCoverageNonConfirming += (float) topicTermsInWebSite.size() / (float) evidence.getTopicTerms().size();
//                    totalNumberOfNonConfirmingWebSites++;
//                }
            }
        }
        
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOPIC_COVERAGE_MAX, maxScore);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOPIC_COVERAGE_SUM, sumScore);
        
//        // average the topic coverage of confirming pages
//        Double topicCoverageAverageConfirming = (double) totalTopicCoverageConfirming / (double) totalNumberOfConfirmingWebSites;
//        evidence.getFeatures().setValue(AbstractFeature.TOPIC_COVERAGE_CONFIRMING_FEATURE, 
//                !topicCoverageAverageConfirming.isNaN() && !topicCoverageAverageConfirming.isInfinite() ? topicCoverageAverageConfirming : 0D);
//        
//        // average the topic coverage of non confirming pages
//        Double topicCoverageAverageNonConfirming = (double) totalTopicCoverageNonConfirming / (double) totalNumberOfNonConfirmingWebSites;
//        evidence.getFeatures().setValue(AbstractFeature.TOPIC_COVERAGE_NON_CONFIRMING_FEATURE, 
//                !topicCoverageAverageNonConfirming.isNaN() && !topicCoverageAverageNonConfirming.isInfinite() ? topicCoverageAverageNonConfirming : 0D);
    }
}
