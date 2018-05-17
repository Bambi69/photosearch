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
import java.util.Iterator;
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
    public void createUsers() {

        // create user index
        userRepository.createUserIndex();

        // build user list
        buildUserList();

        // instance a json mapper
        ObjectMapper mapper = new ObjectMapper(); // create once, reuse

        // create all users
        Iterator<User> usersIt = users.iterator();
        while (usersIt.hasNext()) {
            User userToCreate = usersIt.next();

            // generate json (needed to index it in elastic search)
            try {
                userToCreate.setJson(mapper.writeValueAsBytes(userToCreate));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

            // finally create user
            userRepository.createUser(userToCreate);
        }
    }

    @Override
    public void isAdmin(Authentication authentication) throws Exception {
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN))) {
            throw new Exception();
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

    /**
     * build users list
     */
    private void buildUserList() {

        User gael = new User("gaely", "vlmkq@&123", ROLE_ADMIN, null);

        User helene = new User("helened", "vlmkq@&123", ROLE_MODERATOR, null);

        List<String> julienAuthorizedFaces = new ArrayList<>();
        julienAuthorizedFaces.add("Rose DEVELLE");
        julienAuthorizedFaces.add("Julien DEVELLE");
        julienAuthorizedFaces.add("Hubert DEVELLE");
        julienAuthorizedFaces.add("Marie-Laure DEVELLE");
        User julien = new User("juliend", "vlmkq@&123", ROLE_USER, julienAuthorizedFaces);

        List<String> laurianneAuthorizedFaces = new ArrayList<>();
        laurianneAuthorizedFaces.add("Laurianne GAUTHIER");
        User laurianne = new User("laurianneg", "vlmkq@&123", ROLE_USER, laurianneAuthorizedFaces);

        User germain = new User("germainy", "vlmkq@&123", ROLE_USER, null);

        users.add(gael);
        users.add(helene);
        users.add(julien);
        users.add(laurianne);
        users.add(germain);
    }
}
