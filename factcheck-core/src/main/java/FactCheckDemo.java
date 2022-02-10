import java.util.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice.factcheck.search.engine.elastic.ElasticSearchEngine;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;

import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.ml.feature.evidence.EvidenceFeatureExtractor;
import org.aksw.defacto.ml.feature.evidence.EvidenceScorer;
import org.aksw.defacto.ml.feature.fact.FactFeatureExtraction;
import org.aksw.defacto.ml.feature.fact.FactScorer;

import org.aksw.defacto.util.TimeUtil;
import org.aksw.defacto.search.crawl.*;
import org.aksw.defacto.evidence.*;


public class FactCheckDemo {
    public static void main(String... args) {
        Defacto.init();
        ElasticSearchEngine.init();

        final Model model = ModelFactory.createDefaultModel();
        model.read(DefactoModel.class.getResourceAsStream("/ElfriedeJelinek.ttl"), null,
                "TURTLE");

        DefactoModel defactoModel = new DefactoModel(model, "NB", true, Arrays.asList("en"));

        // System.out.println("SUBJECT :" + defactoModel.getSubjectLabel("en"));

        QueryGenerator queryGenerator = new QueryGenerator(defactoModel);
        Map<Pattern, MetaQuery> metaqueries = queryGenerator.getSearchEngineQueries("en");

        EvidenceCrawler crawler = new EvidenceCrawler(defactoModel, metaqueries);
        Evidence evidence  = crawler.crawlEvidence();
        evidence.setBoaPatterns("en", new ArrayList<>(metaqueries.keySet()));

        System.out.println("Starting feature extraction ... ");

        long startFactConfirmation = System.currentTimeMillis();
		FactFeatureExtraction factFeatureExtraction = new FactFeatureExtraction();
		factFeatureExtraction.extractFeatureForFact(evidence);
		System.out.println("Fact feature extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactConfirmation));
       
        System.out.println("Scoring fact features...");
        long startFactScoring = System.currentTimeMillis();
		FactScorer factScorer = new FactScorer();
		factScorer.scoreEvidence(evidence);
        
		System.out.println("Fact Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactScoring));
        System.out.println("Proofs : " + Integer.toString(evidence.getComplexProofs().size()));
        List<ComplexProof> sortedComplexProofs = new ArrayList<>(evidence.getComplexProofs());

        Collections.sort(sortedComplexProofs, (o1, o2) -> Double.compare(o1.getScore(),o2.getScore()));
            
        if(sortedComplexProofs.size() > 0) {
            for (ComplexProof proof : sortedComplexProofs) {
                if(proof.getScore() != 0) {
                    System.out.println("["+ Double.toString(proof.getScore()) +"] - " + proof.getWebSite().getUrl());
                    System.out.println(proof.getProofPhrase());
                }
            }
        }

        long startFeatureExtraction = System.currentTimeMillis();
		EvidenceFeatureExtractor featureCalculator = new EvidenceFeatureExtractor();
		featureCalculator.extractFeatureForEvidence(evidence);
		System.out.println("Evidence feature extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFeatureExtraction));
    
        System.out.println("Scoring evidence features...");
        long startEvidenceScoring = System.currentTimeMillis();
		EvidenceScorer evidenceScorer = new EvidenceScorer();
		evidenceScorer.scoreEvidence(evidence);
		System.out.println("Evidence Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startEvidenceScoring));

        System.out.println("Evidence : " + Double.toString(evidence.getDeFactoScore()));
    }
}
