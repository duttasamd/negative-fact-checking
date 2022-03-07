package org.dice.factcheck.nlp.stanford.impl;

import java.io.IOException;
import java.util.Properties;

import org.dice.factcheck.nlp.stanford.CoreNLPClient;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CoreNLPLocalClient implements CoreNLPClient {

	// We need two pipelines, one for Sentence splitting entire document
	// Other for applying Coreference on extracted sentences
	private StanfordCoreNLP pipelineSentence;
	private StanfordCoreNLP pipelineCoref;
	private StanfordCoreNLP pipelineOpenIE;

	private final String classifierPath	= "edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz";
	private CRFClassifier<CoreLabel> nerClassifier;

	private static CoreNLPLocalClient coreNLPClient = null;

	private CoreNLPLocalClient() {

		Properties propSentences = new Properties();
		propSentences.put("annotators", "tokenize, ssplit");
		this.pipelineSentence = new StanfordCoreNLP(propSentences);

		Properties propCoreference = new Properties();
		propCoreference.put("tokenize.language", "English");
		propCoreference.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		propCoreference.put("ner.model","edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
		propCoreference.put("ner.applyNumericClassifiers", "false");
		propCoreference.put("ner.useSUTime", "false");
		propCoreference.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		propCoreference.put("coref.algorithm", "statistical");
		propCoreference.put("coref.model", "edu/stanford/nlp/models/coref/statistical/ranking_model.ser.gz");
		propCoreference.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, mention, coref");
		this.pipelineCoref = new StanfordCoreNLP(propCoreference);

		// Properties propOpenIE = new Properties();
		// propOpenIE.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		// propOpenIE.put("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie");
		// this.pipelineOpenIE = new StanfordCoreNLP(propOpenIE);

		try {
			nerClassifier = CRFClassifier.getClassifier(classifierPath);
		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public static CoreNLPLocalClient getCoreNLPClient() {
		if(coreNLPClient == null) {
			coreNLPClient = new CoreNLPLocalClient();
		}

		return coreNLPClient;
	}

	@Override
	public Annotation sentenceAnnotation(String document) {

		Annotation annotatedDoc = new Annotation(document);
		this.pipelineSentence.annotate(annotatedDoc);
		return annotatedDoc;
	}

	@Override
	public Annotation corefAnnotation(String document) {

		Annotation annotatedDoc = new Annotation(document);
		this.pipelineCoref.annotate(annotatedDoc);
		return annotatedDoc;
	}

	@Override
	public Annotation openIEAnnotation(String document) {

		// Annotation annotatedDoc = new Annotation(document);
		// this.pipelineOpenIE.annotate(annotatedDoc);
		// return annotatedDoc;
		return null;
	}

	@Override
	public CRFClassifier<CoreLabel> getNERClassifier() {
		return nerClassifier;
	}

}
