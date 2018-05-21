package com.gyd.photosearch.controller;

import com.gyd.photosearch.service.IndexationService;
import com.gyd.photosearch.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdministrationController {

    private Logger logger = LogManager.getRootLogger();

    @Autowired
    private IndexationService indexationService;

    @Autowired
    private UserService userService;

    @RequestMapping("/administration")
    public String administration() {
        return "administration";
    }

    @RequestMapping("/indexation")
    public String indexation(Authentication authentication) throws Exception {

        logger.info("indexation controller is called");

        // check user authorizations
        userService.isAdmin(authentication);

        indexationService.indexPhotos();
        return "administration";
    }
}
