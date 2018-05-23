package com.gyd.photosearch.repository;

import com.gyd.photosearch.entity.User;
import com.gyd.photosearch.exception.TechnicalException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Repository
public class UserRepository extends TemplateRepository<User> {

    @Value("${elasticsearch.userindex.name}")
    private String userIndexName;

    @Value("${elasticsearch.userindex.type}")
    private String userIndexType;

    @Value("${elasticsearch.indexpattern.user.userName}")
    private String columnUserName;
    @Value("${elasticsearch.indexpattern.user.password}")
    private String columnPassword;
    @Value("${elasticsearch.indexpattern.user.firstName}")
    private String columnFirstname;
    @Value("${elasticsearch.indexpattern.user.lastName}")
    private String columnLastname;
    @Value("${elasticsearch.indexpattern.user.email}")
    private String columnEmail;
    @Value("${elasticsearch.indexpattern.user.role}")
    private String columnRole;
    @Value("${elasticsearch.indexpattern.user.authorizedFaces}")
    private String columnAuthorizedfaces;

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
     * delete and recreate ES index to store users
     */
    public void deleteIndex() {

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

    /**
     * update user
     *
     * @param user
     */
    public void update(User user) throws TechnicalException, ExecutionException, InterruptedException, IOException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(userIndexName);
        updateRequest.type(userIndexType);
        updateRequest.id(user.getId());
        updateRequest.doc(jsonBuilder()
                .startObject()
                .field(columnUserName, user.getUserName())
                .field(columnAuthorizedfaces, user.getAuthorizedFaces())
                .field(columnEmail, user.getEmail())
                .field(columnFirstname, user.getFirstName())
                .field(columnLastname, user.getLastName())
                .field(columnPassword, user.getPassword())
                .field(columnRole, user.getRole())
                .endObject());
        esClient.update(updateRequest).get();
    }

    /**
     * delete user corresponding to this id
     *
     * @param id
     */
    public void delete(String id) {
        esClient.prepareDelete(userIndexName, userIndexType, id).get();
    }
}
