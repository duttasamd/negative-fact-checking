import java.io.PrintWriter;
import java.util.*;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.evidence.*;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NEvidenceFeatureExtractor;
import org.dice.nfactcheck.ml.feature.evidence.NEvidenceScorer;
import org.dice.nfactcheck.ml.feature.fact.NFactFeatureExtraction;
import org.dice.nfactcheck.ml.feature.fact.NFactScorer;
import org.dice.nfactcheck.ml.feature.fact.impl.NDependencyParseFeature;
import org.dice.nfactcheck.ml.feature.fact.impl.SubjectObjectFeature;
import org.dice.nfactcheck.patterns.ClosestPredicate;
import org.dice.nfactcheck.proof.filter.ProofFilter;
import org.dice.nfactcheck.search.crawl.NEvidenceCrawler;
import org.dice.nfactcheck.search.query.WQueryGenerator;

public class NFactcheck {



    public static double checkFact(DefactoModel model, PrintWriter log) {
        double factcheckScore = Factcheck.checkFact(model);
        
        model.wildcardNERTags = ClosestPredicate.getWildcardNerTags(model.getPropertyUri());
        WQueryGenerator queryGenerator = new WQueryGenerator(model);
        Map<Pattern, MetaQuery> metaqueries = queryGenerator.generateSearchQueries();

        NEvidenceCrawler crawler = new NEvidenceCrawler(model, metaqueries);
        NEvidence evidence  = (NEvidence) crawler.crawlEvidence();
        evidence.setBoaPatterns("en", new ArrayList<>(metaqueries.keySet()));

        // System.out.println("PROOF COUNT : " + Integer.toString(evidence.getComplexProofs().size()));

        Set<ComplexProof> complexProofs = evidence.getComplexProofs();

        SubjectObjectFeature subObjExtractor = new SubjectObjectFeature();

        for (ComplexProof complexProof : complexProofs) {
            NComplexProof nproof = (NComplexProof) complexProof;
            subObjExtractor.extract(nproof);
        }

        ProofFilter.filterProofs(evidence);

        complexProofs = evidence.getComplexProofs();

        NDependencyParseFeature dParseFeature = new NDependencyParseFeature();

        for (ComplexProof complexProof : complexProofs) {
            dParseFeature.extractFeature(complexProof, evidence);
        }

        ProofFilter.filterProofs(evidence);

        NFactFeatureExtraction nfactFeatureExtraction = new NFactFeatureExtraction();
		nfactFeatureExtraction.extractFeatureForFact(evidence);

        // System.out.println("Scoring Facts");
        NFactScorer nfactScorer = new NFactScorer();
		nfactScorer.scoreEvidence(evidence);

        // System.out.println("Extracting evidence features");
        NEvidenceFeatureExtractor nfeatureCalculator = new NEvidenceFeatureExtractor();
		nfeatureCalculator.extractFeatureForEvidence(evidence);

        if(evidence.getComplexProofs().size() > 0) {
            // System.out.println("Scoring");
            NEvidenceScorer nevidenceScorer = new NEvidenceScorer();
            nevidenceScorer.scoreEvidence(evidence);

            int count = 3;

            List<ComplexProof> sortedComplexProofs = new ArrayList<>(evidence.getComplexProofs());
            Collections.sort(sortedComplexProofs, (o1, o2) -> Double.compare(o2.getScore(),o1.getScore()));
            
            if(sortedComplexProofs.size() > 0) {
                for (ComplexProof proof : sortedComplexProofs) {
                    NComplexProof nproof = (NComplexProof) proof;
                    if(--count < 0) {
                        break;
                    }

                    if((evidence.getDeFactoScore() > 0.5 && model.isCorrect()) || (evidence.getDeFactoScore() < 0.5 && !model.isCorrect()) || nproof.getScore() > 0.5) {
                        model.setAcceptableProofsFound(true);
                        log.println("--");
                        log.println(proof.getWebSite().getUrl());
                        log.println(proof.getProofPhrase());
                    }
                }
            }

            return evidence.getDeFactoScore();
        } else {
            return evidence.getFactcheckScore();
        }
    }
}
