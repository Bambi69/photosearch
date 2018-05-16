package com.gyd.photosearch.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchParameters {

    private String textToSearch;
    private Map<String, List<String>> selectedFacetValues;

    // user restrictions
    private Boolean isSearchRestrictionsToApply = true;
    private List<String> userAuthorizedFaces = new ArrayList<>();

    // pagination
    private Integer firstItemId; // start by 0
    private Integer nbItemsByPage;
    private Integer activePage;

    /**
     * default constructor
     * @param nbItemsByPage
     */
    public SearchParameters(Integer nbItemsByPage) {
        this.textToSearch = null;
        this.selectedFacetValues = new HashMap<>();
        this.firstItemId = 0;
        this.nbItemsByPage = nbItemsByPage;
        this.activePage = 1;
        this.userAuthorizedFaces = new ArrayList<>();
        this.isSearchRestrictionsToApply = true;
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

    public Integer getFirstItemId() {
        return firstItemId;
    }

    public void setFirstItemId(Integer firstItemId) {
        this.firstItemId = firstItemId;
    }

    public Integer getActivePage() {
        return activePage;
    }

    public void setActivePage(Integer activePage) {
        this.activePage = activePage;
    }

    public Integer getNbItemsByPage() {
        return nbItemsByPage;
    }

    public void setNbItemsByPage(Integer nbItemsByPage) {
        this.nbItemsByPage = nbItemsByPage;
    }

    public List<String> getUserAuthorizedFaces() {
        return userAuthorizedFaces;
    }

    public void setUserAuthorizedFaces(List<String> userAuthorizedFaces) {
        this.userAuthorizedFaces = userAuthorizedFaces;
    }

    public Boolean getSearchRestrictionsToApply() {
        return isSearchRestrictionsToApply;
    }

    public void setSearchRestrictionsToApply(Boolean searchRestrictionsToApply) {
        isSearchRestrictionsToApply = searchRestrictionsToApply;
    }
}
