package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.entity.SearchParameters;

public interface PhotoSearchService {

    /**
     * search photos by criteria
     *
     * @param searchParameters includes text to search, facet values for filtering, nb items to display, etc
     * @return list of results corresponding to filters set
     * @throws Exception
     */
    PhotoList findByCriteria(SearchParameters searchParameters) throws Exception;

    /**
     * build search parameters object from selected facet
     *
     * @param searchParametersSession
     * @param type selected facet type
     * @param selectedFacetValue selected facet value
     * @return
     */
    SearchParameters rebuildSearchParametersFromSelectedFacet(SearchParameters searchParametersSession, String type, String selectedFacetValue);

    /**
     * build search parameters after any pagination action
     *
     * @param searchParametersSession
     * @param requestedPageNumber
     * @return
     */
    SearchParameters rebuildSearchParametersForSwitchPageAction(SearchParameters searchParametersSession, Integer requestedPageNumber);

    /**
     * build search parameters object when user delete any filter
     *
     * @param searchParametersSession
     * @param type
     * @param selectedFacetValue
     * @return
     */
    SearchParameters rebuildSearchParametersFromUnselectedFacet(SearchParameters searchParametersSession, String type, String selectedFacetValue);
}
