package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.Photo;
import com.gyd.photosearch.service.PhotoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
@SessionAttributes("photoInSession")
public class PhotoDetailController {

    private Logger logger = LogManager.getRootLogger();

    @Autowired
    private PhotoService photoService;

    @RequestMapping("/displayPhoto")
    public String displayPhoto(
            @ModelAttribute("photoInSession") Photo photoInSession,
            @RequestParam(value="photoId", required=true) String photoId,
            Model model) throws Exception {

        logger.info("displayPhoto is called");

        photoInSession = photoService.findById(photoId);
        model.addAttribute("photoInSession", photoInSession);

        return "photoDetail";
    }

    @RequestMapping("/updateConfidentiality")
    public String updateConfidentiality(
            @ModelAttribute("photoInSession") Photo photoInSession,
            Model model) throws Exception {

        logger.info("updateConfidentiality is called");
        photoInSession = photoService.updateConfidentiality(photoInSession);

        return "photoDetail";
    }

    @ModelAttribute("photoInSession")
    public Photo getPhotoInSession (HttpServletRequest request) {
        return new Photo();
    }
}
