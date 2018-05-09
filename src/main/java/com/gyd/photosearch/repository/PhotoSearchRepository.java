package com.gyd.photosearch.repository;

import com.gyd.photosearch.entity.SearchParameters;
import com.gyd.photosearch.exception.TechnicalException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Repository
public class PhotoSearchRepository extends ElasticsearchRepository {

    /**
     * list all photos from photo index
     * @return list of photos
     */
    public SearchResponse findByCriteria(SearchParameters searchParameters) throws TechnicalException {

        logger.info("PhotoSearchRepository - findAll");

        // query elasticsearch
        SearchRequestBuilder request = esClient.prepareSearch("photo_index")
                .setTypes("photo")
                //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                //.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))  // Filter
                .addAggregation(
                        AggregationBuilders.terms("by_face")
                                .field("faces.keyword"))
                .addAggregation(
                        AggregationBuilders.dateHistogram("by_month")
                                .field("dateTimeOriginal")
                                .dateHistogramInterval(DateHistogramInterval.MONTH)
                )
                .setFrom(0).setSize(100).setExplain(true)
                ;

        // filter by text
        if (searchParameters != null && searchParameters.getTextToSearch() != null
                && searchParameters.getTextToSearch().compareTo("") != 0) {
            request.setQuery(QueryBuilders.queryStringQuery(searchParameters.getTextToSearch()));
        }

        // filter by face
        if (searchParameters != null && searchParameters.getSelectedFacetValues() != null) {
            for (Map.Entry<String, List<String>> entry : searchParameters.getSelectedFacetValues().entrySet()) {
                Iterator<String> entryIt = entry.getValue().iterator();
                while (entryIt.hasNext()) {
                    String facetValue = entryIt.next();
                    request.setQuery(QueryBuilders.termQuery("faces.keyword", facetValue));
                }
            }
        }

        return request.get();
    }
}
