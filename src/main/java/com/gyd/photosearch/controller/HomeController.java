package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.entity.SearchParameters;
import com.gyd.photosearch.service.PhotoSearchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ui.search.nbItemsToDisplay}")
    public Integer nbItemsToDisplayByDefault;

    @Value("${ui.search.nbAdditionalItemsToDisplay}")
    public Integer nbAdditionalItemsToDisplay;

    private Logger logger = LogManager.getRootLogger();

    @Autowired
    private PhotoSearchService photoSearchService;

    /**
     * list all photos without any filter
     * reinit the session
     *
     * @param model
     * @param sessionStatus to be able to reinit the session
     * @return
     */
    @RequestMapping("/")
    public String listAllPhotos(Model model, SessionStatus sessionStatus) {

        logger.info("listAllPhotos is called");

        // reinit user session
        sessionStatus.setComplete();


        try {
            PhotoList photoList = photoSearchService.findByCriteria(new SearchParameters(nbItemsToDisplayByDefault));
            model.addAttribute("photoList", photoList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "home";
    }

    /**
     * called when user select a value facet to filter results
     *
     * @param searchParametersSession
     * @param type
     * @param selectedFacetValue
     * @param model
     * @return
     */
    @RequestMapping("/selectFacetValue")
    public String filterPhotos(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            @RequestParam(value="type", required=true) String type,
            @RequestParam(value="selectedFacetValue", required=true) String selectedFacetValue,
            Model model) {

        logger.info("filterPhotos is called");

        // update user session from selected facet
        searchParametersSession = photoSearchService
                .rebuildSearchParametersFromSelectedFacet(searchParametersSession, type, selectedFacetValue);

        try {
            PhotoList photoList = photoSearchService.findByCriteria(searchParametersSession);
            model.addAttribute("photoList", photoList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        // update model from user session
        setModelFromUserSession(searchParametersSession, model);

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
            SessionStatus sessionStatus) {

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

        try {
            PhotoList photoList = photoSearchService.findByCriteria(searchParametersSession);
            model.addAttribute("photoList", photoList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        // update model from user session
        setModelFromUserSession(searchParametersSession, model);

        return "home";
    }

    @RequestMapping("/displayMore")
    public String displayMore(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            Model model) {

        logger.info("displayMore is called");

        // set search parameters to display more items
        searchParametersSession.setNbItemsToDisplay(searchParametersSession.getNbItemsToDisplay()+nbAdditionalItemsToDisplay);

        try {
            PhotoList photoList = photoSearchService.findByCriteria(searchParametersSession);
            model.addAttribute("photoList", photoList);

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        // update model from user session
        setModelFromUserSession(searchParametersSession, model);

        return "home";
    }

    /**
     * set model from user session
     *
     * @param searchParametersSession
     * @param model
     */
    private void setModelFromUserSession(SearchParameters searchParametersSession, Model model) {
        model.addAttribute("text", searchParametersSession.getTextToSearch());
    }

    @ModelAttribute("searchParametersSession")
    public SearchParameters getSearchParametersSession (HttpServletRequest request) {
        return new SearchParameters(nbItemsToDisplayByDefault);
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
