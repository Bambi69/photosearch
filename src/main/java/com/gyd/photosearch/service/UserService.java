package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.User;
import com.gyd.photosearch.exception.TechnicalException;
import org.springframework.security.core.Authentication;

public interface UserService {

    /**
     * find user by user name
     *
     * @param userName
     * @return user
     */
    User findByUserName(String userName) throws TechnicalException;

    /**
     * service used to create all users with roles and search restrictions
     */
    void createUsers();

    /**
     * check if authentication corresponds to admin role
     *
     * @param authentication
     * @throws Exception in case of authentication does not correspond to admin role
     */
    void isAdmin(Authentication authentication) throws Exception;

    /**
     * check if authentication corresponds to admin or moderator role
     *
     * @param authentication
     * @throws Exception in case of authentication does not correspond to these roles
     */
    void isAdminOrModerator(Authentication authentication) throws Exception;

    /**
     * return user role from authentication information
     *
     * @param authentication
     * @return user role
     */
    String getUserRoleFromAuthentication(Authentication authentication);

    /**
     * check if authentication corresponds to user role
     *
     * @param authentication
     * @return true if user has "user" role
     */
    Boolean isUserRole(Authentication authentication);
}
