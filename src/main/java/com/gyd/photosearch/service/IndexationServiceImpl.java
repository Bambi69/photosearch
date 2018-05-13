package com.gyd.photosearch.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyd.photosearch.entity.Location;
import com.gyd.photosearch.entity.Photo;
import com.gyd.photosearch.repository.IndexationRepository;
import com.gyd.photosearch.util.DateUtil;
import com.gyd.photosearch.util.ImageResizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class IndexationServiceImpl implements IndexationService {

    @Value("${path.photos.toIndex}")
    private String photosToIndexPath;

    @Value("${path.photos.hd.processed}")
    private String photosHdProcessedPath;

    @Value("${path.photos.thumbnail.processed}")
    private String photosThbProcessedPath;

    @Value("${path.photos.archive}")
    private String photosArchivePath;

    @Value("${path.photos.inerror}")
    private String photosInErrorPath;

    @Autowired
    private IndexationRepository indexationRepository;

    private Logger logger = LogManager.getRootLogger();

    private List<Photo> photos = new ArrayList<Photo>();

    /**
     * index photos in elasticsearch
     */
    @Override
    public void indexPhotos() {

        // initialize list of jpg photos from photosToIndexPath
        analysePhotosFromRepository();

        // create index
        indexationRepository.indexPhotos(photos);
    }

    /**
     * analyse all photos to index:
     * - retrieve relevant metadata
     * - create thumbnail image to display in list
     * - create HD image to display alone
     */
    private void analysePhotosFromRepository() {

        // initialize folder and list of files
        File folder = new File(photosToIndexPath);
        File[] listOfFiles = folder.listFiles();

        // instance a json mapper
        ObjectMapper mapper = new ObjectMapper(); // create once, reuse

        // read list of files
        for (int i = 0; i < listOfFiles.length; i++) {

            // analyzing only jpg files
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".jpg")) {

                // initialize Photo
                Photo p = new Photo();
                p.setName(listOfFiles[i].getName());
                p.setThbName(getThumbnailFileNameFromFileName(p.getName()));
                p.setDateIndexed(new Date());

                try {
                    logger.info("File " + listOfFiles[i].getName());

                    // retrieve relevant metadata
                    retrieveRelevantMetadata(p, listOfFiles[i]);

                    // generate json (needed to index it in elastic search)
                    p.setJson(mapper.writeValueAsBytes(p));

                    // generate thumbnail and HD image from original
                    generateThumbnailHdImage(p.getName());

                    // finally, move original file to archive folder
                    archiveProcessedPhoto(listOfFiles[i]);

                    // if no error, we can add p into the list for indexation
                    photos.add(p);

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
        Files.move(file.toPath(), Paths.get(photosArchivePath + file.getName()));
    }

    /**
     * move original photo to error folder
     * @param file the original photo
     */
    private void archivePhotoInError(File file) {
        try {
            Files.move(file.toPath(), Paths.get(photosInErrorPath + file.getName()), StandardCopyOption.REPLACE_EXISTING);
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
    private void generateThumbnailHdImage(String name) throws IOException {

        ImageResizer.createThumbnailImage(
                photosToIndexPath + name, photosThbProcessedPath + getThumbnailFileNameFromFileName(name));

        ImageResizer.createHighQualityImage(
                photosToIndexPath + name, photosHdProcessedPath + name);
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

            // retrieve original date time
            Date originalDateTime = exifFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
            Calendar c = Calendar.getInstance();
            c.setTime(originalDateTime);
            p.setDateTimeOriginal(DateUtil.convertDateToEsFormat(originalDateTime));
            p.setYearTimeOriginal(c.get(Calendar.YEAR));
            p.setMonthTimeOriginal(DateUtil.getMonthFromDate(originalDateTime));

            // retrieve camera model
            String cameraModel = exifFD0Directory.getString(ExifIFD0Directory.TAG_MAKE);
            cameraModel += " - " + exifFD0Directory.getString(ExifIFD0Directory.TAG_MODEL);
            p.setCameraModel(cameraModel);
        }

        // READ IPTC METADATA
        IptcDirectory iptcDirectory = metadata.getFirstDirectoryOfType(IptcDirectory.class);
        if (iptcDirectory != null) {

            // retrieve keywords (with identified faces)
            String[] keywords = iptcDirectory.getStringArray(IptcDirectory.TAG_KEYWORDS);

            if (keywords != null) {
                for (int j = 0; j < keywords.length; j++) {
                    //logger.info("keywords " + keywords[j]);
                    p.getFaces().add(keywords[j]);
                }
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
            logger.info("longitude: " + longitude);

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
            logger.info("latitude: " + latitude);
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
}
