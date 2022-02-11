import java.util.*;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.util.TimeUtil;
import org.aksw.defacto.evidence.*;
import org.aksw.defacto.ml.feature.evidence.EvidenceFeatureExtractor;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NEvidenceScorer;
import org.dice.nfactcheck.ml.feature.fact.NFactFeatureExtraction;
import org.dice.nfactcheck.ml.feature.fact.NFactScorer;
import org.dice.nfactcheck.ml.feature.fact.impl.NDependencyParseFeature;
import org.dice.nfactcheck.ml.feature.fact.impl.NERFeature;
import org.dice.nfactcheck.patterns.ClosestPredicate;
import org.dice.nfactcheck.proof.filter.ProofFilter;
import org.dice.nfactcheck.search.crawl.NEvidenceCrawler;
import org.dice.nfactcheck.search.query.WQueryGenerator;

public class NFactcheck {



    public static double checkFact(DefactoModel model) {
        // double factcheckScore = Factcheck.checkFact(model);
        
        model.wildcardNERTag = ClosestPredicate.getWildcardNerTag(model.getPropertyUri());
        System.out.println("Wildcard NER Set = " + model.wildcardNERTag);
        WQueryGenerator queryGenerator = new WQueryGenerator(model);
        Map<Pattern, MetaQuery> metaqueries = queryGenerator.generateSearchQueries();

        NEvidenceCrawler crawler = new NEvidenceCrawler(model, metaqueries);
        NEvidence evidence  = (NEvidence) crawler.crawlEvidence();
        evidence.setBoaPatterns("en", new ArrayList<>(metaqueries.keySet()));

        Set<ComplexProof> complexProofs = evidence.getComplexProofs();

        NERFeature nerFeature = new NERFeature();

        for (ComplexProof complexProof : complexProofs) {
            nerFeature.extractFeature(complexProof, evidence);
        }

        ProofFilter.filterProofs(evidence);

        complexProofs = evidence.getComplexProofs();

        NDependencyParseFeature dParseFeature = new NDependencyParseFeature();

        for (ComplexProof complexProof : complexProofs) {
            dParseFeature.extractFeature2(complexProof, evidence);
        }

        ProofFilter.filterProofs(evidence);

        NFactFeatureExtraction nfactFeatureExtraction = new NFactFeatureExtraction();
		nfactFeatureExtraction.extractFeatureForFact(evidence);

        NFactScorer nfactScorer = new NFactScorer();
		nfactScorer.scoreEvidence(evidence);

        EvidenceFeatureExtractor featureCalculator = new EvidenceFeatureExtractor();
		featureCalculator.extractFeatureForEvidence(evidence);

        NEvidenceScorer nevidenceScorer = new NEvidenceScorer();
		nevidenceScorer.scoreEvidence(evidence);

        List<ComplexProof> sortedComplexProofs = new ArrayList<>(evidence.getComplexProofs());

        Collections.sort(sortedComplexProofs, (o1, o2) -> Double.compare(o1.getScore(),o2.getScore()));
            
        if(sortedComplexProofs.size() > 0) {
            for (ComplexProof proof : sortedComplexProofs) {
                NComplexProof nproof = (NComplexProof) proof;
                System.out.println("["+ Double.toString(proof.getScore()) + " " + nproof.getMatchedObject() + "] "+ " -> " + proof.getWebSite().getUrl());
                System.out.println(proof.getProofPhrase());
                System.out.println(proof.getFeatures().toString());
            }
        }

        return evidence.getDeFactoScore();

    } 
}
