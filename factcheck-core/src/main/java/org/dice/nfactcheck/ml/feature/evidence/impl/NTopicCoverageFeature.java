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
            }
        }
        
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOPIC_COVERAGE_MAX, maxScore);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOPIC_COVERAGE_SUM, sumScore);
     
        // System.out.println("Topic coverage : " + Double.toString(maxScore) + " <===> " + Double.toString(sumScore));
    }
}
