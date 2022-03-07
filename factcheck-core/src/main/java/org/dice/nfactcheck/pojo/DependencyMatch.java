package org.dice.nfactcheck.pojo;

import edu.stanford.nlp.ling.IndexedWord;

public class DependencyMatch {
    private String match;
    private int index;
    private int level;
    private IndexedWord predicate;
    
    public String getMatch() {
        return match;
    }
    public void setMatch(String match) {
        this.match = match;
    }
    public DependencyMatch(String match, int index, int level, IndexedWord predicate) {
        this.match = match;
        this.index = index;
        this.level = level;
        this.predicate = predicate;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    
    public IndexedWord getPredicate() {
        return predicate;
    }
    public void setPredicate(IndexedWord predicate) {
        this.predicate = predicate;
    }
}
