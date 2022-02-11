package org.aksw.defacto.search.concurrent;

import java.util.concurrent.Callable;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.fact.FactSearcher;
import org.aksw.defacto.search.fact.SubjectObjectFactSearcher;
import org.apache.log4j.Logger;
import org.dice.factcheck.proof.extract.SubjectObjectProofExtractor;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class WebSiteScoreCallable implements Callable<WebSite> {

    private DefactoModel model;
    protected Pattern pattern;
    private WebSite website;
    private Evidence evidence;
    protected FactSearcher searcher = new SubjectObjectProofExtractor();
    
    /**
     * 
     * @param website
     * @param evidence
     * @param model
     * @param patterns 
     */
    public WebSiteScoreCallable(WebSite website, Evidence evidence, DefactoModel model) {

        this.website  = website;
        this.model    = model;
        this.evidence = evidence;
    }

    public WebSiteScoreCallable(WebSite website, Evidence evidence, DefactoModel model, FactSearcher searcher) {

        this.website  = website;
        this.model    = model;
        this.evidence = evidence;
        this.searcher = searcher;
    }

    @Override
    public WebSite call() {
        
    	searcher.generateProofs(evidence, website, model, pattern);
        return website;
    }
}
