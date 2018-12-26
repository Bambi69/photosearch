package com.gyd.photosearch.service;

import com.gyd.photosearch.entity.Photo;
import com.gyd.photosearch.entity.PhotoList;
import com.gyd.photosearch.entity.SearchParameters;
import com.gyd.photosearch.exception.TechnicalException;
import com.gyd.photosearch.repository.PhotoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhotoServiceImpl implements PhotoService {

    private Logger logger = LogManager.getRootLogger();

    @Value("${ui.search.nbItemsByPage}")
    private Integer nbItemsByPage;

    @Value("${ui.facets.year.searchType}")
    private String yearFacetSearchType;

    @Value("${ui.facets.month.searchType}")
    private String monthFacetSearchType;

    @Autowired
    private PhotoRepository photoRepository;

    @Value("${path.photos.confidential}")
    private String photosConfidentialPath;

    @Value("${path.resources.root}")
    private String resourcesRootPath;

    @Value("${path.confidential.resources.root}")
    private String resourcesConfidentialRootPath;

    @Value("${path.photos.processed}")
    private String photosProcessedPath;

    @Override
    public PhotoList findByCriteria(SearchParameters searchParameters) throws Exception {

        // for user with "user" role
        if (searchParameters.getSearchRestrictionsToApply()) {

            // if no authorized faces, return exception (configuration issue)
            if (searchParameters.getUserAuthorizedFaces() == null || searchParameters.getUserAuthorizedFaces().size() == 0) {
                throw new Exception("No search restrictions defined by administrator. Please contact administrator.");
            }
        }

        // query elasticsearch
        PhotoList photoList = photoRepository.findByCriteria(searchParameters);

        // update page list in search parameters
        List<Integer> pageList = new ArrayList<>();

        // calculate nb of page
        Integer nbOfPage = Math.floorDiv(photoList.getResultCount().intValue(), nbItemsByPage);
        if (nbOfPage * nbItemsByPage != photoList.getResultCount()) { // use modulo to check if rest != 0
            nbOfPage++;
        }

        // build page list
        for (int i = 0; i < nbOfPage; i++) {
            pageList.add(i+1);
        }
        photoList.setPages(pageList);

        // return result
        return photoList;
    }

    @Override
    public SearchParameters rebuildSearchParametersFromSelectedFacet(SearchParameters searchParameters, String type, String selectedFacetValue) {

        // if a month has been selected, we must split value to retrieve year and month
        if (type != null
                && type.compareTo(monthFacetSearchType) == 0
                && selectedFacetValue.split("_").length > 1) {

            // firstly, we apply this method for year value
            searchParameters = rebuildSearchParametersFromSelectedFacet(
                    searchParameters,
                    yearFacetSearchType,
                    selectedFacetValue.split("_")[0]);

            // then, we apply this method for month value
            // /!\ NOTE THAT, at second call, split method length will be 1 /!\
            searchParameters = rebuildSearchParametersFromSelectedFacet(
                    searchParameters,
                    monthFacetSearchType,
                    selectedFacetValue.split("_")[1]);

        } else {

            // if there are already filters applied for this facet type
            if (searchParameters.getSelectedFacetValues().containsKey(type)) {

                // check if this value is not already added
                if(!searchParameters.getSelectedFacetValues().get(type).contains(selectedFacetValue)){
                    searchParameters.getSelectedFacetValues().get(type).add(selectedFacetValue);
                }

            // else
            } else {
                List<String> values = new ArrayList<>();
                values.add(selectedFacetValue);
                searchParameters.getSelectedFacetValues().put(type, values);
            }
        }

        // reinit pagination parameters
        searchParameters = reinitPaginationParameters(searchParameters);

        return searchParameters;
    }

    @Override
    public SearchParameters rebuildSearchParametersFromUnselectedFacet(SearchParameters searchParameters, String type, String selectedFacetValue) {
        if (searchParameters.getSelectedFacetValues().get(type) != null) {
            searchParameters.getSelectedFacetValues().get(type).remove(selectedFacetValue);
            if (searchParameters.getSelectedFacetValues().get(type).size() == 0) {
                searchParameters.getSelectedFacetValues().remove(type);
            }
        }

        // reinit pagination parameters
        searchParameters = reinitPaginationParameters(searchParameters);

        return searchParameters;
    }

    @Override
    public Photo findById(String id) throws TechnicalException {
        return photoRepository.findById(id);
    }

    @Override
    public Photo updateConfidentiality(Photo photo) throws Exception {

        // if photo is confidential, it means photo must become public
        if (photo.getConfidential()) {

            try {
                // move hd file to public folder
                moveFileToPublicFolderFromRelativePath(photo.getPathToHdPhoto());

                // move thumbnail file to public folder
                moveFileToPublicFolderFromRelativePath(photo.getPathToThbPhoto());

                // change photo relative paths
                photo.setPathToHdPhoto(photo.getPathToHdPhoto().replace(resourcesConfidentialRootPath, resourcesRootPath));
                photo.setPathToThbPhoto(photo.getPathToThbPhoto().replace(resourcesConfidentialRootPath, resourcesRootPath));

            } catch (IOException e) {

                // catch this error and continue to manage previous version of this application (old confidential photos are located in public folder)
                logger.error(e.getMessage());
            }

        } else {

            // move hd file to confidential folder
            moveFileToConfidentialFolderFromRelativePath(photo.getPathToHdPhoto());

            // move thumbnail file to confidential folder
            moveFileToConfidentialFolderFromRelativePath(photo.getPathToThbPhoto());

            // change photo relative path
            photo.setPathToHdPhoto(photo.getPathToHdPhoto().replace(resourcesRootPath, resourcesConfidentialRootPath));
            photo.setPathToThbPhoto(photo.getPathToThbPhoto().replace(resourcesRootPath, resourcesConfidentialRootPath));
        }

        photo.setConfidential(!photo.getConfidential());
        photoRepository.update(photo);

        return photo;
    }

    @Override
    public List<String> findAllFaces() {
        return photoRepository.findAllFaces();
    }

    @Override
    public void deleteIndex() {

        // delete photo index
        photoRepository.reinitializeIndex();
    }

    @Override
    public SearchParameters rebuildSearchParametersForSwitchPageAction(
            SearchParameters searchParameters, Integer requestedPageNumber) {

        searchParameters.setFirstItemId((requestedPageNumber-1) * nbItemsByPage);
        searchParameters.setActivePage(requestedPageNumber);

        return searchParameters;
    }

    /**
     * reinitialize pagination parameters
     *
     * @param searchParameters
     * @return
     */
    private SearchParameters reinitPaginationParameters(SearchParameters searchParameters) {
        searchParameters.setFirstItemId(0);
        searchParameters.setActivePage(1);
        return searchParameters;
    }

    /**
     * move file to confidential folder from relative path
     * @param relativePath
     * @throws IOException
     */
    private void moveFileToConfidentialFolderFromRelativePath(String relativePath) throws IOException {

        File file = new File(photosProcessedPath + relativePath.substring(resourcesRootPath.length()));
        Path confidentialPhotoPath = Paths.get(photosConfidentialPath + relativePath.substring(resourcesRootPath.length()));
        Files.createDirectories(confidentialPhotoPath.getParent());
        Files.move(file.toPath(), confidentialPhotoPath);
    }

    /**
     * move file to public folder from relative path
     * @param relativePath
     * @throws IOException
     */
    private void moveFileToPublicFolderFromRelativePath(String relativePath) throws IOException {

        File file = new File(photosConfidentialPath + relativePath.substring(resourcesConfidentialRootPath.length()));
        Path publicPhotoPath = Paths.get(photosProcessedPath + relativePath.substring(resourcesConfidentialRootPath.length()));
        Files.createDirectories(publicPhotoPath.getParent());
        Files.move(file.toPath(), publicPhotoPath);
    }
}