package com.gyd.photosearch.entity;

import java.io.Serializable;

public class Indexation extends GenericEntity implements Serializable {

    private String indexationName;
    private String repositoryName;
    private String description;
    private String photoTag;
    private String date;
    private Integer duration;
    private String status;
    private Integer nbFilesToIndex;
    private Integer nbFilesInError;
    private Integer nbFilesProcessed;

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getPhotoTag() {
        return photoTag;
    }

    public void setPhotoTag(String photoTag) {
        this.photoTag = photoTag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getNbFilesToIndex() {
        return nbFilesToIndex;
    }

    public void setNbFilesToIndex(Integer nbFilesToIndex) {
        this.nbFilesToIndex = nbFilesToIndex;
    }

    public Integer getNbFilesInError() {
        return nbFilesInError;
    }

    public void setNbFilesInError(Integer nbFilesInError) {
        this.nbFilesInError = nbFilesInError;
    }

    public Integer getNbFilesProcessed() {
        return nbFilesProcessed;
    }

    public void setNbFilesProcessed(Integer nbFilesProcessed) {
        this.nbFilesProcessed = nbFilesProcessed;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getIndexationName() {
        return indexationName;
    }

    public void setIndexationName(String indexationName) {
        this.indexationName = indexationName;
    }
}
