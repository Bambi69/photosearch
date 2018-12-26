package com.gyd.photosearch.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyd.photosearch.entity.Indexation;
import com.gyd.photosearch.entity.Location;
import com.gyd.photosearch.entity.Photo;
import com.gyd.photosearch.exception.TechnicalException;
import com.gyd.photosearch.repository.IndexationRepository;
import com.gyd.photosearch.repository.PhotoRepository;
import com.gyd.photosearch.util.DateUtil;
import com.gyd.photosearch.util.ImageResizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IndexationServiceImpl implements IndexationService {

    private static String STATUS_IN_ERROR = "Erreur";
    private static String STATUS_IN_PROGRESS = "En cours";
    private static String STATUS_SUCCESSFUL = "Ok";
    private static String STATUS_DELETED = "Supprimé";

    private static String HD_FOLDER = "hd"; // for high quality genrated images
    private static String THB_FOLDER = "thb"; // for thumbnail images

    @Value("${path.resources.root}")
    private String resourcesRootPath;

    @Value("${path.photos.toIndex}")
    private String photosToIndexPath;

    @Value("${path.photos.processed}")
    private String photosProcessedPath;

    @Value("${path.photos.archive}")
    private String photosArchivePath;

    @Value("${path.photos.inerror}")
    private String photosInErrorPath;

    @Value("${photo.tag.confidentiel}")
    private String confidentialTag;

    @Value("${photo.tag.withoutFace}")
    private String withoutFaceTag;

    @Value("${photo.tag.withoutLocation}")
    private String withoutLocationTag;

    @Value("${photo.tag.withLocation}")
    private String withLocationTag;

    @Value("${photo.tag.withoutCamera}")
    private String withoutCameraTag;

    @Value("${photo.tag.withCamera}")
    private String withCameraTag;

    private Integer nbInError;
    private Integer nbProcessed;
    private String archiveDirectoryPath;
    private String processedDirectoryAboslutePath;
    private String processedHdDirectoryAboslutePath;
    private String processedThbDirectoryAbsolutePath;
    private String processedHdDirectoryResourcePath;
    private String processedThbDirectoryResourcePath;
    private String errorDirectoryPath;
    private List<Photo> photos;

    @Autowired
    private IndexationRepository indexationRepository;

    @Autowired
    private PhotoRepository photoRepository;

    private Logger logger = LogManager.getRootLogger();

    @Override
    public void deleteIndexation(String id) throws Exception {

        // find this indexation
        Indexation indexationToDelete = indexationRepository.findById(id);

        // init global variables
        archiveDirectoryPath = photosArchivePath + indexationToDelete.getRepositoryName();
        processedDirectoryAboslutePath = photosProcessedPath + indexationToDelete.getRepositoryName();
        errorDirectoryPath = photosInErrorPath + indexationToDelete.getRepositoryName();

        // delete repositories
        deleteDirectory(new File(archiveDirectoryPath));
        deleteDirectory(new File(processedDirectoryAboslutePath));
        deleteDirectory(new File(errorDirectoryPath));

        // delete indexed photos
        photoRepository.deletePhotosByIndexationName(indexationToDelete.getPhotoTag());

        // update indexation status
        indexationToDelete.setStatus(STATUS_DELETED);
        indexationRepository.update(indexationToDelete);
    }

    /**
     * delete directory (recursively: must delete content before deleting dir)
     *
     * @param directoryToBeDeleted
     * @return
     */
    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * index photos in elasticsearch
     */
    @Override
    @Async
    public CompletableFuture<Void> indexPhotos(Indexation indexation) throws Exception {

        // init all variables
        reinitVariables();

        // check indexation attributes
        if (indexation.getIndexationName() == null || indexation.getIndexationName().compareTo("") == 0
                || indexation.getRepositoryName() == null || indexation.getRepositoryName().compareTo("") == 0) {
            throw new Exception("impossible to import photos : mandatory attributes are null");
        }

        // check if indexation name not already exists
        List<Indexation> existingIndexations = indexationRepository.findByName(indexation.getIndexationName());
        if (existingIndexations != null && existingIndexations.size() > 0) {
            throw new Exception("this indexation name already exists");
        }

        // check if indexation repository not already exists
        existingIndexations = indexationRepository.findByRepository(indexation.getRepositoryName());
        if (existingIndexations != null && existingIndexations.size() > 0) {
            throw new Exception("this indexation repository already exists");
        }

        // list all jpg files to index
        File folder = new File(photosToIndexPath);
        File[] listOfFiles = folder.listFiles(new FilenameFilter() {

            //apply a filter
            @Override
            public boolean accept(File dir, String name) {
                boolean result;
                if (name.endsWith(".jpg")) {
                    result=true;
                } else {
                    result=false;
                }
                return result;
            }
        });

        // return error if no file to index
        if (listOfFiles == null || listOfFiles.length == 0) {
            throw new Exception("no photo to index");
        }

        // set indexation calculated attributes
        indexation.setDate(DateUtil.convertDateToEsFormat(new Date()));
        indexation.setPhotoTag(DateUtil.convertDateToSimpleFormat(new Date()) + " - " + indexation.getIndexationName());
        indexation.setStatus(STATUS_IN_PROGRESS);
        indexation.setNbFilesToIndex(listOfFiles.length);

        // try to generate json (needed to index it in elastic search)
        ObjectMapper mapper = new ObjectMapper();
        indexation.setJson(mapper.writeValueAsBytes(indexation));

        // save indexation object
        String id = indexationRepository.create(indexation);
        indexation.setId(id);

        // to calculate duration
        Instant startInstant = Instant.now();

        // initialize list of jpg photos from photosToIndexPath
        createFolderStructure(indexation.getRepositoryName());
        analysePhotosFromListOfFiles(listOfFiles, indexation.getPhotoTag());

        // create index
        photoRepository.indexPhotos(photos);

        // if no photo in error, delete error folder
        deleteErrorDirectory();

        // calculate duration
        Instant endInstant = Instant.now();
        indexation.setDuration((int) ChronoUnit.SECONDS.between(startInstant,endInstant));

        // set indexation status
        indexation.setStatus(STATUS_SUCCESSFUL);
        indexation.setNbFilesInError(nbInError);
        indexation.setNbFilesProcessed(nbProcessed);

        // update indexation
        indexationRepository.update(indexation);

        // init all variables
        reinitVariables();

        return CompletableFuture.completedFuture(null);
    }

    /**
     * init all global variables
     */
    private void reinitVariables() {
        nbInError = 0;
        nbProcessed = 0;
        archiveDirectoryPath = "";
        processedDirectoryAboslutePath = "";
        processedHdDirectoryAboslutePath = "";
        processedThbDirectoryAbsolutePath = "";
        processedHdDirectoryResourcePath = "";
        processedThbDirectoryResourcePath = "";
        errorDirectoryPath = "";
        photos = new ArrayList<>();
    }

    @Override
    public Indexation findById(String id) throws TechnicalException {
        return indexationRepository.findById(id);
    }

    @Override
    public List<Indexation> findAll() throws TechnicalException {

        List<Indexation> result = null;

        try {
            result = indexationRepository.findAll();
        } catch (IndexNotFoundException e) {
            logger.error("Index not found : try to create it");
            indexationRepository.deleteIndex();
            return findAll();
        }

        return indexationRepository.findAll();
    }

    @Override
    public void save(Indexation indexation) throws Exception {

        // if id is provided, update indexation
        if (indexation.getId() != null && indexation.getId().compareTo("")!=0) {
            //indexationRepository.update(indexation);

            // else create it
        } else {

            // generate json (needed to index it in elastic search)
            try {
                ObjectMapper mapper = new ObjectMapper();
                indexation.setJson(mapper.writeValueAsBytes(indexation));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

            // create indexation
            indexationRepository.create(indexation);
        }
    }

    @Override
    public void delete(String id) {
        //indexationRepository.delete(id);
    }

    /**
     * analyse all photos to index:
     * - retrieve relevant metadata
     * - create thumbnail image to display in list
     * - create HD image to display alone
     */
    private void analysePhotosFromListOfFiles(File[] listOfFiles, String indexationPhotoTag) {

        // instance a json mapper
        ObjectMapper mapper = new ObjectMapper(); // create once, reuse

        // read list of files
        for (int i = 0; i < listOfFiles.length; i++) {

            // check if file is not a folder
            if (listOfFiles[i].isFile()) {

                // initialize Photo
                Photo p = new Photo();
                p.setName(listOfFiles[i].getName());
                p.setDateIndexed(new Date());

                try {
                    logger.info("File " + listOfFiles[i].getName());

                    // retrieve relevant metadata
                    retrieveRelevantMetadata(p, listOfFiles[i]);

                    // define indexation photo tag
                    p.setIndexationName(indexationPhotoTag);

                    // generate thumbnail and HD image from original
                    p = generateThumbnailHdImage(p, p.getName());

                    // finally, move original file to archive folder
                    archiveProcessedPhoto(listOfFiles[i]);

                    // generate json (needed to index it in elastic search)
                    p.setJson(mapper.writeValueAsBytes(p));

                    // if no error, we can add p into the list for indexation
                    photos.add(p);

                    // increment nb processed photos
                    nbProcessed++;

                // in case of any exception, file is moved in error path
                } catch (ImageProcessingException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                    archivePhotoInError(listOfFiles[i]);
                } catch (FileAlreadyExistsException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                    archivePhotoInError(listOfFiles[i]);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                    archivePhotoInError(listOfFiles[i]);
                }
            }
        }
    }

    /**
     * archive processed photo (move to archive folder)
     * @param file the original photo
     */
    private void archiveProcessedPhoto(File file) throws IOException {
        Files.move(file.toPath(), Paths.get(archiveDirectoryPath + file.getName()));
    }

    /**
     * create directories to use during files processing
     *
     * @param repositoryName
     */
    private void createFolderStructure(String repositoryName) {

        // create archive directory
        archiveDirectoryPath = photosArchivePath + repositoryName + "/";
        File archiveDirectory = new File(archiveDirectoryPath);
        archiveDirectory.mkdir();

        // create HD processed directory
        processedHdDirectoryAboslutePath = photosProcessedPath + repositoryName + "/" + HD_FOLDER + "/";
        processedHdDirectoryResourcePath = resourcesRootPath + repositoryName + "/" + HD_FOLDER + "/";
        File hdDirectory = new File(processedHdDirectoryAboslutePath);
        hdDirectory.mkdirs();

        // create THB processed directory
        processedThbDirectoryAbsolutePath = photosProcessedPath + repositoryName + "/" + THB_FOLDER + "/";
        processedThbDirectoryResourcePath = resourcesRootPath + repositoryName + "/" + THB_FOLDER + "/";
        File thbDirectory = new File(processedThbDirectoryAbsolutePath);
        thbDirectory.mkdir();

        // create error directory
        errorDirectoryPath = photosInErrorPath + repositoryName + "/";
        File errorDirectory = new File(errorDirectoryPath);
        errorDirectory.mkdir();
    }

    /**
     * if no error, delete directory
     */
    private void deleteErrorDirectory() {
        if (nbInError == 0) {
            File errorDirectory = new File(errorDirectoryPath);
            errorDirectory.delete();
        }
    }

    /**
     * move original photo to error folder
     * @param file the original photo
     */
    private void archivePhotoInError(File file) {
        try {
            Files.move(file.toPath(), Paths.get(photosInErrorPath + file.getName()), StandardCopyOption.REPLACE_EXISTING);

            // increment nb photos in error
            nbInError++;

        } catch (IOException e) {
            logger.error("CRITICAL : impossible to move photo named " + file.getName() + " in error folder");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * process original image to create hd and thumbnail images
     * @param name
     */
    private Photo generateThumbnailHdImage(Photo photo, String name) throws IOException {

        ImageResizer.createThumbnailImage(
                photosToIndexPath + name, processedThbDirectoryAbsolutePath + getThumbnailFileNameFromFileName(name));

        ImageResizer.createHighQualityImage(
                photosToIndexPath + name, processedHdDirectoryAboslutePath + name);

        // set resource path to hd and thb photos
        photo.setPathToThbPhoto(processedThbDirectoryResourcePath + getThumbnailFileNameFromFileName(photo.getName()));
        photo.setPathToHdPhoto(processedHdDirectoryResourcePath + name);

        return photo;
    }

    /**
     * get thumbnail file name
     * @param name
     * @return
     */
    private String getThumbnailFileNameFromFileName(String name) {

        int lastDot = name.lastIndexOf('.');
        return name.substring(0,lastDot) + "thb" + name.substring(lastDot);
    }

    /**
     * retrieve relevant metadata from file
     * @param p photo object to complete
     * @param file file to analyse
     */
    private void retrieveRelevantMetadata(Photo p, File file) throws ImageProcessingException, IOException {

        // read image metadata
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        // READ EXIF METADATA
        ExifIFD0Directory exifFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exifFD0Directory != null) {

            // retrieve camera model
            String cameraMake = exifFD0Directory.getString(ExifIFD0Directory.TAG_MAKE);
            String cameraModel = exifFD0Directory.getString(ExifIFD0Directory.TAG_MODEL);

            // if these metadata are correctly defined
            if ((cameraMake != null && cameraMake.compareTo("") != 0)
                    || (cameraModel != null && cameraModel.compareTo("") != 0)) {

                // set camera model
                p.setCameraModel(cameraMake + " - " + cameraModel);

                // add tag
                p.getTags().add(withCameraTag);

            } else {

                // add tag
                p.getTags().add(withoutCameraTag);
            }

            // retrieve original date time
            Date originalDateTime = exifFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
            Calendar c = Calendar.getInstance();
            c.setTime(originalDateTime);
            p.setDateTimeOriginal(DateUtil.convertDateToEsFormat(originalDateTime));
            p.setYearTimeOriginal(c.get(Calendar.YEAR));
            p.setMonthTimeOriginal(DateUtil.getMonthFromDate(originalDateTime));
        }

        // READ IPTC METADATA
        IptcDirectory iptcDirectory = metadata.getFirstDirectoryOfType(IptcDirectory.class);
        if (iptcDirectory != null) {

            // retrieve keywords (with identified faces)
            String[] keywords = iptcDirectory.getStringArray(IptcDirectory.TAG_KEYWORDS);

            if (keywords != null) {
                for (int j = 0; j < keywords.length; j++) {
                    if (keywords[j].compareTo(confidentialTag)==0) {
                        p.setConfidential(true);
                    } else {
                        p.getFaces().add(keywords[j]);
                    }
                }

                // set nb identified face
                p.setNbFaces(p.getFaces().size());
            }

            // if no face, add tag to this photo
            if (p.getFaces().size() == 0) {
                p.getTags().add(withoutFaceTag);
            }
        }

        // READ GPS METADATA
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory != null
                && gpsDirectory.getRationalArray(GpsDirectory.TAG_LONGITUDE) != null
                && gpsDirectory.getRationalArray(GpsDirectory.TAG_LATITUDE) != null) {

            // retrieve GPS coordinates
            p.setLocation(new Location());

            // calculate longitude
            Rational[] longRat = gpsDirectory.getRationalArray(GpsDirectory.TAG_LONGITUDE);
            Double longitude = longRat[0].doubleValue()
                    + longRat[1].doubleValue()/60
                    + longRat[2].doubleValue()/3600;
                    /*
                    if (gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF).equalsIgnoreCase("N")) {
                        longitude = longitude * -1;
                    }
                    */
            p.getLocation().setLon(longitude);

            // calculate latitude
            Rational[] latRat = gpsDirectory.getRationalArray(GpsDirectory.TAG_LATITUDE);
            Double latitude = latRat[0].doubleValue()
                    + latRat[1].doubleValue()/60
                    + latRat[2].doubleValue()/3600;
                    /*
                    if (gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF).equalsIgnoreCase("N")) {
                        latitude = latitude * -1;
                    }
                    */
            p.getLocation().setLat(latitude);

            // add tag
            p.getTags().add(withLocationTag);

        } else {

            // add tag
            p.getTags().add(withoutLocationTag);
        }

        // READ JPEG METADATA
        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        if (jpegDirectory != null) {

            // retrieve resolution
            String resolution =
                    jpegDirectory.getString(JpegDirectory.TAG_IMAGE_WIDTH)
                            + "x"
                            + jpegDirectory.getString(JpegDirectory.TAG_IMAGE_HEIGHT);
            p.setResolution(resolution);
            logger.info("resolution " + resolution);
        }
    }

    @Override
    public void deleteIndex() {

        // delete indexation index
        indexationRepository.deleteIndex();
    }
}
