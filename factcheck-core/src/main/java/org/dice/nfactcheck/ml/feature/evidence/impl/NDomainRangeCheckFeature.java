package org.dice.nfactcheck.ml.feature.evidence.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.query.ResultSet;
import org.dice.nfactcheck.evidence.NEvidence;
import org.dice.nfactcheck.ml.feature.evidence.NAbstractEvidenceFeatures;
import org.dice.nfactcheck.patterns.ClosestPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jens Lehmann
 *
 */
public class NDomainRangeCheckFeature extends AbstractEvidenceFeature {

	private static final Logger LOGGER = LoggerFactory.getLogger(NDomainRangeCheckFeature.class);

	public static void printVersion(Class<?> clazz) {
		Package p = clazz.getPackage();
		System.out.printf("%s%n  Title: %s%n  Version: %s%n  Vendor: %s%n  HashCode: %s%n" ,
				clazz.getName(),
				p.getImplementationTitle(),
				p.getImplementationVersion(),
				p.getImplementationVendor(),
				p.hashCode());
	}

	@Override
	public void extractFeature(Evidence evidence) {
	    NEvidence nevidence = (NEvidence) evidence;
	    try {
			double score = 1.0;
	        
	        String propertyURI = evidence.getModel().getPropertyUri();
	        
	        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
	        long timeToLive = 150l * 60l * 60l * 1000l; 
	        
			if(ClosestPredicate.getWildcardType(evidence.getModel().getPropertyUri()).equals("subject")) {
				String objectURI = evidence.getModel().getDBpediaObjectUri();

				boolean rangeViolation = false;
				String queryRan = "SELECT * WHERE { <"+propertyURI+"> rdfs:range ?ran }";
				LOGGER.debug("DR-Feature DOMAIN: " + queryRan);
				ResultSet rs = qef.createQueryExecution(queryRan).execSelect();
				// without a range, there can be no violation, so we just need to check the case in which a range exists
				if(rs.hasNext()) {
					String range = rs.next().get("ran").toString();
					ResultSet rs2 = qef.createQueryExecution("SELECT * { <"+objectURI+"> a ?type . FILTER (?type LIKE 'http://dbpedia.org/ontology/%') }").execSelect();
					// if there is no type, there is no violations
					if(rs2.hasNext()) {
						boolean test = false;
						while(rs2.hasNext()) {
							String type = rs2.next().get("type").asNode().getURI();
							if(type.equals(range)) {
								test = true;
							}
						}
						rangeViolation = !test;
					}
				}  
				if(rangeViolation) {
					score -= 0.5;
				}
			} else {
				String subjectURI = evidence.getModel().getDBpediaSubjectUri();

				boolean domainViolation = false;
				String queryDom = "SELECT * WHERE { <"+propertyURI+"> rdfs:domain ?dom }";
				LOGGER.debug("DR-Feature DOMAIN: " + queryDom);
				ResultSet rs = qef.createQueryExecution(queryDom).execSelect();
				// without a domain, there can be no violation, so we just need to check the case in which a domain exists
				if(rs.hasNext()) {
					String domain = rs.next().get("dom").toString();
					ResultSet rs2 = qef.createQueryExecution("SELECT * { <"+subjectURI+"> a ?type . FILTER (?type LIKE 'http://dbpedia.org/ontology/%') }").execSelect();
					// if there is no type, there is no violations
					if(rs2.hasNext()) {
						boolean test = false;
						while(rs2.hasNext()) {
							String type = rs2.next().get("type").asNode().getURI();
							if(type.equals(domain)) {
								test = true;
							}
						}
						domainViolation = !test;
					}
				}

				if(domainViolation) {
					score -= 0.5;
				}
			}
	        
	        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.DOMAIN_RANGE_CHECK, score);
	    }
	    catch ( Exception e ) {
	        
	        nevidence.getFeatures().setValue(NAbstractEvidenceFeatures.DOMAIN_RANGE_CHECK, 0D);
	        e.printStackTrace();
	    }
	}
}
