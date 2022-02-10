import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import java.util.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice.factcheck.search.engine.elastic.ElasticSearchEngine;

import javassist.bytecode.stackmap.BasicBlock.Catch;

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

public class TestModel {
    private static int modelCount = 0;
    public static void main(String[] args) throws IOException {
        Defacto.init();
        ElasticSearchEngine.init();

        PrintWriter writer = new PrintWriter("data/results/results_factcheck_.txt", "UTF-8");
        try (Stream<String> lines = Files.lines(Paths.get("data/FactBench/test/domain/" + "test.txt"), Charset.defaultCharset())) {
            for (String line : lines.toArray(String[]::new)) {
                try {
                    // System.out.println("Line : " + line);
                    process(line, writer, "Factcheck");
                    
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } finally {
            writer.close();
        }
    }

    private static void process(String line, PrintWriter pw, String algorithm) {
        TestModel.modelCount++;
        String[] splits = line.split("\\t");
        String triple = splits[0] + " " + splits[1] + " " + splits[2] + " .";
        System.out.println("Triple : " + triple);
        final Model model = ModelFactory.createDefaultModel();
        try( final InputStream in = new ByteArrayInputStream(triple.getBytes("UTF-8")) ) {
            /* Naturally, you'd substitute the syntax of your actual
            * content here rather than use N-TRIPLE.
            */
            model.read(in, null, "N-TRIPLE");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        try {
            
            DefactoModel defactoModel = new DefactoModel(model, splits[0] + splits[1] + splits[2], true, Arrays.asList("en"));
            System.out.println("here");
            // defactoModel.setCorrect(true);
            System.out.println("===============================");
            System.out.println(defactoModel.getSubjectLabel("en") + " " + defactoModel.getPropertyUri() +
                " " + defactoModel.getObjectLabel("en"));
            
            double factCheckScore = processFactcheck(defactoModel);

            StringBuilder resultTriple = new StringBuilder();
            resultTriple.append("<http://swc2019.dice-research.org/task/dataset/s-");
            resultTriple.append(String.format("%5s", Integer.toString(TestModel.modelCount)).replace(" ", "0"));
            resultTriple.append("> <http://swc2017.aksw.org/hasTruthValue> ");
            resultTriple.append(factCheckScore);
            resultTriple.append("^^<http://www.w3.org/2001/XMLSchema#double> .");

            pw.println(resultTriple.toString());
            pw.flush();
            System.out.println(resultTriple.toString());
            System.out.println("Processed model number : " + Integer.toString(TestModel.modelCount));
        } catch (Exception ex) {
            // System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static double processFactcheck(DefactoModel defactoModel) {
        QueryGenerator queryGenerator = new QueryGenerator(defactoModel);
        Map<Pattern, MetaQuery> metaqueries = queryGenerator.getSearchEngineQueries("en");

        EvidenceCrawler crawler = new EvidenceCrawler(defactoModel, metaqueries);
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
