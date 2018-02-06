package com.gyd.photosearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyd.photosearch.entity.Photo;
import com.gyd.photosearch.exception.TechnicalException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TemplateService<T> {

    private Class< T > classType;

    /**
     * convert elasticsearch search response to list of results
     * @param response elasticsearch search response
     * @return list of typed results
     * @throws TechnicalException
     */
    protected List<T> convertSearchResponse(SearchResponse response) throws TechnicalException {
        ObjectMapper mapper = new ObjectMapper();
        SearchHit[] hits = response.getHits().getHits();
        List<T> results = new ArrayList<>();
        for(SearchHit hit : hits){
            String sourceAsString = hit.getSourceAsString();
            if (sourceAsString != null) {
                try {
                    results.add((T) mapper.readValue(sourceAsString, Photo.class));
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new TechnicalException(this.getClass().getName() + " / IOException / " + e.getMessage());
                }
            }
        }
        return results;
    }
}
