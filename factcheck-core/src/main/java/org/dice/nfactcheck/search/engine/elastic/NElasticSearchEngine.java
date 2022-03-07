package org.dice.nfactcheck.search.engine.elastic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.DefaultSearchResult;
import org.aksw.defacto.search.result.SearchResult;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.dice.factcheck.search.engine.elastic.ElasticSearchEngine;
import org.dice.nfactcheck.search.query.NMetaQuery;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

/**
 * Class ElasticSearchEngine provides functionality to query the Elastic search instance hosting the ClueWeb dataset
 *
 * 
 * @author Samrat Dutta<samrat@mail.uni-paderborn.de>
 */
public class NElasticSearchEngine extends ElasticSearchEngine {
    @Override
    public SearchResult query(MetaQuery query, Pattern pattern) {

        try {
            List<WebSite> results = new ArrayList<WebSite>();
            String subject  = query.getSubjectLabel().replace("&", "and");;

            String property = normalizePredicate(query.getPropertyLabel().trim());

            String object   = query.getObjectLabel().replace("&", "and");

            if((subject.contains("(") && subject.contains(")"))
                || (object.contains("(") && object.contains(")"))) {
                subject = subject.replaceAll("\\(.*?\\)","").trim();
                object = object.replaceAll("\\(.*?\\)","").trim();
            }

            NMetaQuery nmq = (NMetaQuery) query;

            String q1 = "\""+subject+" "+property+" "+object+"\"";

            if(nmq.getWildcard() == "object") {
                q1 = "\"" + subject + " " + property + "\"";
            } else if(nmq.getWildcard() == "subject") {
                q1 = "\"" + property + " " + object + "\"";
            }            

            System.out.println(q1 + " size should be 100");

            String jsquery = "{\n" +
            "   \"from\" : 0,\n" +
            "	\"size\" : 100 ,\n" +
            "    \"query\" : {\n" +
            "       \"match_phrase\" : {\n"+
            "           \"Article\" : { \n" +
            "               \"query\" : "+q1+",\n" +
            "               \"slop\" : 10\n" +
            "           } \n"+
            "       } \n"+
            "    } \n"+
            "} \n";


            HttpEntity entity1 = new NStringEntity(jsquery
                    , ContentType.APPLICATION_JSON);


            String index = Defacto.DEFACTO_CONFIG.getStringSetting("elastic", "INDEX");
            Response response = restClientobj.performRequest("POST", "/" + index + "/_search?size=100",Collections.singletonMap("pretty", "true"),entity1);


            String json = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(json, JsonNode.class);
            JsonNode hits = rootNode.get("hits");
            JsonNode hitCount = hits.get("total");
            int docCount = Integer.parseInt(hitCount.asText());

            System.out.println("NElastic Doc Count : " + Integer.toString(docCount));
            
            for(int i=0; i<docCount; i++)
            {
                JsonNode document = hits.get("hits");

                if(document == null) {
                    document = hits.get(i).get("_source");
                } else {
                    document = hits.get("hits").get(i).get("_source");
                }

                JsonNode articleNode = document.get("Article");
                JsonNode articleURLNode = document.get("URL");
                JsonNode articleTitleNode = document.get("Title");
                JsonNode pagerank = document.get("Pagerank");

                if(articleNode != null && articleURLNode != null && articleTitleNode != null && pagerank != null) {
                    String articleText = articleNode.asText();
                    String articleURL = articleURLNode.asText();
                    String articleTitle = articleTitleNode.asText();
                    WebSite website = new WebSite(query, articleURL);
                    website.setTitle(articleTitle);
                    website.setText(articleText);
                    website.setRank(Float.parseFloat(pagerank.asText()));
                    website.setLanguage(query.getLanguage());
                    website.setPredicate(property);
                    results.add(website);
                }
            }
            return new DefaultSearchResult(results, new Long(docCount), query, pattern, false);
        }
        catch (Exception e) {
        //    e.printStackTrace();
            logger.info("Issue with the running Elastic search instance. Please check if the instance is running! "+e.getMessage());
            return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
        }

        finally {
            //Close the connection
            try {

                this.restClientobj.close();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}

