package com.gyd.photosearch.repository;

import com.gyd.photosearch.entity.Photo;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;

@Repository
public class IndexationRepository extends TemplateRepository<Photo> {

    @Value("${elasticsearch.photoindex.name}")
    private String photoIndexName;
    @Value("${elasticsearch.photoindex.type}")
    private String photoIndexType;
    @Value("${deleteIndexIfAlreadyExist}")
    private Boolean deleteIndexIfAlreadyExist;

    public IndexationRepository() {
        setClassType(Photo.class);
    }

    /**
     * index photos
     */
    public void indexPhotos(List<Photo> photos) {

        // firstly, create photo index and delete it if already exists
        if (deleteIndexIfAlreadyExist) {
            createPhotoIndex();
        }

        // browse photos to index
        Iterator<Photo> itPhoto = photos.iterator();
        while (itPhoto.hasNext()) {

            // retrieve current
            Photo photoToBeIndexed = itPhoto.next();

            // index your document
            esClient.prepareIndex(
                    photoIndexName,
                    photoIndexType,
                    photoToBeIndexed.getName())
                    .setSource(photoToBeIndexed.getJson(), XContentType.JSON)
                    .get();
        }
    }

    /**
     * create ES index to store photos
     */
    private void createPhotoIndex() {

        // delete index before starting
        try {
                DeleteIndexResponse deleteResponse = esClient.admin().indices()
                        .delete(new DeleteIndexRequest(photoIndexName)).actionGet();
                logger.info("index " + photoIndexName + " successfully deleted");

        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        String locationType =
                "{\n" +
                        "    \""+photoIndexType+"\": {\n" +
                        "      \"properties\": {\n" +
                        "        \"location\": {\n" +
                        "          \"type\": \"geo_point\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }";

        logger.info("trying to create index...");
        esClient.admin().indices().prepareCreate(photoIndexName)
                .addMapping(photoIndexType,locationType, XContentType.JSON)
                .get();
    }
}
