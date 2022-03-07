package org.dice.nfactcheck.ml.feature.evidence;

import org.aksw.defacto.ml.feature.evidence.EvidenceFeature;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

public abstract class NAbstractEvidenceFeatures implements EvidenceFeature {

    public static FastVector attributes;
    
    public static final Attribute PAGE_RANK_MAX                                = new Attribute("page_rank_max");
    public static final Attribute PAGE_RANK_SUM                                = new Attribute("page_rank_sum");
    public static final Attribute TOTAL_HIT_COUNT_FEATURE                      = new Attribute("total_hit_count");
    public static final Attribute TOPIC_COVERAGE_SUM                           = new Attribute("topic_coverage_sum");
    public static final Attribute TOPIC_COVERAGE_MAX                           = new Attribute("topic_coverage_max");
    public static final Attribute TOPIC_MAJORITY_WEB_SUM                       = new Attribute("topic_majority_web_sum");
    public static final Attribute TOPIC_MAJORITY_WEB_MAX                       = new Attribute("topic_majority_web_max");
    public static final Attribute TOPIC_MAJORITY_SEARCH_RESULT_SUM             = new Attribute("topic_majority_search_sum");
    public static final Attribute TOPIC_MAJORITY_SEARCH_RESULT_MAX             = new Attribute("topic_majority_search_max");
    public static final Attribute NUMBER_OF_PROOFS                             = new Attribute("number_of_proofs");
    public static final Attribute NUMBER_OF_ACCEPTABLE_PROOFS                  = new Attribute("number_of_acceptable_proofs");
    public static final Attribute TOTAL_ACCEPTABLE_PROOFS_SCORE                = new Attribute("total_acceptable_proofs_score");
    public static final Attribute IS_SUBJECT_WILDCARD                          = new Attribute("is_subject_wildcard");
    
    
    public static final Attribute DOMAIN_RANGE_CHECK                           = new Attribute("domain_range_check");
    // public static final Attribute GOODNESS 									   = new Attribute("goodness");

    public static final Attribute WILDCARD_MATCH 					           = new Attribute("wildcard_match");
    public static final Attribute WILDCARD_MATCH_SCORE						   = new Attribute("wildcard_match_score");
    public static final Attribute WILDCARD_PERFECT_MATCH 					   = new Attribute("wildcard_perfect_match");

    public static final Attribute WILDCARD_MATCH_BEST_SW_SCORE                 = new Attribute("wildcard_best_sw_score");
    public static final Attribute WILDCARD_MATCH_NORMALIZED_SW_SCORE            = new Attribute("wildcard_normalized_sw_score");

    public static final Attribute WILDCARD_MATCH_BEST_LEV_SCORE                = new Attribute("wildcard_best_lev_score");
    public static final Attribute WILDCARD_MATCH_NORMALIZED_LEV_SCORE          = new Attribute("wildcard_normalized_lev_score");

    public static final Attribute FACTCHECK_SCORE                              = new Attribute("factcheck_score");

    
    public static Attribute PROPERTY_NAME                     			       = new Attribute("property_name");  
    public static Attribute CLASS                                              = new Attribute("clazz");
    public static Instances provenance;

    static {

    	createInstances();
    }
    
    public static void createInstances(){
    	
    	attributes = new FastVector();
        attributes.addElement(PAGE_RANK_MAX);
        attributes.addElement(PAGE_RANK_SUM);
        attributes.addElement(TOTAL_HIT_COUNT_FEATURE);
        attributes.addElement(TOPIC_COVERAGE_SUM);
        attributes.addElement(TOPIC_COVERAGE_MAX);
        attributes.addElement(TOPIC_MAJORITY_WEB_SUM);
        attributes.addElement(TOPIC_MAJORITY_WEB_MAX);
        attributes.addElement(TOPIC_MAJORITY_SEARCH_RESULT_SUM);
        attributes.addElement(TOPIC_MAJORITY_SEARCH_RESULT_MAX);
        attributes.addElement(NUMBER_OF_PROOFS);
        attributes.addElement(NUMBER_OF_ACCEPTABLE_PROOFS);
        attributes.addElement(TOTAL_ACCEPTABLE_PROOFS_SCORE);
        attributes.addElement(IS_SUBJECT_WILDCARD);
        attributes.addElement(DOMAIN_RANGE_CHECK);

        attributes.addElement(WILDCARD_MATCH);
        attributes.addElement(WILDCARD_MATCH_SCORE);
        attributes.addElement(WILDCARD_PERFECT_MATCH);

        attributes.addElement(WILDCARD_MATCH_BEST_LEV_SCORE);
        attributes.addElement(WILDCARD_MATCH_NORMALIZED_LEV_SCORE);

        attributes.addElement(WILDCARD_MATCH_BEST_SW_SCORE);
        attributes.addElement(WILDCARD_MATCH_NORMALIZED_SW_SCORE);

        attributes.addElement(FACTCHECK_SCORE);
        
        FastVector propertyName = new FastVector(2);
        propertyName.addElement("team");
        propertyName.addElement("spouse");
        propertyName.addElement("foundationPlace");
        propertyName.addElement("author");
        propertyName.addElement("award");
        propertyName.addElement("subsidiary");
        propertyName.addElement("office");
        propertyName.addElement("birthPlace");
        propertyName.addElement("deathPlace");
        propertyName.addElement("starring");
        PROPERTY_NAME = new Attribute("property_name", propertyName);
        attributes.addElement(PROPERTY_NAME);
        
        FastVector clazz = new FastVector(2);
        clazz.addElement("true");
        clazz.addElement("false");
        CLASS = new Attribute("class", clazz);
        attributes.addElement(CLASS);
    	
    	provenance = new Instances("nfactcheck", attributes, 0);
        provenance.setClass(CLASS);
    }
}
