package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.PhotoList;

public interface PhotoSearchService {

    PhotoList findByFilter(String filterType, String filterValue) throws Exception;
}
