package com.gyd.photosearch.entity;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalFacet {

    String name;
    Long globalCount;
    List<HierarchicalFacetEntry> facetEntries = new ArrayList<>();

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

    public List<HierarchicalFacetEntry> getFacetEntries() {
        return facetEntries;
    }

    public void setFacetEntries(List<HierarchicalFacetEntry> facetEntries) {
        this.facetEntries = facetEntries;
    }
}
