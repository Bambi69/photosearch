package com.gyd.photosearch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Value("${path.photos.thumbnail.processed}")
    private String thumbnailPhotos;

    @Value("${path.photos.hd.processed}")
    private String hdPhotos;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("login");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/photos/**")
                .addResourceLocations("file:"+thumbnailPhotos)
                .addResourceLocations("file:"+hdPhotos);
    }

}
