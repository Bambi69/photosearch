package com.gyd.photosearch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericEntity {

    @JsonIgnore
    private String id;

    @JsonIgnore
    private byte[] json; // for indexation, we need object in json format

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getJson() {
        return json;
    }

    public void setJson(byte[] json) {
        this.json = json;
    }
}
