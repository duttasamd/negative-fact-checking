package org.dice.nfactcheck.search.crawl;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.search.crawl.SearchResultCallable;
import org.dice.nfactcheck.search.engine.elastic.NElasticSearchEngine;
import org.dice.nfactcheck.search.query.NMetaQuery;

public class NSearchResultCallable extends SearchResultCallable {
    public NSearchResultCallable(NMetaQuery query, Pattern pattern) {
        super(query, pattern);
        this.engine = new NElasticSearchEngine();
    }
}
