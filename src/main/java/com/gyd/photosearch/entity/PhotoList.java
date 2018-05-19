package com.gyd.photosearch.entity;

import java.util.ArrayList;
import java.util.List;

public class PhotoList {

    SimpleFacet faces;
    SimpleFacet camera;
    SimpleFacet types;
    SimpleFacet confidential;
    SimpleFacet indexationName;
    SimpleFacet nbFaces;
    HierarchicalFacet dates;
    List<Photo> photos = new ArrayList<>();
    Long resultCount;
    List<Integer> pages = new ArrayList<>(); // used for pagination

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

    public List<Integer> getPages() {
        return pages;
    }

    public void setPages(List<Integer> pages) {
        this.pages = pages;
    }

    public SimpleFacet getCamera() {
        return camera;
    }

    public void setCamera(SimpleFacet camera) {
        this.camera = camera;
    }

    public SimpleFacet getTypes() {
        return types;
    }

    public void setTypes(SimpleFacet types) {
        this.types = types;
    }

    public SimpleFacet getConfidential() {
        return confidential;
    }

    public void setConfidential(SimpleFacet confidential) {
        this.confidential = confidential;
    }

    public SimpleFacet getIndexationName() {
        return indexationName;
    }

    public void setIndexationName(SimpleFacet indexationName) {
        this.indexationName = indexationName;
    }

    public SimpleFacet getNbFaces() {
        return nbFaces;
    }

    public void setNbFaces(SimpleFacet nbFaces) {
        this.nbFaces = nbFaces;
    }
}
