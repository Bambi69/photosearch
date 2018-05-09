package com.gyd.photosearch.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchParameters {

    String textToSearch;
    Map<String, List<String>> selectedFacetValues = new HashMap<>();
    Integer nbItemsToDisplay = 30;

    /**
     * constructor by text
     * @param textToSearch
     */
    public SearchParameters(String textToSearch) {
        this.textToSearch = textToSearch;
    }

    /**
     * default constructor
     */
    public SearchParameters() {
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
