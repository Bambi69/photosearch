package com.gyd.photosearch.repository;

import com.gyd.photosearch.entity.*;
import com.gyd.photosearch.exception.TechnicalException;
import com.gyd.photosearch.util.DateUtil;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Repository
public class PhotoRepository extends TemplateRepository<Photo> {

    @Value("${elasticsearch.indexpattern.face}")
    private String faceColumnName;

    @Value("${elasticsearch.indexpattern.confidential}")
    private String confidentialColumnName;

    @Value("${elasticsearch.indexpattern.month}")
    private String monthColumnName;

    @Value("${elasticsearch.indexpattern.year}")
    private String yearColumnName;

    @Value("${elasticsearch.indexpattern.camera}")
    private String cameraColumnName;

    @Value("${elasticsearch.indexpattern.type}")
    private String typeColumnName;

    @Value("${elasticsearch.indexpattern.dateTimeOriginal}")
    private String dateTimeOriginalColumnName;

    @Value("${elasticsearch.indexpattern.indexation}")
    private String indexationNameColumnName;

    @Value("${elasticsearch.indexpattern.nbFaces}")
    private String nbFacesColumnName;

    @Value("${elasticsearch.photoindex.name}")
    private String photoIndexName;

    @Value("${elasticsearch.photoindex.type}")
    private String photoIndexType;

    @Value("${ui.facets.face.name}")
    private String faceFacetName;

    @Value("${ui.facets.datePrise.perMonth.name}")
    private String datePrisePerMonthFacetName;

    @Value("${ui.facets.datePrise.perYear.name}")
    private String datePrisePerYearFacetName;

    @Value("${ui.facets.camera.name}")
    private String cameraFacetName;

    @Value("${ui.facets.type.name}")
    private String typeFacetName;

    @Value("${ui.facets.confidential.name}")
    private String confidentialFacetName;

    @Value("${ui.facets.indexationName.name}")
    private String indexationNameFacetName;

    @Value("${ui.facets.nbFaces.name}")
    private String nbFacesFacetName;

    @Value("${ui.facets.face.searchType}")
    public String faceFacetSearchType;

    @Value("${ui.facets.year.searchType}")
    public String yearFacetSearchType;

    @Value("${ui.facets.month.searchType}")
    public String monthFacetSearchType;

    @Value("${ui.facets.camera.searchType}")
    private String cameraFacetSearchType;

    @Value("${ui.facets.type.searchType}")
    private String typeFacetSearchType;

    @Value("${ui.facets.confidential.searchType}")
    private String confidentialFacetSearchType;

    @Value("${ui.facets.indexationName.searchType}")
    private String indexationNameFacetSearchType;

    @Value("${ui.facets.nbFaces.searchType}")
    private String nbFacesFacetSearchType;

    private static String FACE_AGGREGATION = "01";
    private static String MONTH_AGGREGATION = "02";
    private static String YEAR_AGGREGATION = "03";
    private static String CAMERA_AGGREGATION = "04";
    private static String TYPE_AGGREGATION = "05";
    private static String CONFIDENTIAL_AGGREGATION = "06";
    private static String INDEXATION_AGGREGATION = "07";
    private static String NB_FACES_AGGREGATION = "08";

    public PhotoRepository() {
        setClassType(Photo.class);
    }

    /**
     * list all photos from photo index which correspond to search parameters
     *
     * @param searchParameters
     * @return list of photos
     * @throws TechnicalException
     */
    public PhotoList findByCriteria(SearchParameters searchParameters) throws TechnicalException {

        logger.info("PhotoRepository - findAll");

        // query elasticsearch
        SearchRequestBuilder request = esClient.prepareSearch(photoIndexName)
                .setTypes(photoIndexType)
                //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                //.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))  // Filter
                .addAggregation(
                        AggregationBuilders
                                .terms(FACE_AGGREGATION)
                                .field(faceColumnName)
                                .order(BucketOrder.count(false))
                                .size(15)
                )
                .addAggregation(
                        AggregationBuilders.dateHistogram(MONTH_AGGREGATION)
                                .field(dateTimeOriginalColumnName)
                                .dateHistogramInterval(DateHistogramInterval.MONTH)
                )
                .addAggregation(
                        AggregationBuilders.dateHistogram(YEAR_AGGREGATION)
                                .field(dateTimeOriginalColumnName)
                                .dateHistogramInterval(DateHistogramInterval.YEAR)
                )
                .addAggregation(
                        AggregationBuilders.terms(CAMERA_AGGREGATION)
                                .field(cameraColumnName)
                )
                .addAggregation(
                        AggregationBuilders.terms(TYPE_AGGREGATION)
                                .field(typeColumnName)
                                .order(BucketOrder.key(true))
                )
                .addAggregation(
                        AggregationBuilders.terms(CONFIDENTIAL_AGGREGATION)
                                .field(confidentialColumnName)
                )
                .addAggregation(
                        AggregationBuilders.terms(INDEXATION_AGGREGATION)
                                .field(indexationNameColumnName)
                )
                .addAggregation(
                        AggregationBuilders.terms(NB_FACES_AGGREGATION)
                                .field(nbFacesColumnName)
                )
                // pagination
                .setFrom(searchParameters.getFirstItemId())
                .setSize(searchParameters.getNbItemsByPage())
                // ???
                .setExplain(true)
                ;

        // construct filter
        BoolQueryBuilder boolQuery = buildFilterFromSearchParameters(searchParameters);

        // finally, we add the filter to the request
        request.setQuery(boolQuery);

        // query elasticserach, convert result and return it
        return buildResultFromSearchResponse(request.get());
    }

    /**
     * find photo by id
     *
     * @param id
     * @return photo corresponding to this id
     * @throws TechnicalException
     */
    public Photo findById(String id) throws TechnicalException {
        return findById(photoIndexName, photoIndexType, id);
    }

    /**
     * update photo
     *
     * @param photo
     */
    public void update(Photo photo) throws TechnicalException, ExecutionException, InterruptedException, IOException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(photoIndexName);
        updateRequest.type(photoIndexType);
        updateRequest.id(photo.getId());
        updateRequest.doc(jsonBuilder()
                .startObject()
                .field(confidentialColumnName, photo.getConfidential())
                .endObject());
        esClient.update(updateRequest).get();
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

        // filter by authorized face and by confidential tag
        if (searchParameters.getSearchRestrictionsToApply()) {
            result.must(QueryBuilders.termsQuery(faceColumnName, searchParameters.getUserAuthorizedFaces()));
            result.must(QueryBuilders.termsQuery(confidentialColumnName, false));
        }

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
                    } else if (entry.getKey().compareTo(cameraFacetSearchType)==0) {
                        result.must(QueryBuilders.termQuery(cameraColumnName, facetValue));
                    } else if (entry.getKey().compareTo(typeFacetSearchType)==0) {
                        result.must(QueryBuilders.termQuery(typeColumnName, facetValue));
                    } else if (entry.getKey().compareTo(confidentialFacetSearchType)==0) {
                        result.must(QueryBuilders.termQuery(confidentialColumnName, facetValue));
                    } else if (entry.getKey().compareTo(indexationNameFacetSearchType)==0) {
                        result.must(QueryBuilders.termQuery(indexationNameColumnName, facetValue));
                    } else if (entry.getKey().compareTo(nbFacesFacetSearchType)==0) {
                        result.must(QueryBuilders.termQuery(nbFacesColumnName, facetValue));
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

        // retrieve faces facet from response
        SimpleFacet facesFacet = buildSimpleFacetFromSearchResponse(searchResponse, FACE_AGGREGATION, faceFacetName, String.class);

        // retrieve cameras facet from response
        SimpleFacet camerasFacet = buildSimpleFacetFromSearchResponse(searchResponse, CAMERA_AGGREGATION, cameraFacetName, String.class);

        // retrieve types facet from response
        SimpleFacet typesFacet = buildSimpleFacetFromSearchResponse(searchResponse, TYPE_AGGREGATION, typeFacetName, String.class);

        // retrieve confidential facet from response
        SimpleFacet confidentialFacet = buildSimpleFacetFromSearchResponse(searchResponse, CONFIDENTIAL_AGGREGATION, confidentialFacetName, Boolean.class);

        // retrieve indexation name facet from response
        SimpleFacet indexationNameFacet = buildSimpleFacetFromSearchResponse(searchResponse, INDEXATION_AGGREGATION, indexationNameFacetName, String.class);

        // retrieve nb faces facet from response
        SimpleFacet nbFacesFacet = buildSimpleFacetFromSearchResponse(searchResponse, NB_FACES_AGGREGATION, nbFacesFacetName, Long.class);

        // retrieve months facets from response
        InternalDateHistogram internalDateHistogram = searchResponse.getAggregations().get(MONTH_AGGREGATION);
        HierarchicalFacet datesFacet = new HierarchicalFacet();
        datesFacet.setName(datePrisePerYearFacetName);
        datesFacet.setSubName(datePrisePerMonthFacetName);
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
        if (yearFacetEntry != null && yearFacetEntry.getTitle() != null) {
            datesFacet.getFacetEntries().add(yearFacetEntry);
        }

        // define list of photos
        result.setPhotos(convertSearchResponse(searchResponse));

        // define facets
        result.setFaces(facesFacet);
        result.setDates(datesFacet);
        result.setCamera(camerasFacet);
        result.setTypes(typesFacet);
        result.setConfidential(confidentialFacet);
        result.setIndexationName(indexationNameFacet);
        result.setNbFaces(nbFacesFacet);

        // define count
        result.setResultCount(searchResponse.getHits().getTotalHits());

        return result;
    }

    /**
     * build simple facet (list of value with count) from search response
     * ex:faces, camera, etc.
     *
     * @param searchResponse elastic search response
     * @param aggregationReference reference of aggregation (set before requesting es)
     * @param facetName name of facet to build
     * @return simple facet (list of value with count)
     */
    private SimpleFacet buildSimpleFacetFromSearchResponse(SearchResponse searchResponse, String aggregationReference, String facetName, Class valueClass) {

        SimpleFacet result = new SimpleFacet();
        Terms termsByFace = searchResponse.getAggregations().get(aggregationReference);
        result.setName(facetName);
        result.setGlobalCount(termsByFace.getDocCountError() + termsByFace.getSumOfOtherDocCounts());
        Integer facesFacetCount = 0;
        for (Terms.Bucket entry : termsByFace.getBuckets()) {

            if (valueClass == String.class) {
                result.getFacetEntries().put((String) entry.getKey(), entry.getDocCount());
            } else if (valueClass == Long.class) {
                result.getFacetEntries().put(((Long) entry.getKey()).toString(), entry.getDocCount());
            } else if (valueClass == Boolean.class) {
                Long boolValue = (Long) entry.getKey();
                if (boolValue == 0) {
                    result.getFacetEntries().put(Boolean.FALSE.toString(), entry.getDocCount());
                } else {
                    result.getFacetEntries().put(Boolean.TRUE.toString(), entry.getDocCount());
                }
            }

            facesFacetCount += (int) entry.getDocCount();
        }
        result.setGlobalCount(new Long(facesFacetCount));
        return result;
    }

    /**
     * list all faces from indexed photos
     *
     * @return all identified faces
     */
    public List<String> findAllFaces() {

        logger.info("findAllFaces");

        // query elasticsearch
        SearchRequestBuilder request = esClient.prepareSearch(photoIndexName)
                .setTypes(photoIndexType)
                .addAggregation(
                        AggregationBuilders
                                .terms(FACE_AGGREGATION)
                                .field(faceColumnName)
                                .order(BucketOrder.key(true))
                                .size(10000)
                );

        // retrieve all terms from request results
        Terms termsByFace = request.get().getAggregations().get(FACE_AGGREGATION);

        // build resuls
        List<String> result = new ArrayList<>();
        for (Terms.Bucket entry : termsByFace.getBuckets()) {
            result.add((String) entry.getKey());
        }

        return result;

    }
}
