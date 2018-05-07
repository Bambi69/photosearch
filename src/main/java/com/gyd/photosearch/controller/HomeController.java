package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.service.PhotoSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private PhotoSearchService photoSearchService;

    @RequestMapping("/")
    public String listAllPhotos(Model model) {
        try {
            PhotoList photoList = photoSearchService.findByFilter(null, null);
            model.addAttribute("photoList", photoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "home";
    }

    @RequestMapping("/home")
    public String filterPhotos(
            @RequestParam(value="type", required=true) String type,
            @RequestParam(value="filter", required=true) String filter,
            Model model) {
        try {
            PhotoList photoList = photoSearchService.findByFilter(type, filter);
            model.addAttribute("photoList", photoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "home";
    }
}
