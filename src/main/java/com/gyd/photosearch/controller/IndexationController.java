package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.Indexation;
import com.gyd.photosearch.service.IndexationService;
import com.gyd.photosearch.service.PhotoService;
import com.gyd.photosearch.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class IndexationController {

    private Logger logger = LogManager.getRootLogger();

    @Autowired
    private IndexationService indexationService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserService userService;

    @RequestMapping("/listIndexations")
    public String listIndexations(Authentication authentication, Model model) throws Exception {

        logger.info("listIndexations controller is called");

        // check user authorizations
        userService.isAdmin(authentication);

        // update model
        model.addAttribute("indexationList", indexationService.findAll());

        return "indexationList";
    }

    @GetMapping("/editIndexation")
    public String indexationForm(
            Authentication authentication,
            @RequestParam(value="id", required=false) String id,
            Model model) throws Exception {

        logger.info("indexationForm is called");

        // check user authorizations
        userService.isAdmin(authentication);

        // search indexation
        Indexation indexationForm = new Indexation();
        if (id != null && id.compareTo("") !=0) {
            indexationForm = indexationService.findById(id);
        }

        // update model
        model.addAttribute("indexationForm", indexationForm);

        return "indexationDetail";
    }

    @PostMapping("/editIndexation")
    public String indexationSubmit(
            Authentication authentication,
            @ModelAttribute Indexation indexationForm) throws Exception {

        logger.info("indexationSubmit is called");

        // check user authorizations
        userService.isAdmin(authentication);

        // save user
        indexationService.indexPhotos(indexationForm);

        return "redirect:/listIndexations";
    }

    @RequestMapping("/deleteIndexationAndPhotoIndex")
    public String deleteIndexationAndPhotoIndex(Authentication authentication) throws Exception {
        logger.info("deleteIndexationIndex controller is called");

        // check user authorizations
        userService.isAdmin(authentication);

        indexationService.deleteIndex();
        photoService.deleteIndex();

        return "indexationList";
    }
}
