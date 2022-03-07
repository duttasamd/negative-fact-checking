/**
 * 
 */
package org.dice.nfactcheck.ml.feature.evidence.impl;

import java.util.Collections;
import java.util.List;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.dice.factcheck.topicterms.TopicTermsCoherence;
import org.dice.factcheck.topicterms.Word;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NTopicMajorityWebFeature extends AbstractEvidenceFeature {
    
    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.Feature#extractFeature(org.aksw.defacto.evidence.Evidence)
     */
    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        
        double sumScore = 0D;
        double maxScore = 0D;
        
        // prepare the callables
        for (WebSite website : evidence.getAllWebSites() ) {
            
            int topicMajority = 0;
            
            List<Word> topicTerms = website.getOccurringTopicTerms();
            Collections.sort(topicTerms, new TopicTermsCoherence.WordComparator());
            
            // we want this only for the first three websites
            for ( int i = 0 ; i < topicTerms.size() ; i++) {
                
                // we need to compare each website with each website
                for ( WebSite allWebSite : evidence.getAllWebSites() ) {
                
                    // exclude the identity comparison
                    if ( !allWebSite.equals(website) ) {

                        if ( allWebSite.getText().toLowerCase().contains(topicTerms.get(i).getWord().toLowerCase()) ) topicMajority++;
                    }
                }
            }
            website.setTopicMajorityWebFeature(website.getScore() * topicMajority);
            maxScore = Math.max(maxScore, website.getTopicMajorityWebFeature());
            sumScore += website.getTopicMajorityWebFeature();
        }
        
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOPIC_MAJORITY_WEB_SUM, sumScore);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOPIC_MAJORITY_WEB_MAX, maxScore);
    }
    
}
