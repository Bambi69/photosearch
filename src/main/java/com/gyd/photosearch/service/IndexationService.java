package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.Indexation;
import com.gyd.photosearch.exception.TechnicalException;

import java.util.List;

/**
 * this service can be used to:
 *    - index photos
 */
public interface IndexationService {

    /**
     * service used to index photos from system repository
     */
    void indexPhotos(Indexation indexation) throws Exception;

    /**
     * find indexation by id
     *
     * @param id
     * @return indexation
     * @throws TechnicalException
     */
    Indexation findById(String id) throws TechnicalException;

    /**
     * find all indexations
     *
     * @return all indexations
     * @throws TechnicalException
     */
    List<Indexation> findAll() throws TechnicalException;

    /**
     * create or update indexation
     *
     * @param indexation with data
     */
    void save(Indexation indexation) throws Exception;

    /**
     * delete indexation
     *
     * @param id of indexation to delete
     */
    void delete(String id);

    /**
     * delete indexation index
     */
    void deleteIndex();

    /**
     * delete datas corresponding to one indexation (files and indexed datas)
     *
     * @param id
     */
    void deleteIndexation(String id) throws Exception;
}
