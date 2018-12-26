package com.gyd.photosearch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableAsync
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Value("${path.photos.processed}")
    private String processedPhotos;

    @Value("${path.resources.root}")
    private String resourcesRootPath;

    @Value("${path.photos.confidential}")
    private String photosConfidentialPath;

    @Value("${path.confidential.resources.root}")
    private String confidentialResourcesRootPath;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("login");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // add public photos to registry
        registry
                .addResourceHandler(resourcesRootPath + "**")
                .addResourceLocations("file:" + processedPhotos);

        // add confidential photos to registry
        registry
                .addResourceHandler(confidentialResourcesRootPath + "**")
                .addResourceLocations("file:" + photosConfidentialPath);
    }

}
