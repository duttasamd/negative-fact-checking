package org.dice.nfactcheck.search.concurrent;

import java.util.concurrent.Callable;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.concurrent.WebSiteScoreCallable;
import org.aksw.defacto.search.fact.FactSearcher;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NWebSiteScoreCallable extends WebSiteScoreCallable {
    

    public NWebSiteScoreCallable(WebSite website, Evidence evidence, DefactoModel model, FactSearcher searcher, Pattern pattern) {    
        super(website,evidence, model);
        this.searcher = searcher;
        this.pattern = pattern;
    }
}
