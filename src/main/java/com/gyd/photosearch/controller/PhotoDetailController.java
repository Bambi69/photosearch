package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.Photo;
import com.gyd.photosearch.service.PhotoService;
import com.gyd.photosearch.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

@Controller
@SessionAttributes("photoInSession")
public class PhotoDetailController {

    private Logger logger = LogManager.getRootLogger();

    @Value("${admin.userName}")
    private String adminUserName;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserService userService;

    @RequestMapping("/displayPhoto")
    public String displayPhoto(
            @ModelAttribute("photoInSession") Photo photoInSession,
            @RequestParam(value="photoId", required=true) String photoId,
            Model model) throws Exception {

        logger.info("displayPhoto is called");

        // search photo
        photoInSession = photoService.findById(photoId);

        // if this photo is confidential, check user role
        checkUserPermissionsToPhoto(photoInSession);

        // update model
        model.addAttribute("photoInSession", photoInSession);

        return "photoDetail";
    }

    /**
     * check if authenticated user can access to this photo
     *
     * @param photo
     * @throws Exception in case of access is forbidden
     */
    private void checkUserPermissionsToPhoto(Photo photo) throws Exception {

        // retrieve user's role and search restrictions
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // throw exception in case of no access to confidential photos
        if (photo.getConfidential()) {
            userService.isAdminOrModerator(authentication);
        }

        // check access restrictions applied to this user (for user role only)
        if (userService.isUserRole(authentication)) {

            // if no identified faces, user with user role is not allowed
            if (photo.getFaces() == null || photo.getFaces().size() == 0) {
                logger.error("Authenticated user tries to access to forbidden resource. This incident is reported.");
                throw new Exception("Authenticated user tries to access to forbidden resource. This incident is reported.");
            }

            // if no identified face corresponds to authorized face, user with user role is not allowed
            List<String> authorizedFaces = userService.findByUserName(authentication.getName()).getAuthorizedFaces();
            Iterator<String> identifiedFacesIt = photo.getFaces().iterator();
            while (identifiedFacesIt.hasNext()) {
                if (authorizedFaces.contains(identifiedFacesIt.next())) {
                    return;
                }
            }

            // no identified face corresponds to authorized face
            logger.error("Authenticated user tries to access to forbidden resource. This incident is reported.");
            throw new Exception("Authenticated user tries to access to forbidden resource. This incident is reported.");
        }
    }

    @RequestMapping("/updateConfidentiality")
    public String updateConfidentiality(
            @ModelAttribute("photoInSession") Photo photoInSession,
            Authentication authentication,
            Model model) throws Exception {

        logger.info("updateConfidentiality is called");

        // check user role
        userService.isAdminOrModerator(authentication);

        // update photo
        photoInSession = photoService.updateConfidentiality(photoInSession);

        return "photoDetail";
    }

    @ModelAttribute("photoInSession")
    public Photo getPhotoInSession (HttpServletRequest request) {
        return new Photo();
    }
}
