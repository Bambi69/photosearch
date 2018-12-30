package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.Photo;
import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.entity.SearchParameters;
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
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.ListIterator;

@Controller
@SessionAttributes("searchParametersSession")
public class HomeController {

    @Value("${admin.userName}")
    private String adminUserName;

    @Value("${ui.facets.face.searchType}")
    private String faceFacetSearchType;

    @Value("${ui.facets.year.searchType}")
    private String yearFacetSearchType;

    @Value("${ui.facets.month.searchType}")
    private String monthFacetSearchType;

    @Value("${ui.facets.camera.searchType}")
    private String cameraFacetSearchType;

    @Value("${ui.facets.type.searchType}")
    private String typeFacetSearchType;

    @Value("${ui.facets.confidential.searchType}")
    private String confidentialFacetSearchType;

    @Value("${ui.facets.indexationName.searchType}")
    private String indexationNameFacetSearchType;

    @Value("${ui.facets.nbFaces.searchType}")
    private String nbFacesFacetSearchType;

    @Value("${ui.search.nbItemsByPage}")
    private Integer nbItemsByPage;

    private Logger logger = LogManager.getRootLogger();

    private PhotoList photoList;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserService userService;

    /**
     * list all photos without any filter
     * reinit the session
     *
     * @param searchParametersSession
     * @param model
     * @param sessionStatus to be able to reinit the session
     * @return
     * @throws Exception
     */
    @RequestMapping("/")
    public String listAllPhotos(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            Model model,
            SessionStatus sessionStatus) throws Exception {

        logger.info("listAllPhotos is called");

        // reinit user session
        sessionStatus.setComplete();

        // reinit search parameters
        searchParametersSession = new SearchParameters(nbItemsByPage);
        model.addAttribute("searchParametersSession", searchParametersSession);

        // search photos and update model
        searchPhotosAndUpdateModel(model, new SearchParameters(nbItemsByPage));

        return "home";
    }

    /**
     * called when user select or unselect a value facet
     *
     * @param searchParametersSession
     * @param type
     * @param selectedFacetValue
     * @param model
     * @return
     */
    @RequestMapping("/filterByFacetValue")
    public String filterByFacetValue(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            @RequestParam(value="type", required=true) String type,
            @RequestParam(value="selectedFacetValue", required=true) String selectedFacetValue,
            @RequestParam(value="action", required=true) String action,
            Model model) throws Exception {

        logger.info("filterByFacetValue is called");

        // update user session from selected facet
        if (action != null && action.compareTo("select") == 0) {
            searchParametersSession = photoService
                    .rebuildSearchParametersFromSelectedFacet(searchParametersSession, type, selectedFacetValue);
        } else {
            searchParametersSession = photoService
                    .rebuildSearchParametersFromUnselectedFacet(searchParametersSession, type, selectedFacetValue);
        }

        // search photos and update model
        searchPhotosAndUpdateModel(model, searchParametersSession);

        return "home";
    }

    /**
     * called when user search by text
     * note that it reinit all search parameters
     *
     * @param searchParametersSession
     * @param text
     * @param sessionToReinit if user search new text, we must reinit the session
     * @param model
     * @param sessionStatus to be able to reinit the session
     * @return
     */
    @RequestMapping("/searchByText")
    public String search(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            @RequestParam(value="text", required=true) String text,
            @RequestParam(value="sessionToReinit", required=false, defaultValue = "true") Boolean sessionToReinit,
            Model model,
            SessionStatus sessionStatus) throws Exception {

        logger.info("search by text is called");

        // reinit the session in case of new search
        if(sessionToReinit) {

            // reinit user session
            sessionStatus.setComplete();

            // redirect to searchByText
            return "redirect:/searchByText?text="+URLEncoder.encode(text, "UTF-8")+"&sessionToReinit=false";
        }

        // set search parameters
        searchParametersSession.setTextToSearch(text);

        // search photos and update model
        searchPhotosAndUpdateModel(model, searchParametersSession);

        return "home";
    }

    /**
     * when user selects a specific page number in pagination component
     *
     * @param searchParametersSession
     * @param pageNumber
     * @param model
     * @return
     */
    @RequestMapping("/switchPage")
    public String switchPage(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            @RequestParam(value="pageNumber", required=false) Integer pageNumber,
            Model model) throws Exception {

        logger.info("switchPage is called");

        // update search parameters and search photos corresponding to this page
        switchToOtherPage(model, searchParametersSession, pageNumber);

        return "home";
    }

    /**
     * switch to other page
     *
     * @param model
     * @param searchParametersSession
     * @param pageNumber
     * @throws Exception
     */
    private void switchToOtherPage(Model model, SearchParameters searchParametersSession, Integer pageNumber) throws Exception {

        // set search parameters to take into account pagination action
        searchParametersSession = photoService.rebuildSearchParametersForSwitchPageAction(
                searchParametersSession, pageNumber);

        // search photos and update model
        searchPhotosAndUpdateModel(model, searchParametersSession);

        return;
    }

    /**
     * when user is on photo detail and wants to go back to search page
     *
     * @param searchParametersSession
     * @param model
     * @return
     */
    @RequestMapping("/goBackToSearch")
    public String goBackToSearch(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            Model model) throws Exception {

        logger.info("goBackToSearch is called");

        // search photos and update model
        searchPhotosAndUpdateModel(model, searchParametersSession);

        return "home";
    }

    @RequestMapping("/nextPhoto")
    public String nextPhoto(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            @RequestParam(value="currentPhotoId", required=false) String currentPhotoId,
            Model model) throws Exception {

        logger.info("nextPhoto is called");

        // iterate on photolist to find next
        Iterator<Photo> itPhotos = photoList.getPhotos().iterator();
        Boolean found = false;
        Photo photo;
        while (itPhotos.hasNext()) {
            photo = itPhotos.next();

            // if photo was found on previous iteration, user is redirected to photo corresponding to current iteration
            if (found) return "redirect:/displayPhoto?photoId="+photo.getId();
            if (photo.getId().compareTo(currentPhotoId) == 0) found = true;
        }

        // if photo was found on last iteration, it means it is the last photo of current page: we must go to next page
        if(found) {

            // firstly, check if active page is not the last one
            if (searchParametersSession.getActivePage() == photoList.getPages().size()) {
                return "redirect:/displayPhoto?photoId="+currentPhotoId;
            }

            // else, switch to next page
            switchToOtherPage(model, searchParametersSession, searchParametersSession.getActivePage()+1);

            // and redirect user to first item of this new page
            return "redirect:/displayPhoto?photoId="+photoList.getPhotos().get(0).getId();
        }

        return "redirect:/displayPhoto?photoId="+currentPhotoId;
    }

    @RequestMapping("/previousPhoto")
    public String previousPhoto(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            @RequestParam(value="currentPhotoId", required=false) String currentPhotoId,
            Model model) throws Exception {

        logger.info("previousPhoto is called");

        // iterate on photolist from last to first
        Boolean found = false;
        Photo photo;
        ListIterator<Photo> listIter = photoList.getPhotos().listIterator(photoList.getPhotos().size());
        while (listIter.hasPrevious()) {
            photo = listIter.previous();

            // if photo was found on previous iteration, user is redirected to photo corresponding to current iteration
            if (found) return "redirect:/displayPhoto?photoId="+photo.getId();
            if (photo.getId().compareTo(currentPhotoId) == 0) found = true;
        }

        // if photo was found on last iteration, it means it is the first photo of current page: we must go to previous page
        if(found) {

            // firstly, check if active page is not the first one
            if (searchParametersSession.getActivePage() == 1) {
                return "redirect:/displayPhoto?photoId="+currentPhotoId;
            }

            // else, switch to previous page
            switchToOtherPage(model, searchParametersSession, searchParametersSession.getActivePage()-1);

            // and redirect user to the last item of this new page
            return "redirect:/displayPhoto?photoId="+photoList.getPhotos().get(photoList.getPhotos().size()-1).getId();
        }

        return "redirect:/displayPhoto?photoId="+currentPhotoId;
    }

    /**
     * search photos which correspond to search parameters
     * add list to the model
     * update model with text to search
     *
     * @param model
     * @param searchParameters
     * @return
     * @throws Exception
     */
    private void searchPhotosAndUpdateModel(Model model, SearchParameters searchParameters) throws Exception {

        // retrieve user's role and search restrictions
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // set user role (admin user is not persisted)
        searchParameters.setSearchRestrictionsToApply(userService.isUserRole(authentication));

        // set search restrictions associated to the user
        if (authentication.getName().compareTo(adminUserName) != 0) {
            searchParameters.setUserAuthorizedFaces(userService.findByUserName(authentication.getName()).getAuthorizedFaces());
        }

        // search photos
        photoList = photoService.findByCriteria(searchParameters);
        model.addAttribute("photoList", photoList);
        model.addAttribute("text", searchParameters.getTextToSearch());
    }

    @ModelAttribute("searchParametersSession")
    public SearchParameters getSearchParametersSession (HttpServletRequest request) {
        return new SearchParameters(nbItemsByPage);
    }

    @ModelAttribute("faceFacetSearchType")
    public String getFaceFacetSearchType (HttpServletRequest request) {
        return faceFacetSearchType;
    }

    @ModelAttribute("yearFacetSearchType")
    public String getYearFacetSearchType (HttpServletRequest request) {
        return yearFacetSearchType;
    }

    @ModelAttribute("monthFacetSearchType")
    public String getMonthFacetSearchType (HttpServletRequest request) {
        return monthFacetSearchType;
    }

    @ModelAttribute("cameraFacetSearchType")
    public String getCameraFacetSearchType(HttpServletRequest request) {
        return cameraFacetSearchType;
    }

    @ModelAttribute("typeFacetSearchType")
    public String getTypeFacetSearchType(HttpServletRequest request) {
        return typeFacetSearchType;
    }

    @ModelAttribute("confidentialFacetSearchType")
    public String getConfidentialFacetSearchType(HttpServletRequest request) {
        return confidentialFacetSearchType;
    }

    @ModelAttribute("indexationNameFacetSearchType")
    public String getIndexationNameFacetSearchType(HttpServletRequest request) {
        return indexationNameFacetSearchType;
    }

    @ModelAttribute("nbFacesFacetSearchType")
    public String getNbFacesFacetSearchType(HttpServletRequest request) {
        return nbFacesFacetSearchType;
    }
}
