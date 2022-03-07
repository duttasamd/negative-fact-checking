package org.dice.nfactcheck.ml.feature.fact;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

public class NAbstractFactFeatures {
    public static FastVector attributes = new FastVector();
    
    public static final Attribute DIGIT_COUNT 								= new Attribute("digit_count");
    public static final Attribute COMMA_COUNT 								= new Attribute("comma_count");
    public static final Attribute TOKEN_DISTANCE                    		= new Attribute("token_distance");
    public static final Attribute SMITH_WATERMAN 							= new Attribute("smith_waterman");;
    public static final Attribute CHARACTER_COUNT 							= new Attribute("character_count");;
    public static final Attribute TOTAL_OCCURRENCE                  		= new Attribute("total_occurrence");
    public static final Attribute WORDNET_EXPANSION                 		= new Attribute("wordnet_expansion");
    public static final Attribute PAGE_TITLE_OBJECT                 		= new Attribute("page_title_object");
    public static final Attribute PAGE_TITLE_SUBJECT                		= new Attribute("page_title_subject");
    public static final Attribute END_OF_SENTENCE_DOT               		= new Attribute("end_of_sentence_dot");
    public static final Attribute AVERAGE_TOKEN_LENGHT 						= new Attribute("average_token_length");
    public static final Attribute UPPERCASE_LETTER_COUNT 					= new Attribute("uppercase_letters_count");
    public static final Attribute END_OF_SENTENCE_QUESTION_MARK     		= new Attribute("end_of_sentence_question_mark");
    public static final Attribute END_OF_SENTENCE_EXCLAMATION_MARK  		= new Attribute("end_of_sentence_exclamation_mark");
    public static final Attribute NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS 	= new Attribute("number_of_non_alpha_numeric_characters");

    public static final Attribute IS_SUBJECT_WILDCARD                       = new Attribute("is_subject_wildcard");

    // public static final Attribute DEPENDENCY_SUBJECT_PREDICATE			    = new Attribute("dependency_subject_predicate");
    public static final Attribute DEPENDENCY_SUBJECT_PREDICATE_LEVEL_DISTANCE 
                                                                            = new Attribute("dependency_subject_predicate_level_distance");
    public static final Attribute DEPENDENCY_SUBJECT_PREDICATE_INDEX_DISTANCE 
                                                                            = new Attribute("dependency_subject_predicate_index_distance");                                        
    // public static final Attribute DEPENDENCY_PREDICATE_OBJECT			    = new Attribute("dependency_predicate_object");
    public static final Attribute DEPENDENCY_PREDICATE_OBJECT_LEVEL_DISTANCE 
                                                                            = new Attribute("dependency_predicate_object_level_distance");
    public static final Attribute DEPENDENCY_PREDICATE_OBJECT_INDEX_DISTANCE 
                                                                            = new Attribute("dependency_predicate_object_index_distance");

    public static final Attribute NER_MATCH					                = new Attribute("ner_match");
    public static final Attribute NER_NOT_LIST					            = new Attribute("ner_not_list");
    public static final Attribute NER_MATCH_COUNT					        = new Attribute("ner_match_count");

    public static final Attribute WILDCARD_SCORE					        = new Attribute("wildcard_score");
    public static final Attribute WILDCARD_COUNT					        = new Attribute("wildcard_count");

	public static final Attribute QUERY_SUBOROBJ_SIMILARITY 				= new Attribute("query_suborobj_similarity");

    public static Attribute LANGUAGE                   						= new Attribute("language");
    public static Attribute PROPERTY_NAME                     				= new Attribute("property_name");
    public static Attribute CLASS                                   		= new Attribute("clazz");
    
    static {        
        attributes.addElement(QUERY_SUBOROBJ_SIMILARITY);

        attributes.addElement(COMMA_COUNT);
    	attributes.addElement(DIGIT_COUNT);
        attributes.addElement(AVERAGE_TOKEN_LENGHT);
    	attributes.addElement(UPPERCASE_LETTER_COUNT);
    	attributes.addElement(CHARACTER_COUNT);
    	attributes.addElement(END_OF_SENTENCE_DOT);
        attributes.addElement(END_OF_SENTENCE_QUESTION_MARK);
        attributes.addElement(END_OF_SENTENCE_EXCLAMATION_MARK);
        
        attributes.addElement(PAGE_TITLE_SUBJECT);
        attributes.addElement(PAGE_TITLE_OBJECT);
        
        
        attributes.addElement(NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS);
        attributes.addElement(TOKEN_DISTANCE);
        attributes.addElement(TOTAL_OCCURRENCE);
        attributes.addElement(WORDNET_EXPANSION);

        attributes.addElement(IS_SUBJECT_WILDCARD);

        // attributes.addElement(DEPENDENCY_SUBJECT_PREDICATE);
        attributes.addElement(DEPENDENCY_SUBJECT_PREDICATE_LEVEL_DISTANCE);
        attributes.addElement(DEPENDENCY_SUBJECT_PREDICATE_INDEX_DISTANCE);

        // attributes.addElement(DEPENDENCY_PREDICATE_OBJECT);
        attributes.addElement(DEPENDENCY_PREDICATE_OBJECT_LEVEL_DISTANCE);
        attributes.addElement(DEPENDENCY_PREDICATE_OBJECT_INDEX_DISTANCE);

        attributes.addElement(NER_MATCH);
        attributes.addElement(NER_NOT_LIST);
        attributes.addElement(NER_MATCH_COUNT);

        attributes.addElement(WILDCARD_SCORE);
        attributes.addElement(WILDCARD_COUNT);
        
        FastVector propertyName = new FastVector(10);
        propertyName.addElement("team");
        propertyName.addElement("spouse");
        propertyName.addElement("foundationPlace");
        propertyName.addElement("author");
        propertyName.addElement("award");
        propertyName.addElement("subsidiary");
        propertyName.addElement("leaderName");
        propertyName.addElement("birthPlace");
        propertyName.addElement("deathPlace");
        propertyName.addElement("starring");
        PROPERTY_NAME = new Attribute("property_name", propertyName);
        attributes.addElement(PROPERTY_NAME);
        
        FastVector clazz = new FastVector(2);
        clazz.addElement("acceptable");
        clazz.addElement("unacceptable");
        CLASS = new Attribute("class", clazz);
        attributes.addElement(CLASS);
    }
    
    public static Instances nfactFeatures  = new Instances("nfact_confirmation", attributes, 0);
    
    static {
        
        nfactFeatures.setClass(CLASS);
    }
}
