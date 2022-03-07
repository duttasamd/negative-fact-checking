package org.dice.nfactcheck.patterns;

import java.util.*;
import java.util.stream.Collectors;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.nlp.ner.NamedEntityTagNormalizer;

public class ClosestPredicate {
    private static Map<String, List<String>> closestPredicateMap;
    private static Map<String, String> wildcardMap;
    private static Map<String, Set<String>> nerMap;
    private static Map<String, String> subjectNerMap;

    public static void init() {
        wildcardMap = new HashMap<String, String>();
        nerMap = new HashMap<String, Set<String>>();
        subjectNerMap = new HashMap<String, String>();

        wildcardMap.put("http://dbpedia.org/ontology/birthPlace", "object");
        wildcardMap.put("http://dbpedia.org/ontology/deathPlace", "object");
        wildcardMap.put("http://dbpedia.org/ontology/foundationPlace", "object");
        wildcardMap.put("http://dbpedia.org/ontology/award", "object");
        wildcardMap.put("http://dbpedia.org/ontology/team", "object");
        wildcardMap.put("http://dbpedia.org/ontology/office", "object");
        wildcardMap.put("http://dbpedia.org/ontology/spouse", "object");
        wildcardMap.put("http://dbpedia.org/ontology/starring", "object");

        wildcardMap.put("http://dbpedia.org/ontology/author", "subject");
        wildcardMap.put("http://dbpedia.org/ontology/subsidiary", "subject");

        nerMap.put("http://dbpedia.org/ontology/birthPlace", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PLACE));
        nerMap.put("http://dbpedia.org/ontology/deathPlace", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PLACE));
        nerMap.put("http://dbpedia.org/ontology/foundationPlace", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PLACE));

        nerMap.put("http://dbpedia.org/ontology/office", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PLACE, 
                                                                NamedEntityTagNormalizer.NAMED_ENTITY_TAG_ORGANIZATION));

        nerMap.put("http://dbpedia.org/ontology/award", null);

        nerMap.put("http://dbpedia.org/ontology/author", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON));
        nerMap.put("http://dbpedia.org/ontology/spouse", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON));
        nerMap.put("http://dbpedia.org/ontology/starring", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON));

        nerMap.put("http://dbpedia.org/ontology/subsidiary", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_ORGANIZATION));
        nerMap.put("http://dbpedia.org/ontology/team", Set.of(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_ORGANIZATION));



        //USE if not using boa patterns -- config setting
        closestPredicateMap = new HashMap<String, List<String>>();
        
        List<String> birthList = Arrays.asList("born", "birth", "hometown");
        List<String> deathList = Arrays.asList("death", "died", "buried");

        List<String> foundationList = Arrays.asList("found", "founded", "headquarter");
        List<String> awardList = Arrays.asList("won", "received", "award");

        List<String> authorList = Arrays.asList("wrote", "author", "writer", "book");
        List<String> teamList = Arrays.asList("player", "played", "team", "member");
        List<String> officeList = Arrays.asList("president", "leader", "prime minister");
        List<String> spouseList = Arrays.asList("spouse", "partner", "wife", "husband", "married");

        List<String> starringList = Arrays.asList("starring", "act", "cast");
        List<String> subsidiaryList = Arrays.asList("subsidiary", "parent", "company");

        closestPredicateMap.put("http://dbpedia.org/ontology/birthPlace", birthList);
        
        
        subjectNerMap.put("http://dbpedia.org/ontology/birthPlace", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON);

        closestPredicateMap.put("http://dbpedia.org/ontology/deathPlace", deathList);
        
        
        subjectNerMap.put("http://dbpedia.org/ontology/deathPlace", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON);

        closestPredicateMap.put("http://dbpedia.org/ontology/foundationPlace", foundationList);
        
        
        // subjectNerMap.put("http://dbpedia.org/ontology/foundationPlace", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_ORGANIZATION);

        closestPredicateMap.put("http://dbpedia.org/ontology/award", awardList);
        
        
        subjectNerMap.put("http://dbpedia.org/ontology/award", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON);

        closestPredicateMap.put("http://dbpedia.org/ontology/author", authorList);
        
        

        closestPredicateMap.put("http://dbpedia.org/ontology/team", teamList);

        closestPredicateMap.put("http://dbpedia.org/ontology/office", officeList);
        
        
        subjectNerMap.put("http://dbpedia.org/ontology/office", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON);

        closestPredicateMap.put("http://dbpedia.org/ontology/spouse", spouseList);
        
        
        subjectNerMap.put("http://dbpedia.org/ontology/spouse", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON);
        
        closestPredicateMap.put("http://dbpedia.org/ontology/starring", starringList);

        closestPredicateMap.put("http://dbpedia.org/ontology/subsidiary", subsidiaryList);
    
    }

    public static List<String> getClosestPredicates(String key) {
        //SET true default
        if(Defacto.DEFACTO_CONFIG.getBooleanSetting("boa", "USE_BOA_PATTERNS")) {
            BoaPatternSearcher.init();
            BoaPatternSearcher bps = new BoaPatternSearcher();
            List<Pattern> patterns = bps.getNaturalLanguageRepresentations(key, 4, "en");

            List<String> stringPatterns = patterns.stream()
                .map(patten -> patten.getNormalized().trim())
                .collect(Collectors.toList());

            return stringPatterns;
        } else {
            return closestPredicateMap.get(key);
        }
    }

    public static String getWildcardType(String key) {
        return wildcardMap.get(key);
    }

    public static Set<String> getWildcardNerTags(String key) {
        return nerMap.get(key);
    }

    public static String getSubjectNerTag(String key) {
        return subjectNerMap.get(key);
    }
}
