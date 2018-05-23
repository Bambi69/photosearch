package com.gyd.photosearch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyd.photosearch.entity.User;
import com.gyd.photosearch.exception.TechnicalException;
import com.gyd.photosearch.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private Logger logger = LogManager.getRootLogger();

    private List<User> users = new ArrayList<>();

    private static String ROLE_ADMIN = "ROLE_ADMIN";
    private static String ROLE_MODERATOR = "ROLE_MODERATOR";
    private static String ROLE_USER = "ROLE_USER";

    @Override
    public User findByUserName(String userName) throws TechnicalException {
        return userRepository.findById(userName);
    }

    @Override
    public List<User> findAll() throws TechnicalException {
        return userRepository.findAll();
    }

    @Override
    public void deleteIndex() {

        // delete user index
        userRepository.deleteIndex();
    }

    @Override
    public void isAdmin(Authentication authentication) throws Exception {
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN))) {
            logger.error("Authenticated user tries to access to forbidden resource. This incident is reported.");
            throw new Exception("Authenticated user tries to access to forbidden resource. This incident is reported.");
        }
    }

    @Override
    public void isAdminOrModerator(Authentication authentication) throws Exception {
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN))
                && !authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_MODERATOR))) {
            logger.error("Authenticated user tries to access to forbidden resource. This incident is reported.");
            throw new Exception("Authenticated user tries to access to forbidden resource. This incident is reported.");
        }
    }

    @Override
    public String getUserRoleFromAuthentication(Authentication authentication) {
        if(authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN))) {
            return ROLE_ADMIN;
        }
        if(authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_MODERATOR))) {
            return ROLE_MODERATOR;
        }
        return ROLE_USER;
    }

    @Override
    public Boolean isUserRole(Authentication authentication) {
        if (getUserRoleFromAuthentication(authentication).compareTo(ROLE_USER) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public User findById(String id) throws TechnicalException {
        return userRepository.findById(id);
    }

    @Override
    public List<String> getAllRoles() {
        List<String> roles = new ArrayList<>();
        roles.add(ROLE_ADMIN);
        roles.add(ROLE_MODERATOR);
        roles.add(ROLE_USER);
        return roles;
    }

    @Override
    public void save(User user) throws Exception {

        // if id is provided, update user
        if (user.getId() != null && user.getId().compareTo("")!=0) {
            userRepository.update(user);

        // else create it
        } else {

            // generate json (needed to index it in elastic search)
            try {
                ObjectMapper mapper = new ObjectMapper();
                user.setJson(mapper.writeValueAsBytes(user));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

            // create user
            userRepository.createUser(user);
        }
    }

    @Override
    public void delete(String id) {
        userRepository.delete(id);
    }
}
