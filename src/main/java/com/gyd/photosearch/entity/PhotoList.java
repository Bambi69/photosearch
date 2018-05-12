package com.gyd.photosearch.entity;

import java.util.ArrayList;
import java.util.List;

public class PhotoList {

    SimpleFacet faces;
    HierarchicalFacet dates;
    List<Photo> photos = new ArrayList<>();
    Long resultCount;

    /**
     * constructor from list
     * @param photos
     */
    public PhotoList(List<Photo> photos) {
        this.photos = photos;
    }

    /**
     * default constructor
     */
    public PhotoList() {
    }

    public SimpleFacet getFaces() {
        return faces;
    }

    public void setFaces(SimpleFacet faces) {
        this.faces = faces;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public HierarchicalFacet getDates() {
        return dates;
    }

    public void setDates(HierarchicalFacet dates) {
        this.dates = dates;
    }

    public Long getResultCount() {
        return resultCount;
    }

    public void setResultCount(Long resultCount) {
        this.resultCount = resultCount;
    }
}
