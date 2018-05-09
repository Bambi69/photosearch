package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.entity.SearchParameters;
import com.gyd.photosearch.service.PhotoSearchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("searchParametersSession")
public class HomeController {

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
            PhotoList photoList = photoSearchService.findByCriteria(null);
            model.addAttribute("photoList", photoList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "home";
    }

    /**
     * called when user select a value facet to filter results
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

        // update user session
        Map<String, List<String>> selectedFacetValues = searchParametersSession.getSelectedFacetValues();
        if (selectedFacetValues.containsKey(type)) {
            selectedFacetValues.get(type).add(selectedFacetValue);
        } else {
            List<String> values = new ArrayList<>();
            values.add(selectedFacetValue);
            selectedFacetValues.put(type, values);
        }

        try {
            PhotoList photoList = photoSearchService.findByCriteria(searchParametersSession);
            model.addAttribute("photoList", photoList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "home";
    }

    /**
     * called when user search by text
     * note that it reinit all search parameters
     *
     * @param searchParametersSession
     * @param textToSearch
     * @param model
     * @return
     */
    @RequestMapping("/searchByText")
    public String search(
            @ModelAttribute("searchParametersSession") SearchParameters searchParametersSession,
            @RequestParam(value="textToSearch", required=true) String textToSearch,
            Model model) {

        logger.info("search by text is called");

        // reinit search parameters
        searchParametersSession = new SearchParameters(textToSearch);

        try {
            PhotoList photoList = photoSearchService.findByCriteria(searchParametersSession);
            model.addAttribute("photoList", photoList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "home";
    }

    @ModelAttribute("searchParametersSession")
    public SearchParameters getSearchParametersSession (HttpServletRequest request) {
        return new SearchParameters();
    }
}
