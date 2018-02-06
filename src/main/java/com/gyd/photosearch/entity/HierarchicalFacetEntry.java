package com.gyd.photosearch.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class HierarchicalFacetEntry {

    String title;
    Long count;
    Map<String, Long> entry = new LinkedHashMap<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Map<String, Long> getEntry() {
        return entry;
    }

    public void setEntry(Map<String, Long> entry) {
        this.entry = entry;
    }
}
