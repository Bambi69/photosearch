package com.gyd.photosearch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Photo extends GenericEntity implements Serializable {

    private String name;
    private String thbName; // thumbnail generated file name
    private String dateTimeOriginal;
    private Integer yearTimeOriginal;
    private String monthTimeOriginal;
    private List<String> faces = new ArrayList<String>();
    private Location location;
    private String cameraModel;
    private Date dateIndexed;
    private String resolution;

    @JsonIgnore
    private String url;

    @JsonIgnore
    private String thbUrl; // thumbnail image url

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTimeOriginal() {
        return dateTimeOriginal;
    }

    public void setDateTimeOriginal(String dateTimeOriginal) {
        this.dateTimeOriginal = dateTimeOriginal;
    }

    public Integer getYearTimeOriginal() {
        return yearTimeOriginal;
    }

    public void setYearTimeOriginal(Integer yearTimeOriginal) {
        this.yearTimeOriginal = yearTimeOriginal;
    }

    public String getMonthTimeOriginal() {
        return monthTimeOriginal;
    }

    public void setMonthTimeOriginal(String monthTimeOriginal) {
        this.monthTimeOriginal = monthTimeOriginal;
    }

    public List<String> getFaces() {
        return faces;
    }

    public void setFaces(List<String> faces) {
        this.faces = faces;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getCameraModel() {
        return cameraModel;
    }

    public void setCameraModel(String cameraModel) {
        this.cameraModel = cameraModel;
    }

    public Date getDateIndexed() {
        return dateIndexed;
    }

    public void setDateIndexed(Date dateIndexed) {
        this.dateIndexed = dateIndexed;
    }

    public String getUrl() {
        //TODO we must delete the root path
        return "/photos/" + getName();
    }

    public String getThbUrl() {
        //TODO we must delete the root path
        return "/photos/" + getThbName();
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getThbName() {
        return thbName;
    }

    public void setThbName(String thbName) {
        this.thbName = thbName;
    }
}