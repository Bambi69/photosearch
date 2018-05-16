package com.gyd.photosearch.controller;

import com.gyd.photosearch.service.IndexationService;
import com.gyd.photosearch.service.UserServiceImpl;
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
    private UserServiceImpl userService;

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

    @RequestMapping("/createUsers")
    public String createUsers(Authentication authentication) throws Exception {
        logger.info("createUsers controller is called");

        // check user authorizations
        userService.isAdmin(authentication);

        userService.createUsers();

        return "administration";
    }
}
