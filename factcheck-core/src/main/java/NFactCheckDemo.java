import java.util.Arrays;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.DefactoModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice.factcheck.search.engine.elastic.ElasticSearchEngine;
import org.dice.nfactcheck.patterns.ClosestPredicate;

public class NFactCheckDemo {
    public static void main(String[] args) {
        Defacto.init();
        ElasticSearchEngine.init();
        ClosestPredicate.init();

        final Model model = ModelFactory.createDefaultModel();
        model.read(DefactoModel.class.getResourceAsStream("/PaulHaarhuis.ttl"), null,
                "TURTLE");

        DefactoModel defactoModel = new DefactoModel(model, "NB", true, Arrays.asList("en"));

        double score = NFactcheck.checkFact(defactoModel);

        System.out.println("Score : " + Double.toString(score));
    }
}
