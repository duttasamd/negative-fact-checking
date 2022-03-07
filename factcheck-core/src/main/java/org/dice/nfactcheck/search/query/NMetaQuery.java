package org.dice.nfactcheck.search.query;

import java.util.ArrayList;
import java.util.List;

import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.topic.frequency.Word;


public class NMetaQuery extends  MetaQuery {
    private String wildcard;

    /**
     * 
     * @param subjectLabel
     * @param propertyLabel
     * @param objectLabel
     * @param language 
     * @param topicTerms
     */
    public NMetaQuery(String subjectLabel, String propertyLabel, String objectLabel, String language, List<Word> topicTerms, String wildcard) {
        
        super(subjectLabel, propertyLabel, objectLabel, language, topicTerms);
        this.wildcard = wildcard;
        if(wildcard.equals("subject")) {
            this.subjectLabel = "";
        } else {
            this.objectLabel = "";
        }
    }
    
    public NMetaQuery(String metaQuery) {

        super(metaQuery);

        if(this.subjectLabel.trim().isEmpty()) {
            this.wildcard = "subject";
        } else if(this.objectLabel.trim().isEmpty()) {
            this.wildcard = "object";
        }
    }

    public String getWildcard() {
        return this.wildcard;
    }
}
