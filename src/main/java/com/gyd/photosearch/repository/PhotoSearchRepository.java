package com.gyd.photosearch.repository;

import com.gyd.photosearch.exception.TechnicalException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.springframework.stereotype.Repository;

@Repository
public class PhotoSearchRepository extends ElasticsearchRepository {

    /**
     * list all photos from photo index
     * @return list of photos
     */
    public SearchResponse findByCriteria(String faceValue) throws TechnicalException {

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

        if (faceValue != null && faceValue.compareTo("") != 0) {
            request.setQuery(QueryBuilders.termQuery("faces.keyword", faceValue)); // Query
        }

        return request.get();
    }
}
