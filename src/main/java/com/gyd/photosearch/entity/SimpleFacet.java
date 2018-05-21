package com.gyd.photosearch.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleFacet {

    String name;
    Long globalCount;
    Map<String, Long> facetEntries = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGlobalCount() {
        return globalCount;
    }

    public void setGlobalCount(Long globalCount) {
        this.globalCount = globalCount;
    }

    public Map<String, Long> getFacetEntries() {
        return facetEntries;
    }

    public void setFacetEntries(Map<String, Long> facetEntries) {
        this.facetEntries = facetEntries;
    }
}
