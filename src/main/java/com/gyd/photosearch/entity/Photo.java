package com.gyd.photosearch.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Photo extends GenericEntity implements Serializable {

    private String name;
    private String dateTimeOriginal;
    private Integer yearTimeOriginal;
    private String monthTimeOriginal;
    private String season;
    private List<String> faces = new ArrayList<>();
    private Integer nbFaces;
    private Location location;
    private String city;
    private String department;
    private String region;
    private String country;
    private String cameraModel;
    private Date dateIndexed;
    private String resolution;
    private Boolean isConfidential = false;
    private List<String> tags = new ArrayList<>();
    private String indexationName;

    private String pathToHdPhoto;
    private String pathToThbPhoto; // thumbnail image path


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

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Boolean getConfidential() {
        return isConfidential;
    }

    public void setConfidential(Boolean confidential) {
        isConfidential = confidential;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public Integer getNbFaces() {
        return nbFaces;
    }

    public void setNbFaces(Integer nbFaces) {
        this.nbFaces = nbFaces;
    }

    public String getIndexationName() {
        return indexationName;
    }

    public void setIndexationName(String indexationName) {
        this.indexationName = indexationName;
    }

    public String getPathToHdPhoto() {
        return pathToHdPhoto;
    }

    public void setPathToHdPhoto(String pathToHdPhoto) {
        this.pathToHdPhoto = pathToHdPhoto;
    }

    public String getPathToThbPhoto() {
        return pathToThbPhoto;
    }

    public void setPathToThbPhoto(String pathToThbPhoto) {
        this.pathToThbPhoto = pathToThbPhoto;
    }
}