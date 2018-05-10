package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.entity.SearchParameters;
import com.gyd.photosearch.repository.PhotoSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhotoSearchServiceImpl implements PhotoSearchService {

    @Autowired
    private PhotoSearchRepository photoSearchRepository;

    @Override
    public PhotoList findByCriteria(SearchParameters searchParameters) throws Exception {

        // query elasticsearch
        PhotoList result = photoSearchRepository.findByCriteria(searchParameters);

        // return result
        return result;
    }
}