package org.dice.nfactcheck.evidence;

import java.util.List;
import java.util.Map.Entry;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;

import edu.stanford.nlp.ling.CoreLabel;
import weka.core.Instance;

public class NComplexProof extends ComplexProof {

    protected boolean isTargetFullMatch;
    protected List<Entry<String,Integer>> probableWildcardWords;
    protected List<Entry<String, Integer>>  indexedSubjectList;
    protected List<Entry<String, Integer>>  indexedObjectList;
    protected List<List<CoreLabel>> nerResolvedProofPhrase;

    public List<List<CoreLabel>> getNerResolvedProofPhrase() {
        return nerResolvedProofPhrase;
    }

    public void setNerResolvedProofPhrase(List<List<CoreLabel>> nerResolvedProofPhrase) {
        this.nerResolvedProofPhrase = nerResolvedProofPhrase;
    }

    public List<Entry<String, Integer>> getIndexedSubjectList() {
        return indexedSubjectList;
    }

    public void setIndexedSubjectList(List<Entry<String, Integer>> indexedSubjectList) {
        this.indexedSubjectList = indexedSubjectList;
    }

    public List<Entry<String, Integer>> getIndexedObjectList() {
        return indexedObjectList;
    }

    public void setIndexedObjectList(List<Entry<String, Integer>> indexedObjectList) {
        this.indexedObjectList = indexedObjectList;
    }

    protected String mostProbableWildcard = null;
    protected double mostProbableWildcardScore = 0.0;


    protected boolean isSubjectPredicateMatch = false;
    protected boolean isPredicateObjectMatch = false;
    protected boolean isSubjectObjectSamePredicateMatch = false;
    protected boolean isSubjectObjectMatch = false;

    public int getSubjectPredicateIndexDistance() {
        return subjectPredicateIndexDistance;
    }

    public void setSubjectPredicateIndexDistance(int subjectPredicateIndexDistance) {
        this.subjectPredicateIndexDistance = subjectPredicateIndexDistance;
    }

    public int getPredicateObjectIndexDistance() {
        return predicateObjectIndexDistance;
    }

    public void setPredicateObjectIndexDistance(int predicateObjectIndexDistance) {
        this.predicateObjectIndexDistance = predicateObjectIndexDistance;
    }

    protected int subjectPredicateLevelDistance = 100;
    protected int subjectPredicateIndexDistance = 100;
    protected int predicateObjectLevelDistance = 100;
    protected int predicateObjectIndexDistance = 100;

    protected int subjectObjectLevelDistance = 100;


    protected String matchedSubject = null;
    public boolean isTargetFullMatch() {
        return isTargetFullMatch;
    }

    public boolean isSubjectPredicateMatch() {
        return isSubjectPredicateMatch;
    }

    public void setSubjectPredicateMatch(boolean isSubjectPredicateMatch) {
        this.isSubjectPredicateMatch = isSubjectPredicateMatch;
    }

    public boolean isPredicateObjectMatch() {
        return isPredicateObjectMatch;
    }

    public void setPredicateObjectMatch(boolean isPredicateObjectMatch) {
        this.isPredicateObjectMatch = isPredicateObjectMatch;
    }

    public boolean isSubjectObjectSamePredicateMatch() {
        return isSubjectObjectSamePredicateMatch;
    }

    public void setSubjectObjectSamePredicateMatch(boolean isSubjectObjectSamePredicateMatch) {
        this.isSubjectObjectSamePredicateMatch = isSubjectObjectSamePredicateMatch;
    }

    public boolean isSubjectObjectMatch() {
        return isSubjectObjectMatch;
    }

    public void setSubjectObjectMatch(boolean isSubjectObjectMatch) {
        this.isSubjectObjectMatch = isSubjectObjectMatch;
    }

    public int getSubjectPredicateLevelDistance() {
        return subjectPredicateLevelDistance;
    }

    public void setSubjectPredicateLevelDistance(int subjectPredicateLevelDistance) {
        this.subjectPredicateLevelDistance = subjectPredicateLevelDistance;
    }

    public int getPredicateObjectLevelDistance() {
        return predicateObjectLevelDistance;
    }

    public void setPredicateObjectLevelDistance(int predicateObjectLevelDistance) {
        this.predicateObjectLevelDistance = predicateObjectLevelDistance;
    }

    public int getSubjectObjectLevelDistance() {
        return subjectObjectLevelDistance;
    }

    public void setSubjectObjectLevelDistance(int subjectObjectLevelDistance) {
        this.subjectObjectLevelDistance = subjectObjectLevelDistance;
    }

    public void setTargetFullMatch(boolean isTargetFullMatch) {
        this.isTargetFullMatch = isTargetFullMatch;
    }

    public String getMatchedSubject() {
        return matchedSubject;
    }

    public void setMatchedSubject(String matchedSubject) {
        this.matchedSubject = matchedSubject;
    }

    public String getMatchedObject() {
        return matchedObject;
    }

    public void setMatchedObject(String matchedObject) {
        this.matchedObject = matchedObject;
    }

    public void setToRemove(boolean isToRemove) {
        this.isToRemove = isToRemove;
    }

    public void setSubjectWildcard(boolean isSubjectWildcard) {
        this.isSubjectWildcard = isSubjectWildcard;
    }

    protected String matchedObject = null;

    protected boolean isToRemove = false;
    protected boolean isSubjectWildcard = false;

    public List<Entry<String,Integer>> getProbableWildcardWords() {
        return probableWildcardWords;
    }

    public void setProbableWildcardWords(List<Entry<String, Integer>> probableWildcardWords) {
        this.probableWildcardWords = probableWildcardWords;
    }

    public void setIsSubjectWildcard(boolean isSubjectWildcard) {
        this.isSubjectWildcard = isSubjectWildcard;
    }

    public boolean getIsSubjectWildcard() {
        return this.isSubjectWildcard;
    }

    public NComplexProof(DefactoModel model, String firstLabel, String secondLabel, String occurrence,
            String normalizedOccurrence, WebSite site, boolean isTargetFullMatch, boolean isSubjectWildcard) {
        super(model, firstLabel, secondLabel, occurrence, normalizedOccurrence, site);
        this.instance = new Instance(NAbstractFactFeatures.nfactFeatures.numAttributes());
        this.isTargetFullMatch = isTargetFullMatch;
        this.isSubjectWildcard = isSubjectWildcard;
    }

    public void setIsToRemove(boolean remove) {
        this.isToRemove = remove;
    }

    public void setMostProbableWildcard(String mostProbableWildcard) {
        this.mostProbableWildcard = mostProbableWildcard;
    }

    public String getMostProbableWildcard() {
        return this.mostProbableWildcard;
    }

    public void setMostProbableWildcardScore(double mostProbableWildcardScore) {
        this.mostProbableWildcardScore = mostProbableWildcardScore;
    }

    public double getMostProbableWildcardScore() {
        return this.mostProbableWildcardScore;
    }

    public boolean getIsToRemove() {
        return this.isToRemove;
    }
    
}
