package org.dice.nfactcheck.patterns;

import java.util.*;

import org.aksw.defacto.nlp.ner.NamedEntityTagNormalizer;

public class ClosestPredicate {
    private static Map<String, List<String>> closestPredicateMap;
    private static Map<String, String> wildcardMap;
    private static Map<String, String> nerMap;

    public static void init() {
        closestPredicateMap = new HashMap<String, List<String>>();
        wildcardMap = new HashMap<String, String>();
        nerMap = new HashMap<String, String>();

        
        List<String> birthList = Arrays.asList("born", "birth", "hometown");
        List<String> deathList = Arrays.asList("death", "died", "buried");

        List<String> foundationList = Arrays.asList("found", "founded", "headquarter", "started");
        List<String> awardList = Arrays.asList("awarded", "won", "received", "award");

        List<String> authorList = Arrays.asList("wrote", "author", "authored", "writer");
        List<String> teamList = Arrays.asList("player", "played", "team", "member");
        List<String> officeList = Arrays.asList("leader", "president", "chairman", "owner");
        List<String> spouseList = Arrays.asList("spouse", "partner", "wife", "husband", "married");

        List<String> starringList = Arrays.asList("starring", "acted", "cast");
        List<String> subsidiaryList = Arrays.asList("subsidiary", "parent", "company");



        closestPredicateMap.put("http://dbpedia.org/ontology/birthPlace", birthList);
        wildcardMap.put("http://dbpedia.org/ontology/birthPlace", "object");
        nerMap.put("http://dbpedia.org/ontology/birthPlace", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PLACE);

        closestPredicateMap.put("http://dbpedia.org/ontology/deathPlace", deathList);
        wildcardMap.put("http://dbpedia.org/ontology/deathPlace", "object");
        nerMap.put("http://dbpedia.org/ontology/deathPlace", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PLACE);

        closestPredicateMap.put("http://dbpedia.org/ontology/foundationPlace", foundationList);
        wildcardMap.put("http://dbpedia.org/ontology/foundationPlace", "object");
        nerMap.put("http://dbpedia.org/ontology/foundationPlace", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PLACE);

        closestPredicateMap.put("http://dbpedia.org/ontology/award", awardList);
        wildcardMap.put("http://dbpedia.org/ontology/award", "object");
        nerMap.put("http://dbpedia.org/ontology/award", null);

        closestPredicateMap.put("http://dbpedia.org/ontology/author", authorList);
        wildcardMap.put("http://dbpedia.org/ontology/author", "subject");
        nerMap.put("http://dbpedia.org/ontology/author", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON);

        closestPredicateMap.put("http://dbpedia.org/ontology/team", teamList);
        wildcardMap.put("http://dbpedia.org/ontology/team", "object");
        nerMap.put("http://dbpedia.org/ontology/team", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_ORGANIZATION);

        closestPredicateMap.put("http://dbpedia.org/ontology/office", officeList);
        wildcardMap.put("http://dbpedia.org/ontology/office", "subject");
        nerMap.put("http://dbpedia.org/ontology/office", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_ORGANIZATION);

        closestPredicateMap.put("http://dbpedia.org/ontology/spouse", spouseList);
        wildcardMap.put("http://dbpedia.org/ontology/spouse", "object");
        nerMap.put("http://dbpedia.org/ontology/spouse", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON);
        
        closestPredicateMap.put("http://dbpedia.org/ontology/starring", starringList);
        wildcardMap.put("http://dbpedia.org/ontology/starring", "object");
        nerMap.put("http://dbpedia.org/ontology/starring", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_PERSON);

        closestPredicateMap.put("http://dbpedia.org/ontology/subsidiary", subsidiaryList);
        wildcardMap.put("http://dbpedia.org/ontology/subsidiary", "object");
        nerMap.put("http://dbpedia.org/ontology/subsidiary", NamedEntityTagNormalizer.NAMED_ENTITY_TAG_ORGANIZATION);
    }

    public static Map<String, List<String>> getClosestPredicateMap() {
        return closestPredicateMap;
    }

    public static List<String> getClosestPredicates(String key) {
        return closestPredicateMap.get(key);
    }

    public static String getWildcardType(String key) {
        return wildcardMap.get(key);
    }

    public static String getWildcardNerTag(String key) {
        return nerMap.get(key);
    }
}
