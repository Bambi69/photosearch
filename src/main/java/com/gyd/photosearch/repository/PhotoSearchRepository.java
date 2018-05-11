package com.gyd.photosearch.repository;

import com.gyd.photosearch.entity.*;
import com.gyd.photosearch.exception.TechnicalException;
import com.gyd.photosearch.util.DateUtil;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Repository
public class PhotoSearchRepository extends TemplateRepository<Photo> {

    @Value("${elasticsearch.indexpattern.face}")
    private String faceColumnName;

    @Value("${elasticsearch.indexpattern.month}")
    private String monthColumnName;

    @Value("${elasticsearch.indexpattern.year}")
    private String yearColumnName;

    @Value("${elasticsearch.photoindex.name}")
    private String photoIndexName;

    @Value("${elasticsearch.photoindex.type}")
    private String photoIndexType;

    @Value("${elasticsearch.indexpattern.dateTimeOriginal}")
    private String dateTimeOriginalColumnName;

    @Value("${ui.facets.face.name}")
    private String faceFacetName;

    @Value("${ui.facets.datePrise.name}")
    private String datePriseFacetName;

    @Value("${ui.facets.face.searchType}")
    public String faceFacetSearchType;

    @Value("${ui.facets.year.searchType}")
    public String yearFacetSearchType;

    @Value("${ui.facets.month.searchType}")
    public String monthFacetSearchType;

    private static String FACE_AGGREGATION = "01";
    private static String MONTH_AGGREGATION = "02";

    /**
     * list all photos from photo index which correspond to search parameters
     *
     * @param searchParameters
     * @return list of photos
     * @throws TechnicalException
     */
    public PhotoList findByCriteria(SearchParameters searchParameters) throws TechnicalException {

        logger.info("PhotoSearchRepository - findAll");

        // query elasticsearch
        SearchRequestBuilder request = esClient.prepareSearch(photoIndexName)
                .setTypes(photoIndexType)
                //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                //.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))  // Filter
                .addAggregation(
                        AggregationBuilders.terms(FACE_AGGREGATION)
                                .field(faceColumnName))
                .addAggregation(
                        AggregationBuilders.dateHistogram(MONTH_AGGREGATION)
                                .field(dateTimeOriginalColumnName)
                                .dateHistogramInterval(DateHistogramInterval.MONTH)
                )
                .setFrom(0).setSize(searchParameters.getNbItemsToDisplay()).setExplain(true)
                ;

        // construct filter
        BoolQueryBuilder boolQuery = buildFilterFromSearchParameters(searchParameters);

        // finally, we add the filter to the request
        request.setQuery(boolQuery);

        // query elasticserach
        SearchResponse searchResponse = request.get();

        // return it
        return buildResultFromSearchResponse(searchResponse);
    }

    /**
     * build filter from search parameters
     *
     * @param searchParameters
     * @return
     */
    private BoolQueryBuilder buildFilterFromSearchParameters(SearchParameters searchParameters) {

        // init result
        BoolQueryBuilder result = QueryBuilders.boolQuery();

        // filter by text
        if (searchParameters != null && searchParameters.getTextToSearch() != null
                && searchParameters.getTextToSearch().compareTo("") != 0) {
            result.must(QueryBuilders.queryStringQuery(searchParameters.getTextToSearch()));
        }

        // filter by facets
        if (searchParameters != null && searchParameters.getSelectedFacetValues() != null) {
            for (Map.Entry<String, List<String>> entry : searchParameters.getSelectedFacetValues().entrySet()) {
                Iterator<String> entryIt = entry.getValue().iterator();
                while (entryIt.hasNext()) {
                    String facetValue = entryIt.next();

                    if (entry.getKey().compareTo(faceFacetSearchType)==0) {
                        result.must(QueryBuilders.termQuery(faceColumnName, facetValue));
                    } else if (entry.getKey().compareTo(yearFacetSearchType)==0) {
                        result.must(QueryBuilders.termQuery(yearColumnName, facetValue));
                    } else if (entry.getKey().compareTo(monthFacetSearchType)==0) {
                        result.must(QueryBuilders.termQuery(monthColumnName, facetValue));
                    }
                }
            }
        }

        return result;
    }

    /**
     * build photo list from search response
     * it mostly consists to convert elastic facet to hashmap
     *
     * @param searchResponse
     * @return
     */
    private PhotoList buildResultFromSearchResponse(SearchResponse searchResponse) throws TechnicalException {

        // init result
        PhotoList result = new PhotoList();

        // retrieve faces facets from response
        Terms termsByFace = searchResponse.getAggregations().get(FACE_AGGREGATION);
        SimpleFacet facesFacet = new SimpleFacet();
        facesFacet.setName(faceFacetName);
        facesFacet.setGlobalCount(termsByFace.getDocCountError() + termsByFace.getSumOfOtherDocCounts());
        Integer facesFacetCount = 0;
        for (Terms.Bucket entry : termsByFace.getBuckets()) {
            facesFacet.getFacetEntries().put((String) entry.getKey(), entry.getDocCount());
            facesFacetCount += (int) entry.getDocCount();
        }
        facesFacet.setGlobalCount(new Long(facesFacetCount));

        // retrieve histogram facets from response
        InternalDateHistogram internalDateHistogram = searchResponse.getAggregations().get(MONTH_AGGREGATION);
        HierarchicalFacet datesFacet = new HierarchicalFacet();
        datesFacet.setName(datePriseFacetName);
        String previousYear = null;
        HierarchicalFacetEntry yearFacetEntry = new HierarchicalFacetEntry();

        for (InternalDateHistogram.Bucket entry : internalDateHistogram.getBuckets()) {

            if (entry.getDocCount() > 0) {

                // retrieve informations from current entry
                Date currentDate = DateUtil.getDateFromDateTime((DateTime) entry.getKey());
                String year = DateUtil.getYearFromDate(currentDate);
                String month = DateUtil.getMonthFromDate(currentDate);

                // if current year does not exist in facet, create entry
                if (previousYear == null || previousYear.compareTo(year) != 0) {

                    // if previous year is not null, it is at least the second iteration
                    // we must add yearFacetEntry before reinitializing it
                    if (previousYear != null) {
                        datesFacet.getFacetEntries().add(yearFacetEntry);
                    }

                    yearFacetEntry = new HierarchicalFacetEntry();
                    yearFacetEntry.setTitle(year);
                    yearFacetEntry.setCount(entry.getDocCount());
                    yearFacetEntry.getEntry().put(month, entry.getDocCount());
                    previousYear = year;

                    // else, increment count on existing entry
                } else {
                    yearFacetEntry.setCount(yearFacetEntry.getCount() + entry.getDocCount());
                    yearFacetEntry.getEntry().put(month, entry.getDocCount());
                }
            }
        }

        // at the end, we must add yearFacetEntry
        if (yearFacetEntry != null) {
            datesFacet.getFacetEntries().add(yearFacetEntry);
        }

        // build result
        result.setPhotos(convertSearchResponse(searchResponse));
        result.setFaces(facesFacet);
        result.setDates(datesFacet);

        // return it
        return result;
    }
}
