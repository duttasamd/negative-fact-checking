package org.dice.nfactcheck.evidence;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;

import weka.core.Instance;

public class NEvidence extends Evidence {
    // protected HashSet<NComplexProof> complexProofs;
    protected List<Entry<String, Integer>> wilcardCountList;
    protected double factcheckScore;
    protected double nfactcheckScore;

    public double getFactcheckScore() {
        return factcheckScore;
    }

    public void setFactcheckScore(double factcheckScore) {
        this.factcheckScore = factcheckScore;
    }

    public double getNfactcheckScore() {
        return nfactcheckScore;
    }

    public void setNfactcheckScore(double nfactcheckScore) {
        this.nfactcheckScore = nfactcheckScore;
    }

    public NEvidence(DefactoModel model) {
        super(model);
        // this.complexProofs = new HashSet<NComplexProof>();
    }

    public NEvidence(DefactoModel model, Long totalHitCount, Set<Pattern> keySet) {
        super(model, totalHitCount, keySet);
        // this.complexProofs = new HashSet<NComplexProof>();
    }

    public void setWildcardCountList(List<Entry<String, Integer>> wilcardCountList) {
        this.wilcardCountList = wilcardCountList;
    }

    public List<Entry<String, Integer>> getWildcardCountList() {
        return this.wilcardCountList;
    }

    public void removeComplexProofs(Set<ComplexProof> toRemoveProofs) {
        // System.out.println("Removing : " + Integer.toString(toRemoveProofs.size()));
        complexProofs.removeAll(toRemoveProofs);
    }

    public void removeComplexProof(ComplexProof toRemoveProof) {
        System.out.println("Removing complex proof.");
        complexProofs.remove(toRemoveProof);
    }

    public List<Pattern> getEnBoaPatterns() {
        return boaPatterns.get("en");
    }

    public Instance getFeatures() {
    
        if ( features == null ) {

            this.features = new Instance(NAbstractEvidenceFeatures.provenance.numAttributes());
            this.features.setDataset(NAbstractEvidenceFeatures.provenance);
            this.features.setValue(NAbstractEvidenceFeatures.CLASS, String.valueOf(model.isCorrect()));
        }
        
        return features;
    }
    
}
