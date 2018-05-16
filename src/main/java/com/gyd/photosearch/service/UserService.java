package com.gyd.photosearch.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {

    /**
     * service used to create all users and define roles
     */
    void createUsers();

    /**
     * find user by name and return spring security user details
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    UserDetails loadUserByUsername(String username) throws Exception;
}
