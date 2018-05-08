package com.gyd.photosearch.entity;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Photo implements Serializable {

    private String name;
    private String thbName; // thumbnail generated file name
    private String dateTimeOriginal;
    private Integer yearTimeOriginal;
    private Integer monthTimeOriginal;
    private List<String> faces = new ArrayList<String>();
    private List<String> unknownKeywords= new ArrayList<String>();
    private Location location;
    private String cameraModel;
    private Date dateIndexed;
    private Image imageToDisplay;
    private String url;
    private String thbUrl; // thumbnail image url
    private String resolution;
    private byte[] json; // to index photo, we need photo in json format

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

    public Integer getMonthTimeOriginal() {
        return monthTimeOriginal;
    }

    public void setMonthTimeOriginal(Integer monthTimeOriginal) {
        this.monthTimeOriginal = monthTimeOriginal;
    }

    public List<String> getFaces() {
        return faces;
    }

    public void setFaces(List<String> faces) {
        this.faces = faces;
    }

    public List<String> getUnknownKeywords() {
        return unknownKeywords;
    }

    public void setUnknownKeywords(List<String> unknownKeywords) {
        this.unknownKeywords = unknownKeywords;
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

    public Image getImageToDisplay() {
        return imageToDisplay;
    }

    public void setImageToDisplay(Image imageToDisplay) {
        this.imageToDisplay = imageToDisplay;
    }

    public String getUrl() {
        //TODO we must delete the root path
        return "/photos/" + getName();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThbUrl() {
        //TODO we must delete the root path
        return "/photos/" + getThbName();
    }

    public void setThbUrl(String thbUrl) {
        this.thbUrl = thbUrl;
    }

    public byte[] getJson() {
        return json;
    }

    public void setJson(byte[] json) {
        this.json = json;
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