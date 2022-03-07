import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice.factcheck.search.engine.elastic.ElasticSearchEngine;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NEvidenceFeatureExtractor;
import org.dice.nfactcheck.ml.feature.fact.NFactFeatureExtraction;
import org.dice.nfactcheck.ml.feature.fact.NFactScorer;
import org.dice.nfactcheck.ml.feature.fact.impl.NDependencyParseFeature;
import org.dice.nfactcheck.ml.feature.fact.impl.NERFeature;
import org.dice.nfactcheck.ml.feature.fact.impl.SubjectObjectFeature;
import org.dice.nfactcheck.patterns.ClosestPredicate;
import org.dice.nfactcheck.proof.filter.ProofFilter;
import org.dice.nfactcheck.search.crawl.NEvidenceCrawler;
import org.dice.nfactcheck.search.query.WQueryGenerator;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.ml.feature.fact.FactScorer;

public class TrainModel {
    private static int modelCount = 0;
    private static int proofCount = 0;
    public static void main(String[] args) throws IOException {
        Set<String> processedSubjects = new HashSet<>();
        Defacto.init();
        ElasticSearchEngine.init();
        ClosestPredicate.init();

        PrintWriter trainingFileWriter = new PrintWriter("data/results/mix_training.arff", "UTF-8");
        PrintWriter proofWriter = new PrintWriter("data/results/mix_proofs.txt", "UTF-8");

        try (Stream<String> lines = Files.lines(Paths.get("data/FactBench/train/mix/" + "train.txt"), Charset.defaultCharset())) {
            for (String line : lines.toArray(String[]::new)) {
                try {
                    // if(line.contains("award") && line.toLowerCase().contains("true") && !processedSubjects.contains(line) && !line.toLowerCase().contains("cher")) {
                        processedSubjects.add(line);
                        process(line, trainingFileWriter, proofWriter, "NFactcheck");
                        // System.out.println(line);
                        // System.out.println(modelCount++);
                    // }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } finally {
            trainingFileWriter.close();
            proofWriter.close();
        }
    }

    private static void process(String line, PrintWriter trainingFileWriter, PrintWriter proofWriter, String algorithm) {
        TrainModel.modelCount++;
        String[] splits = line.split("\\t");
        String triple = splits[0] + " " + splits[1] + " " + splits[2] + " .";
        System.out.println("[" + Integer.toString(TrainModel.modelCount) + "] Triple : " + triple);

        final Model model = ModelFactory.createDefaultModel();
        try( final InputStream in = new ByteArrayInputStream(triple.getBytes("UTF-8")) ) {
            model.read(in, null, "N-TRIPLE");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        try {
            boolean isCorrect = splits[3].toLowerCase().equals("true");

            DefactoModel defactoModel = new DefactoModel(model, splits[0] + splits[1] + splits[2], isCorrect, Arrays.asList("en"));
            System.out.println("===============================");
            System.out.println(defactoModel.getSubjectLabel("en") + " " + defactoModel.getPropertyUri() +
                " " + defactoModel.getObjectLabel("en"));

            proofWriter.println("=================");
            proofWriter.println(defactoModel.getSubjectLabel("en") + " " + defactoModel.getPropertyUri() +
            " " + defactoModel.getObjectLabel("en") + " -> " + Boolean.toString(defactoModel.isCorrect()));
            proofWriter.println();

            double factcheckScore = Factcheck.checkFact(defactoModel);
            proofWriter.println("Factcheck Score : " + Double.toString(factcheckScore));
            System.out.println("Factcheck Score : " + Double.toString(factcheckScore));

            if(factcheckScore < 0.7) {
                System.out.println("Processing in nfactcheck module...");
                defactoModel.wildcardNERTags = ClosestPredicate.getWildcardNerTags(defactoModel.getPropertyUri());

                WQueryGenerator queryGenerator = new WQueryGenerator(defactoModel);
                Map<Pattern, MetaQuery> metaqueries = queryGenerator.generateSearchQueries();

                NEvidenceCrawler crawler = new NEvidenceCrawler(defactoModel, metaqueries);
                NEvidence evidence  = (NEvidence) crawler.crawlEvidence();
                evidence.setBoaPatterns("en", new ArrayList<>(metaqueries.keySet()));
                evidence.setFactcheckScore(factcheckScore);

                Set<ComplexProof> complexProofs = evidence.getComplexProofs();

                SubjectObjectFeature subObjFeature = new SubjectObjectFeature();
        
                for (ComplexProof complexProof : complexProofs) {
                    NComplexProof nproof = (NComplexProof) complexProof;
                    subObjFeature.extract(nproof);
                }

                ProofFilter.filterProofs(evidence);

                complexProofs = evidence.getComplexProofs();

                NDependencyParseFeature dParseFeature = new NDependencyParseFeature();

                for (ComplexProof complexProof : complexProofs) {
                    dParseFeature.extractFeature(complexProof, evidence);
                }

                ProofFilter.filterProofs(evidence);
                complexProofs = evidence.getComplexProofs();

                if(complexProofs.size() > 0) {
                    NFactFeatureExtraction nfactFeatureExtraction = new NFactFeatureExtraction();
                    nfactFeatureExtraction.extractFeatureForFact(evidence);

                    NFactScorer nfactScorer = new NFactScorer();
                    nfactScorer.scoreEvidence(evidence);

                    NEvidenceFeatureExtractor nfeatureCalculator = new NEvidenceFeatureExtractor();
                    nfeatureCalculator.extractFeatureForEvidence(evidence);

                    proofWriter.println(evidence.getFeatures().toString());
                    proofWriter.println();

                    trainingFileWriter.println(evidence.getFeatures().toString());
                }                
            }
        } finally {
            proofWriter.flush();
            trainingFileWriter.flush();
        }
    } 
}
