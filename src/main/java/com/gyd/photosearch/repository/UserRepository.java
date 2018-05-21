package com.gyd.photosearch.repository;

import com.gyd.photosearch.entity.User;
import com.gyd.photosearch.exception.TechnicalException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository extends TemplateRepository<User> {

    @Value("${elasticsearch.userindex.name}")
    private String userIndexName;

    @Value("${elasticsearch.userindex.type}")
    private String userIndexType;

    public UserRepository() {
        setClassType(User.class);
    }

    /**
     * create user in user index
     *
     * @param user
     */
    public void createUser(User user) {
        esClient.prepareIndex(
                userIndexName,
                userIndexType,
                user.getUserName())
                .setSource(user.getJson(), XContentType.JSON)
                .get();
    }

    /**
     * create ES index to store users
     */
    public void createUserIndex() {

        // delete index before starting
        try {
            DeleteIndexResponse deleteResponse = esClient.admin().indices()
                    .delete(new DeleteIndexRequest(userIndexName)).actionGet();
            logger.info("index " + userIndexName + " successfully deleted");

        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        logger.info("trying to create index...");
        esClient.admin().indices().prepareCreate(userIndexName)
                .get();
    }

    /**
     * find user by id
     *
     * @param id
     * @return user corresponding to this id
     * @throws TechnicalException
     */
    public User findById(String id) throws TechnicalException {
        return findById(userIndexName, userIndexType, id);
    }

    /**
     * find all users
     *
     * @return all users
     * @throws TechnicalException
     */
    public List<User> findAll() throws TechnicalException {

        logger.info("PhotoRepository - findAll");

        // query elasticsearch
        SearchRequestBuilder request = esClient.prepareSearch(userIndexName)
                .setTypes(userIndexType)
                .setExplain(true)
                ;

        return convertSearchResponse(request.get());
    }
}
