package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.User;
import com.gyd.photosearch.exception.TechnicalException;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

    /**
     * find user by user name
     *
     * @param userName
     * @return user
     */
    User findByUserName(String userName) throws TechnicalException;

    /**
     * find all users
     *
     * @return all users
     * @throws TechnicalException
     */
    List<User> findAll() throws TechnicalException;

    /**
     * service used to delete user index
     */
    void deleteIndex();

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

    /**
     * find user by id
     *
     * @param id
     * @return user
     * @throws TechnicalException
     */
    User findById(String id) throws TechnicalException;

    /**
     * get all roles
     *
     * @return all roles
     */
    List<String> getAllRoles();

    /**
     * create or update user
     *
     * @param user with data
     */
    void save(User user) throws Exception;

    /**
     * delete user
     *
     * @param id
     */
    void delete(String id);
}
