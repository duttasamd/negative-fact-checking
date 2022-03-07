package org.dice.nfactcheck.proof.extract;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.fact.FactSearcher;
import org.apache.commons.lang3.StringUtils;
import org.dice.nfactcheck.search.query.NMetaQuery;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;


public class PredicateBasedProofExtractor implements FactSearcher {
	public static int exactMatchesCount = 0;

	private static final Logger LOGGER = LoggerFactory.getLogger(PredicateBasedProofExtractor.class);

	/**
	 * 
	 */
	public PredicateBasedProofExtractor() {
	}


	/**
	 *  Extract proof phrases from a website required to confirm fact
	 */

	@Override
	public void generateProofs(Evidence evidence, WebSite website, DefactoModel model, Pattern pattern) {
		// System.out.println("Generate proofs");
		// System.out.println(website.getText());
		// System.out.println();
		String language = "en";
		NEvidence nevidence = (NEvidence) evidence;
		try
		{			
			model.getObjectLabelNoFallBack("en");
			String predicateLabel = pattern.getNormalized().trim();

			NMetaQuery nmq = (NMetaQuery) website.getQuery();
			List<Pattern> patterns = nevidence.getEnBoaPatterns();

			Set<String> predicateLabels = new HashSet<String>();
			Set<String> targetLabels = new HashSet<String>();

			if(nmq.getWildcard() == "subject") {
				targetLabels.add(model.getObjectLabelNoFallBack(language));
				targetLabels.addAll(model.getObjectAltLabels(language));
			} else if(nmq.getWildcard() == "object") {
				targetLabels.add(model.getSubjectLabelNoFallBack(language));
				targetLabels.addAll(model.getSubjectAltLabels(language));
			}

			for (Pattern predicatepattern : patterns) {
				predicateLabels.add(predicatepattern.getNormalized().trim());
			}

			targetLabels.remove(Constants.NO_LABEL);

			List<java.util.regex.Pattern> targetPatterns = new ArrayList<java.util.regex.Pattern>();
			Iterator<String> it = targetLabels.iterator();
			while(it.hasNext())
				targetPatterns.add(java.util.regex.Pattern.compile(it.next().toString(), java.util.regex.Pattern.CASE_INSENSITIVE));

			String normalizedText = website.getText();
			for (java.util.regex.Pattern pattern2 : targetPatterns) {
				normalizedText = pattern2.matcher(normalizedText).replaceAll("exact_targetFound");
			}

			if(nmq.getWildcard() == "object") {
				String subjectLabel = evidence.getModel().getSubjectLabel("en");
				subjectLabel = subjectLabel.replaceAll("\\(.+?\\)", "").trim();

				for (String label : subjectLabel.split(" ")) {
					if(label.length()>2)
						targetLabels.add(label.trim());
				}
			} else if(nmq.getWildcard() == "subject") {
				String objectLabel = evidence.getModel().getObjectLabel("en");
				objectLabel = objectLabel.replaceAll("\\(.+?\\)", "").trim();
				for (String label : objectLabel.split(" ")) {
					if(label.length()>2)
						targetLabels.add(label.trim());
				}
			}

			/**** Create regex Patterns for Subject and Object forms and add to list ****/

			targetPatterns.clear();
			it = targetLabels.iterator();
			while(it.hasNext())
				targetPatterns.add(java.util.regex.Pattern.compile(it.next().toString(), java.util.regex.Pattern.CASE_INSENSITIVE));

			List<java.util.regex.Pattern> predicatePatterns = new ArrayList<java.util.regex.Pattern>();
			Iterator<String> pit = predicateLabels.iterator();
			while(pit.hasNext())
				predicatePatterns.add(java.util.regex.Pattern.compile(pit.next().toString(), java.util.regex.Pattern.CASE_INSENSITIVE));
	
			/**** Normalize website text using pattern replacement and store it in String ****/
			// replace all the surface forms identified with normalized string

			// for (java.util.regex.Pattern pattern2 : targetPatterns) {
			// 	normalizedText = pattern2.matcher(normalizedText).replaceAll("targetFound");
			// }
			
			for (java.util.regex.Pattern pattern2 : predicatePatterns) {
				normalizedText = pattern2.matcher(normalizedText).replaceAll("predicateFound");
			}

			
			/**** Annotate the website text and normalized website text using SNLP sentence split "ssplit" ****/

			Annotation docNormalized = model.corenlpClient.sentenceAnnotation(normalizedText);
			Annotation docOriginal = model.corenlpClient.sentenceAnnotation(website.getText());

			/**** Find proof phrases in both direction i.e., subject followed by object and vice-versa ****/

			
			HashMap<String, Integer> subObjectPhrases = findProofPhrase(docNormalized, docOriginal, "predicateFound", "exact_targetFound");
			subObjectPhrases.putAll(findProofPhrase(docNormalized, docOriginal, "exact_targetFound", "predicateFound"));

			for (String proofPhrase : subObjectPhrases.keySet()) {
				if(proofPhrase.split(" ").length < Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "N_NUMBER_OF_TOKENS_BETWEEN_ENTITIES")) {
					
					boolean isSubjectWildcard = false;
					if(nmq.getWildcard() == "subject") {
						isSubjectWildcard = true;	
					}

					ComplexProof proof = new NComplexProof(evidence.getModel(), targetLabel, predicateLabel, proofPhrase.trim(), proofPhrase.trim(), website, true, isSubjectWildcard);

					evidence.addComplexProof(proof);
				}
			}
		}
		catch (Exception e) {
			LOGGER.info("Exception while extracting proof phrases from web page: "+e.getMessage());
			e.printStackTrace();
		}

	}

	public static void toLowerCase(Set<String> strings)
	{
		String[] stringsArray = strings.toArray(new String[0]);
		for (int i=0; i< stringsArray.length; ++i) {
			stringsArray[i] = stringsArray[i].toLowerCase();
		}
		strings.clear();
		strings.addAll(Arrays.asList(stringsArray));
	}

	public static void toUpperCase(Set<String> strings)
	{
		String[] stringsArray = strings.toArray(new String[0]);
		for (int i=0; i< stringsArray.length; ++i) {
			stringsArray[i] = stringsArray[i].toUpperCase();
		}
		strings.clear();
		strings.addAll(Arrays.asList(stringsArray));
	}


	/**
	 * Takes annotated documents as input and return the proof phrase containing proof string and 
	 * length in number of sentences
	 */

	private HashMap<String, Integer> findProofPhrase(Annotation docNormalized, Annotation docOriginal, String labelFirst, String labelSecond)
	{
		int sentenceCount = 0;
		int count = 0;
		int subjoccur = 0;
		boolean subjectFound = false;
		TreeMap<Integer, Integer> sent = new TreeMap<>();
		List<CoreMap> sentencesNormalized = docNormalized.get(CoreAnnotations.SentencesAnnotation.class);
		List<CoreMap> sentencesOriginal = docOriginal.get(CoreAnnotations.SentencesAnnotation.class);

		// process sentences to check if a sentence or sequence of sentences contain subject and object labels

		for (CoreMap sentence : sentencesNormalized) {

			String sentenceString = sentence.toString();

			// both the labels are found in a single sentence			
			if(sentenceString.contains(labelFirst) && sentenceString.contains(labelSecond))
			{
				subjectFound = false;
				sent.put(count, count);
				count++;
				continue;
			}

			// When only one label is found, mark it as start and look for other label
			if(sentenceString.contains(labelFirst))
			{
				subjectFound = true;
				sentenceCount = 1;
				subjoccur = count;
				count++;
				continue;
			}

			// When one label was already found in previous sentence and second label is not found
			// simply keep track and continue
			if(subjectFound && !(sentenceString.contains(labelSecond)))
			{
				sentenceCount++;
				count++;
				continue;
			}

			// When both labels are found in a sequence 
			// Note that, we currently limit sequence upto 3 sentences
			if(sentenceString.contains(labelSecond) && subjectFound)
			{
				if(sentenceCount<3)
				{
					sent.put(subjoccur, count);
				}
				else
				{
					subjoccur = 0;
				}
				subjectFound = false;
				count++;
				continue;
			}
			count++;
		}

		HashMap<String, Integer> subjectObjectStrOriginal = new HashMap<String, Integer>();

		// we need original sentences, we know the list of sentences with start and end indexes
		subjectObjectStrOriginal = getOriginalSentences(sentencesOriginal, sent);

		return subjectObjectStrOriginal;
	}


	/**
	 * Returns the original proof sentences from website text
	 */

	private HashMap<String, Integer> getOriginalSentences(List<CoreMap> sentencesNormal, TreeMap<Integer, Integer> sent)
	{
		HashMap<String, Integer> subjectObjectStrNormal = new HashMap<String, Integer>();
		for(Entry<Integer, Integer> entry : sent.entrySet()) {
			if(entry.getKey().equals(entry.getValue()))
			{
				if(entry.getKey()<=sentencesNormal.size()-1)
				{
					CoreMap senten = sentencesNormal.get(entry.getKey());
					subjectObjectStrNormal.put(senten.get(CoreAnnotations.TextAnnotation.class),1);
					continue;
				}
			}
			int k=0;
			String temp = "";
			for(int i = entry.getKey(); i<=entry.getValue();i++)
			{	    		  
				if(i<=sentencesNormal.size()-1)
				{
					if(!temp.isEmpty())
						temp = temp+" "+sentencesNormal.get(i).get(CoreAnnotations.TextAnnotation.class);
					else
						temp = temp+sentencesNormal.get(i).get(CoreAnnotations.TextAnnotation.class);
					k++;
				}
			}
			subjectObjectStrNormal.put(temp.trim(), k);
		}
		return subjectObjectStrNormal;
	}
}
