package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.entity.SearchParameters;
import com.gyd.photosearch.repository.PhotoSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PhotoSearchServiceImpl implements PhotoSearchService {

    @Value("${ui.facets.face.searchType}")
    public String faceFacetSearchType;

    @Value("${ui.facets.year.searchType}")
    public String yearFacetSearchType;

    @Value("${ui.facets.month.searchType}")
    public String monthFacetSearchType;

    @Autowired
    private PhotoSearchRepository photoSearchRepository;

    @Override
    public PhotoList findByCriteria(SearchParameters searchParameters) throws Exception {

        // query elasticsearch
        PhotoList result = photoSearchRepository.findByCriteria(searchParameters);

        // return result
        return result;
    }

    @Override
    public SearchParameters rebuildSearchParametersFromSelectedFacet(SearchParameters searchParameters, String type, String selectedFacetValue) {

        // if a month has been selected, we must split value to retrieve year and month
        if (type != null
                && type.compareTo(monthFacetSearchType) == 0
                && selectedFacetValue.split("_").length > 1) {

            // firstly, we apply this method for year value
            searchParameters = rebuildSearchParametersFromSelectedFacet(
                    searchParameters,
                    yearFacetSearchType,
                    selectedFacetValue.split("_")[0]);

            // then, we apply this method for month value
            // note that, at second call, split method length will be 1
            searchParameters = rebuildSearchParametersFromSelectedFacet(
                    searchParameters,
                    monthFacetSearchType,
                    selectedFacetValue.split("_")[1]);

        } else {

            // if there are already filters applied for this facet type
            if (searchParameters.getSelectedFacetValues().containsKey(type)) {
                searchParameters.getSelectedFacetValues().get(type).add(selectedFacetValue);

            // else
            } else {
                List<String> values = new ArrayList<>();
                values.add(selectedFacetValue);
                searchParameters.getSelectedFacetValues().put(type, values);
            }
        }

        return searchParameters;
    }
}