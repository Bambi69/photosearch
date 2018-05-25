package com.gyd.photosearch;

import com.gyd.photosearch.entity.User;
import com.gyd.photosearch.service.UserDetailsServiceImpl;
import com.gyd.photosearch.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private Logger logger = LogManager.getRootLogger();

    @Value("${admin.userName}")
    private String adminUserName;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                //.antMatchers("/", "/home").permitAll()
                    .antMatchers("/dist/**","/vendor/**")
                    .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        // init variables
        boolean error = false;
        List<User> users = null;

        // add userDetailsService for authentication purpose
        auth.userDetailsService(userDetailsService);

        // check if at least one user exists
        try {
            users = userService.findAll();
        } catch (Exception e) {
            logger.error("no user has been created : enable in memory authentication");
            error = true;
        }

        // enable in memory authentication if no user is created
        if (error || users == null || users.size() == 0) {
            auth
                .inMemoryAuthentication()
                .withUser(adminUserName).password("vlmkq@&123").roles("ADMIN");
        }
    }
}