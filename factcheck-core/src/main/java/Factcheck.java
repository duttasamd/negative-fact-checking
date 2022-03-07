import java.util.ArrayList;
import java.util.Map;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.EvidenceFeatureExtractor;
import org.aksw.defacto.ml.feature.evidence.EvidenceScorer;
import org.aksw.defacto.ml.feature.fact.FactFeatureExtraction;
import org.aksw.defacto.ml.feature.fact.FactScorer;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.dice.nfactcheck.ml.feature.evidence.NEvidenceFeatureExtractor;
import org.dice.nfactcheck.ml.feature.evidence.NEvidenceScorer;

public class Factcheck {
    public static double checkFact(DefactoModel model) {
        QueryGenerator queryGenerator = new QueryGenerator(model);
        Map<Pattern, MetaQuery> metaqueries = queryGenerator.getSearchEngineQueries("en");

        EvidenceCrawler crawler = new EvidenceCrawler(model, metaqueries);
        Evidence evidence  = crawler.crawlEvidence();
        evidence.setBoaPatterns("en", new ArrayList<>(metaqueries.keySet()));

		FactFeatureExtraction factFeatureExtraction = new FactFeatureExtraction();
		factFeatureExtraction.extractFeatureForFact(evidence);

        FactScorer factScorer = new FactScorer();
		factScorer.scoreEvidence(evidence);

        EvidenceFeatureExtractor featureCalculator = new EvidenceFeatureExtractor();
		featureCalculator.extractFeatureForEvidence(evidence);

        EvidenceScorer evidenceScorer = new EvidenceScorer();
		evidenceScorer.scoreEvidence(evidence);

        return evidence.getDeFactoScore();
    }
}
