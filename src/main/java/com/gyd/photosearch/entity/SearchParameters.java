package com.gyd.photosearch.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchParameters {

    String textToSearch;
    Map<String, List<String>> selectedFacetValues;
    Integer nbItemsToDisplay;

    /**
     * default constructor
     * @param NB_ITEMS_TO_DISPLAY_BY_DEFAULT
     */
    public SearchParameters(Integer NB_ITEMS_TO_DISPLAY_BY_DEFAULT) {
        this.textToSearch = null;
        this.selectedFacetValues = new HashMap<>();
        this.nbItemsToDisplay = NB_ITEMS_TO_DISPLAY_BY_DEFAULT;
    }


    public String getTextToSearch() {
        return textToSearch;
    }

    public void setTextToSearch(String textToSearch) {
        this.textToSearch = textToSearch;
    }

    public Map<String, List<String>> getSelectedFacetValues() {
        return selectedFacetValues;
    }

    public void setSelectedFacetValues(Map<String, List<String>> selectedFacetValues) {
        this.selectedFacetValues = selectedFacetValues;
    }

    public Integer getNbItemsToDisplay() {
        return nbItemsToDisplay;
    }

    public void setNbItemsToDisplay(Integer nbItemsToDisplay) {
        this.nbItemsToDisplay = nbItemsToDisplay;
    }
}
