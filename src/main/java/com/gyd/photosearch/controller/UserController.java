package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.User;
import com.gyd.photosearch.service.PhotoService;
import com.gyd.photosearch.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {

    private Logger logger = LogManager.getRootLogger();

    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    private List<User> userList;

    @RequestMapping("/deleteUserIndex")
    public String deleteUserIndex(Authentication authentication) throws Exception {
        logger.info("deleteUserIndex controller is called");

        // check user authorizations
        userService.isAdmin(authentication);

        userService.deleteIndex();

        return "userList";
    }

    @RequestMapping("/listUsers")
    public String listUsers(Authentication authentication, Model model) throws Exception {
        logger.info("listUsers controller is called");

        // check user authorizations
        userService.isAdmin(authentication);

        // update model
        model.addAttribute("userList", userService.findAll());

        return "userList";
    }

    @GetMapping("/editUser")
    public String userForm(
            Authentication authentication,
            @RequestParam(value="id", required=false) String id,
            Model model) throws Exception {

        logger.info("editUser is called");

        // check user authorizations
        userService.isAdmin(authentication);

        // search user
        User userForm = new User();
        if (id != null && id.compareTo("") !=0) {
            userForm = userService.findById(id);
        }

        // update model
        model.addAttribute("userForm", userForm);
        model.addAttribute("roles", userService.getAllRoles());
        model.addAttribute("allFaces", photoService.findAllFaces());

        return "userDetail";
    }

    @PostMapping("/editUser")
    public String userSubmit(
            Authentication authentication,
            @ModelAttribute User userForm) throws Exception {

        logger.info("saveUser is called");

        // check user authorizations
        userService.isAdmin(authentication);

        // save user
        userService.save(userForm);

        return "redirect:/listUsers";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(
            Authentication authentication,
            @RequestParam(value="id", required=false) String id) throws Exception {

        logger.info("deleteUser controller is called");

        // delete user
        userService.delete(id);

        return "redirect:/listUsers";
    }
}
