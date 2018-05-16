package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.entity.SearchParameters;
import com.gyd.photosearch.service.PhotoSearchService;
import com.gyd.photosearch.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

@Controller
@SessionAttributes("searchParametersSession")
public class HomeController {

    @Value("${ui.facets.face.searchType}")
    public String faceFacetSearchType;

    @Value("${ui.facets.year.searchType}")
    public String yearFacetSearchType;

    @Value("${ui.facets.month.searchType}")
    public String monthFacetSearchType;

    @Value("${ui.search.nbItemsByPage}")
    public Integer nbItemsByPage;

    private Logger logger = LogManager.getRootLogger();

    private PhotoList photoList;

    @Autowired
    private PhotoSearchService photoSearchService;

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
            searchParametersSession = photoSearchService
                    .rebuildSearchParametersFromSelectedFacet(searchParametersSession, type, selectedFacetValue);
        } else {
            searchParametersSession = photoSearchService
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
            return "redirect:/searchByText?text="+text+"&sessionToReinit=false";
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

        // set search parameters to take into account pagination action
        searchParametersSession = photoSearchService.rebuildSearchParametersForSwitchPageAction(
                searchParametersSession, pageNumber);

        // search photos and update model
        searchPhotosAndUpdateModel(model, searchParametersSession);

        return "home";
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

        // check if user is authenticated
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new Exception("user is not authenticated");
        }

        // set user role
        searchParameters.setSearchRestrictionsToApply(userService.isUserRole(authentication));

        // set search restrictions associated to the user
        searchParameters.setUserAuthorizedFaces(userService.findByUserName(authentication.getName()).getAuthorizedFaces());

        // search photos
        photoList = photoSearchService.findByCriteria(searchParameters);
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
}
