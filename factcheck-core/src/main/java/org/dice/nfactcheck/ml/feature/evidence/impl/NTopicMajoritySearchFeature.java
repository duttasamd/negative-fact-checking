/**
 * 
 */
package org.dice.nfactcheck.ml.feature.evidence.impl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NTopicMajoritySearchFeature extends AbstractEvidenceFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.Feature#extractFeature(org.aksw.defacto.evidence.Evidence)
     */
    @Override
    public void extractFeature(Evidence evidence) {
        NEvidence nevidence = (NEvidence) evidence;
        List<WebSite> allWebsites = new ArrayList<WebSite>();
        for ( List<WebSite> entry : evidence.getWebSites().values() ) allWebsites.addAll(entry);

        double sumScore = 0D;
        double maxScore = 0D;
        
        // i is the index of the website in the row
        for ( int i = 0; i < evidence.getSimilarityMatrix().length; i++ ) {
            
            WebSite site = allWebsites.get(i);
            
            // j is the index of the websites in the columns
            for ( int j = 0; j < evidence.getSimilarityMatrix()[i].length ; j++ ) { 

                // TODO do we want to count the identity, if not j and i need to be different                
                if ( evidence.getSimilarityMatrix()[i][j] > Defacto.DEFACTO_CONFIG.getDoubleSetting("evidence", "WEBSITE_SIMILARITY_THRESHOLD") && (i != j) ) {
                    
                    site.setTopicMajoritySearchFeature(site.getScore() * evidence.getSimilarityMatrix()[i][j]);
                    maxScore = Math.max(maxScore, site.getTopicMajoritySearchFeature());
                    sumScore += site.getTopicMajoritySearchFeature();
                    
                }
            }
        }
        
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOPIC_MAJORITY_SEARCH_RESULT_SUM, sumScore);
        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.TOPIC_MAJORITY_SEARCH_RESULT_MAX, maxScore);
     
        // System.out.println("Topic majority search : " + Double.toString(maxScore) + " <===> " + Double.toString(sumScore));
    }
}
