package org.dice.nfactcheck.ml.feature.evidence;

import java.util.HashSet;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.EvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.EvidenceFeatureExtractor;
import org.dice.nfactcheck.ml.feature.evidence.impl.*;

public class NEvidenceFeatureExtractor {
    public static Set<EvidenceFeature> features = new HashSet<EvidenceFeature>();
    
    static {

    	EvidenceFeatureExtractor.features.add(new NDomainRangeCheckFeature());
        EvidenceFeatureExtractor.features.add(new NPageRankFeature());
        EvidenceFeatureExtractor.features.add(new NTotalHitCountFeature());
        EvidenceFeatureExtractor.features.add(new NTopicCoverageFeature());
        EvidenceFeatureExtractor.features.add(new NTopicMajorityWebFeature());
        EvidenceFeatureExtractor.features.add(new NTopicMajoritySearchFeature());
        EvidenceFeatureExtractor.features.add(new NProofFeature());
        EvidenceFeatureExtractor.features.add(new NNameFeature());
        EvidenceFeatureExtractor.features.add(new NERWildcardMatchFeature());
    }
    
    /**
     * 
     * @param evidence
     */
    public void extractFeatureForEvidence(Evidence evidence) {
        
        // score the collected evidence with every feature extractor defined
        for ( EvidenceFeature feature : EvidenceFeatureExtractor.features ) 
            feature.extractFeature(evidence);
        
        // we only need to add the feature vector to the weka instances object if we plan to write the training file
        if ( Defacto.DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE") )
            NAbstractEvidenceFeatures.provenance.add(evidence.getFeatures());
    }
}
