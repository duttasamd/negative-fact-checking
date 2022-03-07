package org.dice.nfactcheck.search.crawl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.concurrent.WebSiteScoreCallable;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.SearchResult;
import org.dice.factcheck.topicterms.Word;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.patterns.ClosestPredicate;
import org.dice.nfactcheck.proof.extract.PredicateBasedProofExtractor;
import org.dice.nfactcheck.search.concurrent.NWebSiteScoreCallable;
import org.dice.nfactcheck.search.query.NMetaQuery;
import org.dice.factcheck.search.engine.elastic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NEvidenceCrawler extends EvidenceCrawler {

	private static final Logger LOGGER = LoggerFactory.getLogger(NEvidenceCrawler.class);
    public static Map<DefactoModel,NEvidence> evidenceCache = new HashMap<DefactoModel,NEvidence>();
    
    /**
     * 
     * @param model
     * @param patternToQueries
     */
    public NEvidenceCrawler(DefactoModel model, Map<Pattern, MetaQuery> queries) {
        super(model, queries);
    }

    @Override
    protected Set<SearchResult> generateSearchResultsInParallel() {

        Set<SearchResult> results = new HashSet<SearchResult>();
        Set<NSearchResultCallable> searchResultCallables = new HashSet<NSearchResultCallable>();
        
        // collect the urls for a particular pattern
        // could be done in parallel 
        for ( Map.Entry<Pattern, MetaQuery> entry : this.patternToQueries.entrySet())
            searchResultCallables.add(new NSearchResultCallable((NMetaQuery) entry.getValue(), entry.getKey()));
        
        try {
        	
        	ExecutorService executor = Executors.newFixedThreadPool(Defacto.DEFACTO_CONFIG.getIntegerSetting("crawl", "NUMBER_OF_SEARCH_RESULTS_THREADS"));
            for ( Future<SearchResult> result : executor.invokeAll(searchResultCallables)) {

                results.add(result.get());
            }
            executor.shutdownNow();
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        
        return results;
    }

    @Override
    protected void scoreSearchResults(Set<SearchResult> searchResults, DefactoModel model, Evidence evidence) {

        // ########################################
    	// 1. Score the websites 
    	 Set<WebSite> results = new HashSet<WebSite>();
        List<WebSiteScoreCallable> scoreCallables =  new ArrayList<WebSiteScoreCallable>();
        for ( SearchResult result : searchResults ) 
            for (WebSite site : result.getWebSites() ) {
                WebSiteScoreCallable websiteScoreCallable 
                    = new NWebSiteScoreCallable(site, evidence, model, new PredicateBasedProofExtractor(), result.getPattern());
                scoreCallables.add(websiteScoreCallable);
            }
                
        
        // nothing found, nothing to score
        if ( scoreCallables.isEmpty() ) return;
                    
        long start = System.currentTimeMillis();
        // wait als long as the scoring needs, and score every website in parallel
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
    		for ( Future<WebSite> result : executor.invokeAll(scoreCallables)) {
    			results.add(result.get());
            }
        }   
        catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
        //this.executeAndWaitAndShutdownCallables(Executors.newFixedThreadPool(100), scoreCallables);
        
        // ########################################
    	// 2. parse the pages to look for dates
        // List<RegexParseCallable> parsers = new ArrayList<RegexParseCallable>();
        // List<ComplexProof> proofs = new ArrayList<ComplexProof>(evidence.getComplexProofs());
        
        // // create |CPU| parsers for n websites and split them to the parsers
        // for ( ComplexProof proofsSublist : proofs)
        // 	parsers.add(new RegexParseCallable(proofsSublist));
        
        // start = System.currentTimeMillis();
        // LOGGER.debug(String.format("Proof parsing %s websites per parser, %s at a time!", parsers.size(), Constants.NUMBER_NLP_STANFORD_MODELS));
        // executeAndWaitAndShutdownCallables(Executors.newFixedThreadPool(
        // 		Constants.NUMBER_NLP_STANFORD_MODELS), parsers);
        LOGGER.debug(String.format("Proof parsing finished in %sms!", (System.currentTimeMillis() - start)));
        
        // this.extractDates(evidence);
    }

    public NEvidence crawlEvidence() {
    	
    	NEvidence evidence = null;
        ElasticSearchEngine.init();

    	if ( !evidenceCache.containsKey(this.model) ) {
            Set<SearchResult> searchResults = this.generateSearchResultsInParallel();
            
            // multiple pattern bring the same results but we dont want that
            this.filterSearchResults(searchResults);

            Long totalHitCount = 0L; // sum over the n*m query results        
            for ( SearchResult result : searchResults ) {
            	totalHitCount += result.getTotalHitCount();  
                // System.out.println(result.getWebSites().size());
            }
            // System.out.println("Total hit count : " + totalHitCount);
                    
            evidence = new NEvidence(model, totalHitCount, patternToQueries.keySet());
    	    scoreSearchResults(searchResults, model, evidence);
                    
            // start multiple threads to download the text of the websites simultaneously
            for ( SearchResult result : searchResults ) 
                evidence.addWebSites(result.getPattern(), result.getWebSites());
            
            evidenceCache.put(model, evidence);
    	}
    	evidence = evidenceCache.get(model);
    	
        for ( String language : model.getLanguages() ) {
            List<Word> topicTerms = new ArrayList<Word>();
                
            if(topicTerms.isEmpty())
            {
                String wildcard = ClosestPredicate.getWildcardType(evidence.getModel().getPropertyUri());
                Word topicTerm = null;

                if(wildcard.equals("subject")) {
                    String objectLabel = evidence.getModel().getObjectLabel(language);
                    topicTerm = new Word(objectLabel, 0);
                } else {
                    String subjectLabel = evidence.getModel().getSubjectLabel(language);
                    topicTerm = new Word(subjectLabel, 0);
                }
                
                if(!topicTerm.getWord().equals(Constants.NO_LABEL)) {
                    topicTerms.add(topicTerm);

                    List<Pattern> patterns = evidence.getEnBoaPatterns();
                    for ( Pattern p : patterns ) {
                        Word predicate = new Word(p.getNormalized().trim(), 0);
                        topicTerms.add(predicate);
                    }
                    evidence.setTopicTerms(language, topicTerms);
                    evidence.setTopicTermVectorForWebsites(language);
                }
            }
        }
        evidence.calculateSimilarityMatrix();
        
        return evidence;
    }
}
