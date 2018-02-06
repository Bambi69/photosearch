package com.gyd.photosearch.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;

public class ElasticsearchRepository {

    @Autowired
    protected Client esClient;

    protected Logger logger = LogManager.getRootLogger();

}
