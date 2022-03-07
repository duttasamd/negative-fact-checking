import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.wordnet.WordNetExpansion;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;
import org.dice.nfactcheck.ml.feature.fact.NAbstractFactFeatures;

public class Test {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter trainingFileWriter = new PrintWriter("data/results/evidence_template.arff", "UTF-8");

        trainingFileWriter.println(NAbstractEvidenceFeatures.provenance.toString());
        trainingFileWriter.flush();

        trainingFileWriter.close();


    }
}
