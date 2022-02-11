package org.dice.nfactcheck.search.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Statement;
import org.dice.nfactcheck.patterns.ClosestPredicate;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.*;

public class WQueryGenerator extends QueryGenerator {

    public WQueryGenerator(DefactoModel model) {
        super(model);
    }

    
    public Map<Pattern,MetaQuery> generateSearchQueries() {
        String language = "en";
        Statement fact = model.getFact();
        Map<Pattern, MetaQuery> queryStrings = null;
        String subjectLabel = model.getSubjectLabelNoFallBack(language);
        String objectLabel  = model.getObjectLabelNoFallBack(language);


        BoaPatternSearcher.init();
        ClosestPredicate.init();

        List<String> predicateList = ClosestPredicate.getClosestPredicates(fact.getPredicate().getURI());
        if(predicateList != null) {
            queryStrings = new HashMap<Pattern,MetaQuery>();
            for (String predicate : predicateList) {
                Pattern pattern = new Pattern("?D? " + predicate + " ?R?", "en");
                pattern.naturalLanguageRepresentationWithoutVariables = predicate;
                MetaQuery metaQuery = new NMetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, "object");
                queryStrings.put(pattern, metaQuery);
            }
        }

        return queryStrings;
    }
}