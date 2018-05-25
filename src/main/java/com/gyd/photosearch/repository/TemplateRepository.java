package com.gyd.photosearch.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyd.photosearch.entity.GenericEntity;
import com.gyd.photosearch.exception.TechnicalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TemplateRepository<T extends GenericEntity> {

    @Autowired
    protected Client esClient;

    protected Logger logger = LogManager.getRootLogger();

    private Class<T> classType;

    /**
     * generic find by id
     *
     * @param indexName
     * @param indexType
     * @param id
     * @return result T object
     * @throws TechnicalException
     */
    protected T findById(String indexName, String indexType, String id) throws TechnicalException {
        try {
            GetResponse response = esClient.prepareGet(indexName, indexType, id).get();
            return convertSourceAsStringToBean(response.getId(), response.getSourceAsString());

            // if index does not exist, return null. Else, in memory authentication cannot succeed
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    /**
     * convert elasticsearch search response to list of results
     * @param response elasticsearch search response
     * @return list of typed results
     * @throws TechnicalException
     */
    protected List<T> convertSearchResponse(SearchResponse response) throws TechnicalException {
        SearchHit[] hits = response.getHits().getHits();
        List<T> results = new ArrayList<>();
        for(SearchHit hit : hits){
            results.add(convertSourceAsStringToBean(hit.getId(), hit.getSourceAsString()));
        }
        return results;
    }

    /**
     * convert elasticsearch "sourceAsString" to bean
     *
     * @param id
     * @param sourceAsString
     * @return
     * @throws TechnicalException
     */
    protected T convertSourceAsStringToBean(String id, String sourceAsString) throws TechnicalException {
        T result = null;
        if (sourceAsString != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                result = (T) mapper.readValue(sourceAsString, classType);
                result.setId(id);
            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
                throw new TechnicalException(this.getClass().getName() + " / IOException / " + e.getMessage());
            }
        }
        return result;
    }

    protected void setClassType(Class<T> classType) {
        this.classType = classType;
    }
}
