package org.dice.factcheck.nlp.stanford;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

public interface CoreNLPClient {

	public Annotation sentenceAnnotation(String document);
	
	public Annotation corefAnnotation(String document);

	public Annotation openIEAnnotation(String document);

	public CRFClassifier<CoreLabel> getNERClassifier();
}
