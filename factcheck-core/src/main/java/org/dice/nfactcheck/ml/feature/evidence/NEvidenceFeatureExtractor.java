package org.dice.nfactcheck.ml.feature.evidence;

import java.util.HashSet;
import java.util.Set;
import org.aksw.defacto.ml.feature.evidence.EvidenceFeature;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.impl.*;

public class NEvidenceFeatureExtractor {
    public static Set<EvidenceFeature> features = new HashSet<EvidenceFeature>();
    
    static {

    	NEvidenceFeatureExtractor.features.add(new NDomainRangeCheckFeature());
        NEvidenceFeatureExtractor.features.add(new NPageRankFeature());
        NEvidenceFeatureExtractor.features.add(new NTotalHitCountFeature());
        
        NEvidenceFeatureExtractor.features.add(new NTopicCoverageFeature());
        NEvidenceFeatureExtractor.features.add(new NTopicMajorityWebFeature());
        NEvidenceFeatureExtractor.features.add(new NTopicMajoritySearchFeature());
        NEvidenceFeatureExtractor.features.add(new NProofFeature());
        NEvidenceFeatureExtractor.features.add(new NNameFeature());
        NEvidenceFeatureExtractor.features.add(new NERWildcardMatchFeature());
    }
    
    /**
     * 
     * @param evidence
     */
    public void extractFeatureForEvidence(NEvidence evidence) {
        
        // score the collected evidence with every feature extractor defined
        for ( EvidenceFeature feature : NEvidenceFeatureExtractor.features ) 
            feature.extractFeature(evidence);
            
        // we only need to add the feature vector to the weka instances object if we plan to write the training file
        // if ( Defacto.DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE") )
        NAbstractEvidenceFeatures.provenance.add(evidence.getFeatures());
    }
}
