package com.gyd.photosearch.repository;

import com.gyd.photosearch.entity.Indexation;
import com.gyd.photosearch.exception.TechnicalException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Repository
public class IndexationRepository extends TemplateRepository<Indexation> {

    @Value("${elasticsearch.indexationindex.name}")
    private String indexationIndexName;

    @Value("${elasticsearch.indexationindex.type}")
    private String indexationIndexType;

    @Value("${elasticsearch.indexpattern.indexation.duration}")
    private String columnDuration;
    @Value("${elasticsearch.indexpattern.indexation.status}")
    private String columnStatus;
    @Value("${elasticsearch.indexpattern.indexation.nbFilesToIndex}")
    private String columnNbToIndex;
    @Value("${elasticsearch.indexpattern.indexation.nbFilesInError}")
    private String columnNbInError;
    @Value("${elasticsearch.indexpattern.indexation.nbFilesProcessed}")
    private String columnNbProcessed;
    @Value("${elasticsearch.indexpattern.indexation.name}")
    private String columnName;
    @Value("${elasticsearch.indexpattern.indexation.repository}")
    private String columnRepository;

    public IndexationRepository() {
        setClassType(Indexation.class);
    }

    /**
     * find indexation by id
     *
     * @param id
     * @return indexation coresponding to this id
     * @throws TechnicalException
     */
    public Indexation findById(String id) throws TechnicalException {
        return findById(indexationIndexName, indexationIndexType, id);
    }

    /**
     * find all indexations
     *
     * @return all indexations
     * @throws TechnicalException
     */
    public List<Indexation> findAll() throws TechnicalException {

        logger.info("findAll");

        // query elasticsearch
        SearchRequestBuilder request = esClient.prepareSearch(indexationIndexName)
                .setTypes(indexationIndexType)
                .setExplain(true)
                ;

        return convertSearchResponse(request.get());
    }

    /**
     * find indexations by name
     *
     * @param name
     * @return
     * @throws TechnicalException
     */
    public List<Indexation> findByName(String name) throws TechnicalException {

        logger.info("findByName");

        // build filter to apply
        QueryBuilder qb = termQuery(columnName, name);

        // query elasticsearch
        SearchRequestBuilder request = esClient.prepareSearch(indexationIndexName)
                .setTypes(indexationIndexType)
                .setQuery(qb)
                .setSize(100)
                ;

        return convertSearchResponse(request.get());
    }

    /**
     * find indexations by repository
     *
     * @param repository
     * @return
     * @throws TechnicalException
     */
    public List<Indexation> findByRepository(String repository) throws TechnicalException {

        logger.info("findByRepository");

        // build filter to apply
        QueryBuilder qb = termQuery(columnRepository, repository);

        // query elasticsearch
        SearchRequestBuilder request = esClient.prepareSearch(indexationIndexName)
                .setTypes(indexationIndexType)
                .setQuery(qb)
                .setSize(100)
                ;

        return convertSearchResponse(request.get());
    }

    /**
     * delete indexation corresponding to this id
     *
     * @param id
     */
    public void delete(String id) {
        esClient.prepareDelete(indexationIndexName, indexationIndexType, id).get();
    }

    /**
     * create indexation in indexation index
     *
     * @param indexation
     */
    public String create(Indexation indexation) {
        return esClient.prepareIndex(
                indexationIndexName,
                indexationIndexType,
                indexation.getIndexationName())
                .setSource(indexation.getJson(), XContentType.JSON)
                .get().getId();
    }

    /**
     * update indexation
     *
     * @param indexation
     */
    public void update(Indexation indexation) throws TechnicalException, ExecutionException, InterruptedException, IOException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(indexationIndexName);
        updateRequest.type(indexationIndexType);
        updateRequest.id(indexation.getId());
        updateRequest.doc(jsonBuilder()
                .startObject()
                .field(columnDuration, indexation.getDuration())
                .field(columnNbInError, indexation.getNbFilesInError())
                .field(columnNbProcessed, indexation.getNbFilesProcessed())
                .field(columnNbToIndex, indexation.getNbFilesToIndex())
                .field(columnStatus, indexation.getStatus())
                .endObject());
        esClient.update(updateRequest).get();
    }

    /**
     * delete and recreate ES index to store indexations
     */
    public void deleteIndex() {

        // delete index
        try {
            DeleteIndexResponse deleteResponse = esClient.admin().indices()
                    .delete(new DeleteIndexRequest(indexationIndexName)).actionGet();
            logger.info("index " + indexationIndexName + " successfully deleted");

        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        logger.info("trying to create index...");
        esClient.admin().indices().prepareCreate(indexationIndexName)
                .get();
    }
}
