package com.gyd.photosearch.controller;

import com.gyd.photosearch.service.IndexationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * this controller is used to index new photos
 */

@Controller
public class IndexationController {

    private Logger logger = LogManager.getRootLogger();

    @Autowired
    private IndexationService indexationService;

    @RequestMapping("/indexation")
    public String indexation() {
        logger.error("indexation controller is called");
        indexationService.indexPhotos();
        return "greeting";
    }
}
