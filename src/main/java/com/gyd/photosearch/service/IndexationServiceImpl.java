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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class IndexationServiceImpl implements IndexationService {

    @Value("${system.imagePath}")
    private String imagePath;

    @Autowired
    private IndexationRepository indexationRepository;

    private Logger logger = LogManager.getRootLogger();

    private List<Photo> photos = new ArrayList<Photo>();

    /**
     * index photos in elasticsearch
     */
    @Override
    public void indexPhotos() {

        // initialize list of jpg photos from imagePath
        setPhotosFromRepository();

        // create index
        indexationRepository.indexPhotos(photos);
    }

    /**
     * retrieve photo from imagePath
     */
    private void setPhotosFromRepository() {
        try {
            // initialize folder and list of files
            File folder = new File(imagePath);
            File[] listOfFiles = folder.listFiles();

            // instance a json mapper
            ObjectMapper mapper = new ObjectMapper(); // create once, reuse

            // read list of files
            for (int i = 0; i < listOfFiles.length; i++) {

                // analyzing only jpg files
                if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".jpg")) {
                    logger.info("File " + listOfFiles[i].getName());

                    // initialize Photo
                    Photo p = new Photo();
                    p.setName(listOfFiles[i].getName());
                    p.setDirectory(imagePath);
                    p.setDateIndexed(new Date());

                    // read image metadata
                    Metadata metadata = ImageMetadataReader.readMetadata(listOfFiles[i]);

                    // READ EXIF METADATA
                    ExifIFD0Directory exifFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                    if (exifFD0Directory != null) {

                        // retrieve original date time
                        Date originalDateTime = exifFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
                        Calendar c = Calendar.getInstance();
                        c.setTime(originalDateTime);
                        p.setDateTimeOriginal(DateUtil.convertDateToEsFormat(originalDateTime));
                        p.setYearTimeOriginal(c.get(Calendar.YEAR));
                        p.setMonthTimeOriginal(c.get(Calendar.MONTH)+1);

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
                    if (gpsDirectory != null) {

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

                    // generate json (needed to index it in elastic search)
                    p.setJson(mapper.writeValueAsBytes(p));

                    // generate thumbnail image
                    ImageResizer.createThumbnailImage(
                            imagePath + p.getName(),imagePath + p.getName());

                    // add Photo into the list
                    photos.add(p);
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ImageProcessingException ipe) {
            ipe.printStackTrace();
        }
    }
}
