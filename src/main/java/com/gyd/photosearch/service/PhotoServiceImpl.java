package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.*;
import com.gyd.photosearch.repository.PhotoRepository;
import com.gyd.photosearch.util.DateUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PhotoServiceImpl extends TemplateService<Photo> implements PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Override
    public PhotoList findByFilter(String filterType, String filterValue) throws Exception {

        // init variables
        SearchResponse searchResponse;
        PhotoList result = new PhotoList();

        // query elasticsearch
        if (filterType != null && filterType.compareTo("face") == 0) {
            searchResponse = photoRepository.findByCriteria(filterValue);
        } else {
            searchResponse = photoRepository.findByCriteria(null);
        }

        //////////////////////////////////////////////
        // retrieve faces facets from response
        //////////////////////////////////////////////
        Terms termsByFace = searchResponse.getAggregations().get("by_face");
        SimpleFacet facesFacet = new SimpleFacet();
        facesFacet.setName("Personnes");
        facesFacet.setGlobalCount(termsByFace.getDocCountError() + termsByFace.getSumOfOtherDocCounts());
        Integer facesFacetCount = 0;
        for (Terms.Bucket entry : termsByFace.getBuckets()) {
            facesFacet.getFacetEntries().put((String) entry.getKey(), entry.getDocCount());
            facesFacetCount += (int) entry.getDocCount();
        }

        //////////////////////////////////////////////
        // retrieve histogram facets from response
        //////////////////////////////////////////////
        InternalDateHistogram internalDateHistogram = searchResponse.getAggregations().get("by_month");
        HierarchicalFacet datesFacet = new HierarchicalFacet();
        datesFacet.setName("Dates de prise");
        String previousYear = null;
        HierarchicalFacetEntry yearFacetEntry = new HierarchicalFacetEntry();

        for (InternalDateHistogram.Bucket entry : internalDateHistogram.getBuckets()) {

            // init variables
            Map<String, Long> monthMap = new HashMap<>();

            // retrieve informations from current entry
            Date currentDate = DateUtil.getDateFromDateTime((DateTime) entry.getKey());
            String year = DateUtil.getYearFromDate(currentDate);
            String month = DateUtil.getMonthFromDate(currentDate);

            // if current year does not exist in facet, create entry
            if(previousYear == null || previousYear.compareTo(year) != 0) {
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

        // build result
        result.setPhotos(convertSearchResponse(searchResponse));
        result.setFaces(facesFacet);
        result.setDates(datesFacet);

        // return result
        return result;
    }

}