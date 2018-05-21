package com.gyd.photosearch.controller;

import com.gyd.photosearch.entity.User;
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

    private List<User> userList;

    @RequestMapping("/createUsers")
    public String createUsers(Authentication authentication) throws Exception {
        logger.info("createUsers controller is called");

        // check user authorizations
        userService.isAdmin(authentication);

        userService.createUsers();

        return "userList";
    }

    @RequestMapping("/listUsers")
    public String listUsers(Authentication authentication, Model model) throws Exception {
        logger.info("listUsers controller is called");

        // check user authorizations
        userService.isAdmin(authentication);

        // list all users
        userList = userService.findAll();

        model.addAttribute("userList", userList);

        return "userList";
    }

    @GetMapping("/editUser")
    public String editUser(
            @RequestParam(value="id", required=true) String id,
            Model model) throws Exception {

        logger.info("editUser is called");

        // search user
        User user = userService.findById(id);

        // update model
        model.addAttribute("user", user);
        model.addAttribute("roles", userService.getAllRoles());

        return "userDetail";
    }

    @PostMapping("/editUser")
    public String editUser(
            @ModelAttribute User user) throws Exception {

        logger.info("saveUser is called");

        return "redirect:/listUsers";
    }
}
