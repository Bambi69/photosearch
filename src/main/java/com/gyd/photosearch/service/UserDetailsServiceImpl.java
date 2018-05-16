package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.User;
import com.gyd.photosearch.exception.TechnicalException;
import com.gyd.photosearch.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private Logger logger = LogManager.getRootLogger();

    /**
     * for spring security
     *
     * @param username
     * @return
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        User user = null;
        try {
            user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException(
                        "No user found with username: "+ username);
            }
        } catch (TechnicalException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        return  new org.springframework.security.core.userdetails.User
                (user.getUserName(),
                        user.getPassword().toLowerCase(), enabled, accountNonExpired,
                        credentialsNonExpired, accountNonLocked,
                        getAuthorities(user.getRole()));
    }

    /**
     * get spring security authorities from user role
     *
     * @param role
     * @return
     */
    private static List<GrantedAuthority> getAuthorities (String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }
}
