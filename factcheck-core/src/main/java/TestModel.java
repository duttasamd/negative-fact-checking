import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice.factcheck.search.engine.elastic.ElasticSearchEngine;
import org.dice.nfactcheck.patterns.ClosestPredicate;


import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.Defacto;

public class TestModel {
    static int modelCount = 0;
    static int nfactcheckCount = 0;

    static int countTrueStatement = 0;
    static int countFalseStatement = 0;

    static int countFactCheckTruePositive = 0;
    static int countNFactCheckTruePositive = 0;
    static int countFoundEvidenceNFactcheckTruePositive = 0;
    static int countFoundEvidenceFactcheckTruePositive = 0;
    static int countTruePositive = 0;
    static int countFoundEvidenceTruePositive = 0;

    static int countFactCheckTrueNegative = 0;
    static int countNFactCheckTrueNegative = 0;
    static int countFoundEvidenceNFactcheckTrueNegative = 0;
    static int countFoundEvidenceFactcheckTrueNegative = 0;
    static int countTrueNegative = 0;
    static int countFoundEvidenceTrueNegative = 0;

    static int countFactCheckFalsePositive = 0;
    static int countNFactCheckFalsePositive = 0;
    static int countFoundEvidenceNFactcheckFalsePositive = 0;
    static int countFoundEvidenceFactcheckFalsePositive = 0;
    static int countFalsePositive = 0;
    static int countFoundEvidenceFalsePositive = 0;

    static int countFactCheckFalseNegative = 0;
    static int countNFactCheckFalseNegative = 0;
    static int countFoundEvidenceNFactcheckFalseNegative = 0;
    static int countFoundEvidenceFactcheckFalseNegative = 0;
    static int countFalseNegative = 0;
    static int countFoundEvidenceFalseNegative = 0;


    public static void main(String[] args) throws IOException {
        Defacto.init();
        ElasticSearchEngine.init();
        ClosestPredicate.init();

        PrintWriter resultWriter = new PrintWriter("data/results/results_nfactcheck_mix.nt", "UTF-8");
        PrintWriter groundTruthWriter = new PrintWriter("data/results/ground_truth_nfactcheck_mix.nt", "UTF-8");
        PrintWriter log = new PrintWriter("data/results/log2202_nfactcheck_mix.txt", "UTF-8");

        try (Stream<String> lines = Files.lines(Paths.get("data/FactBench/test/mix/" + "test.txt"), Charset.defaultCharset())) {
            for (String line : lines.toArray(String[]::new)) {
                try {
                    process(line, groundTruthWriter, resultWriter, log, "NFactcheck");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } finally {
            groundTruthWriter.close();
            resultWriter.close();
            log.close();
        }
    }

    private static void process(String line, PrintWriter truthWriter, PrintWriter resultWriter, PrintWriter log, String algorithm) {
        TestModel.modelCount++;
        String[] splits = line.split("\\t");
        String triple = splits[0] + " " + splits[1] + " " + splits[2] + " .";
        System.out.println("Triple : " + triple);

        int truthValue = 0;
        
        if(splits[3].toLowerCase().equals("true")) {
            truthValue = 1;
        }


        final Model model = ModelFactory.createDefaultModel();
        try( final InputStream in = new ByteArrayInputStream(triple.getBytes("UTF-8")) ) {
            /* Naturally, you'd substitute the syntax of your actual
            * content here rather than use N-TRIPLE.
            */
            model.read(in, null, "N-TRIPLE");
        } catch (Exception ex) {
	    TestModel.modelCount--;
            ex.printStackTrace();
            return;
        }

        try {
            DefactoModel defactoModel = new DefactoModel(model, splits[0] + splits[1] + splits[2], true, Arrays.asList("en"));
            System.out.println("===============================");
            System.out.println(defactoModel.getSubjectLabel("en") + " " + defactoModel.getPropertyUri() +
                " " + defactoModel.getObjectLabel("en"));

            log.println("===============================");
            log.println(defactoModel.getSubjectLabel("en") + " " + defactoModel.getPropertyUri() +
            " " + defactoModel.getObjectLabel("en") + " => " + splits[3]);
            
            String datasetnumber = "<http://swc2019.dice-research.org/task/dataset/s-" + String.format("%5s", Integer.toString(TestModel.modelCount)).replace(" ", "0") + ">";
            StringBuilder groundTruthTriples = new StringBuilder();

            groundTruthTriples.append(datasetnumber + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .\n");
            groundTruthTriples.append(datasetnumber + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <" + defactoModel.getSubjectUri() + "> .\n");
            groundTruthTriples.append(datasetnumber + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <" + defactoModel.getPropertyUri() + "> .\n");
            groundTruthTriples.append(datasetnumber + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> <" + defactoModel.getObjectUri() + "> .\n");
            groundTruthTriples.append(datasetnumber + " <http://swc2017.aksw.org/hasTruthValue> \"" + Integer.toString(truthValue) + "\"^^<http://www.w3.org/2001/XMLSchema#double> .");
            
            double score = 0.0;
            double factcheckScore = 0.0;
            score = Factcheck.checkFact(defactoModel);
            factcheckScore = score;

            if(truthValue == 0) {
                TestModel.countFalseStatement++;
                if(score >= 0.5) {
                    TestModel.countFactCheckFalsePositive++;
                } else {
                    TestModel.countFactCheckTrueNegative++;
                }
            } else {
                TestModel.countTrueStatement++;
                if(score >= 0.5) {
                    TestModel.countFactCheckTruePositive++;
                } else {
                    TestModel.countFactCheckFalseNegative++;
                }
            }

            StringBuilder sb = new StringBuilder();

            sb.append("------------------------------\n");
            sb.append("Factcheck : " + Double.toString(factcheckScore));

            if(algorithm.equals("NFactcheck") && score < 0.7) {
                score = NFactcheck.checkFact(defactoModel, log);
                sb.append("; NFactcheckScore : " + Double.toString(score));

                TestModel.nfactcheckCount++;
                if(truthValue == 0) {
                    if(score >= 0.5) {
                        TestModel.countNFactCheckFalsePositive++;
                        if(defactoModel.isAcceptableProofsFound()) {
                            TestModel.countFoundEvidenceNFactcheckFalsePositive++;
                        }
                    } else {
                        TestModel.countNFactCheckTrueNegative++;
                        if(defactoModel.isAcceptableProofsFound()) {
                            TestModel.countFoundEvidenceNFactcheckTrueNegative++;
                        }
                    }
                } else {
                    if(score >= 0.5) {
                        TestModel.countNFactCheckTruePositive++;
                        if(defactoModel.isAcceptableProofsFound()) {
                            TestModel.countFoundEvidenceNFactcheckTruePositive++;
                        }
                    } else {
                        TestModel.countNFactCheckFalseNegative++;
                        if(defactoModel.isAcceptableProofsFound()) {
                            TestModel.countFoundEvidenceNFactcheckFalseNegative++;
                        }
                    }
                }

                if(truthValue == 0) {
                    if(factcheckScore >= 0.5) {
                        if(defactoModel.isAcceptableProofsFound()) {
                            TestModel.countFoundEvidenceFactcheckFalsePositive++;
                        }
                    } else {
                        if(defactoModel.isAcceptableProofsFound()) {
                            TestModel.countFoundEvidenceFactcheckTrueNegative++;
                        }
                    }
                } else {
                    TestModel.countTrueStatement++;
                    if(factcheckScore >= 0.5) {
                        if(defactoModel.isAcceptableProofsFound()) {
                            TestModel.countFoundEvidenceFactcheckTruePositive++;
                        }
                    } else {
                        if(defactoModel.isAcceptableProofsFound()) {
                            TestModel.countFoundEvidenceFactcheckFalseNegative++;
                        }
                    }
                }
            }

            if(truthValue == 0) {
                if(score >= 0.5) {
                    TestModel.countFalsePositive++;
                    sb.append("\nNFactcheck : False Positive ");
                    if(defactoModel.isAcceptableProofsFound()) {
                        sb.append(" - with proofs");
                        TestModel.countFoundEvidenceFalsePositive++;
                    }
                } else {
                    TestModel.countTrueNegative++;
                    sb.append("\nNFactcheck : True Negative ");
                    if(defactoModel.isAcceptableProofsFound()) {
                        sb.append(" - with proofs");
                        TestModel.countFoundEvidenceTrueNegative++;
                    }
                }
            } else {
                if(score >= 0.5) {
                    TestModel.countTruePositive++;
                    sb.append("\nNFactcheck : True Positive ");
                    if(defactoModel.isAcceptableProofsFound()) {
                        sb.append(" - with proofs");
                        TestModel.countFoundEvidenceTruePositive++;
                    }
                } else {
                    TestModel.countFalseNegative++;
                    sb.append("\nNFactcheck : False Negative ");
                    if(defactoModel.isAcceptableProofsFound()) {
                        sb.append(" - with proofs");
                        TestModel.countFoundEvidenceFalseNegative++;
                    }
                }
            }
            
            sb.append("\nFactcheck -> ");
            sb.append("TP: "); sb.append(TestModel.countFactCheckTruePositive);
            sb.append("["); sb.append(TestModel.countFoundEvidenceFactcheckTruePositive); sb.append("] ");
            sb.append("TN: "); sb.append(TestModel.countFactCheckTrueNegative);
            sb.append("["); sb.append(TestModel.countFoundEvidenceFactcheckTrueNegative); sb.append("] ");
            sb.append("FP: "); sb.append(TestModel.countFactCheckFalsePositive);
            sb.append("["); sb.append(TestModel.countFoundEvidenceFactcheckFalsePositive); sb.append("] ");
            sb.append("FN: "); sb.append(TestModel.countFactCheckFalseNegative);
            sb.append("["); sb.append(TestModel.countFoundEvidenceFactcheckFalseNegative); sb.append("] ");
            sb.append("Total : "); sb.append(TestModel.modelCount);
            sb.append("\n");
            sb.append("NFactcheck -> ");
            sb.append("TP: "); sb.append(TestModel.countNFactCheckTruePositive);
            sb.append("["); sb.append(TestModel.countFoundEvidenceNFactcheckTruePositive); sb.append("] ");
            sb.append("TN: "); sb.append(TestModel.countNFactCheckTrueNegative);
            sb.append("["); sb.append(TestModel.countFoundEvidenceNFactcheckTrueNegative); sb.append("] ");
            sb.append("FP: "); sb.append(TestModel.countNFactCheckFalsePositive);
            sb.append("["); sb.append(TestModel.countFoundEvidenceNFactcheckFalsePositive); sb.append("] ");
            sb.append("FN: "); sb.append(TestModel.countNFactCheckFalseNegative);
            sb.append("["); sb.append(TestModel.countFoundEvidenceNFactcheckFalseNegative); sb.append("] ");
            sb.append("Total : "); sb.append(TestModel.nfactcheckCount);
            sb.append("\n");
            sb.append("Final -> ");
            sb.append("TP: "); sb.append(TestModel.countTruePositive);
            sb.append("["); sb.append(TestModel.countFoundEvidenceTruePositive); sb.append("] ");
            sb.append("TN: "); sb.append(TestModel.countTrueNegative);
            sb.append("["); sb.append(TestModel.countFoundEvidenceTrueNegative); sb.append("] ");
            sb.append("FP: "); sb.append(TestModel.countFalsePositive);
            sb.append("["); sb.append(TestModel.countFoundEvidenceFalsePositive); sb.append("] ");
            sb.append("FN: "); sb.append(TestModel.countFalseNegative);
            sb.append("["); sb.append(TestModel.countFoundEvidenceFalseNegative); sb.append("] ");
            sb.append("Total : "); sb.append(TestModel.modelCount);
            sb.append("\n");

            log.println(sb.toString());            

            StringBuilder resultTriple = new StringBuilder();
            resultTriple.append(datasetnumber + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .\n");
            resultTriple.append(datasetnumber + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <" + defactoModel.getSubjectUri() + "> .\n");
            resultTriple.append(datasetnumber + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <" + defactoModel.getPropertyUri() + "> .\n");
            resultTriple.append(datasetnumber + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> <" + defactoModel.getObjectUri() + "> .\n");
            
            resultTriple.append(datasetnumber + " <http://swc2017.aksw.org/hasTruthValue> \"");
            resultTriple.append(score);
            resultTriple.append("\"^^<http://www.w3.org/2001/XMLSchema#double> .");

            truthWriter.println(groundTruthTriples.toString());
            truthWriter.flush();
            
            resultWriter.println(resultTriple.toString());
            resultWriter.flush();

            System.out.println(resultTriple.toString());
            System.out.println("Processed model number : " + Integer.toString(TestModel.modelCount));
        } catch (Exception ex) {
	    TestModel.modelCount--;
            // System.out.println(ex.getMessage());
            ex.printStackTrace();
        } finally {
            log.flush();
        }
    }
}
