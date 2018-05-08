package com.gyd.photosearch.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.min;

public class ImageResizer {

    private static int THUMBNAIL_MAX_HEIGHT = 800;
    private static int THUMBNAIL_MAX_WIDTH = 800;

    private static int HIGH_QUALITY_MAX_HEIGHT = 1600;
    private static int HIGH_QUALITY_MAX_WIDTH = 1600;

    private static BufferedImage inputImage;


    /**
     * Create thumbnail image
     * @param inputImagePath Path of the original image
     * @param outputImagePath Path to save the resized image
     * @throws IOException
     */
    public static void createThumbnailImage(String inputImagePath, String outputImagePath)
            throws IOException {

        createImageInDifferentFormat(inputImagePath, outputImagePath, THUMBNAIL_MAX_WIDTH, THUMBNAIL_MAX_HEIGHT);
    }

    /**
     * Create high quality image
     * @param inputImagePath Path of the original image
     * @param outputImagePath Path to save the resized image
     * @throws IOException
     */
    public static void createHighQualityImage(String inputImagePath, String outputImagePath)
            throws IOException {

        createImageInDifferentFormat(inputImagePath, outputImagePath, HIGH_QUALITY_MAX_WIDTH, HIGH_QUALITY_MAX_HEIGHT);
    }

    /**
     * Create image in different format : target or thumbnail
     * @param inputImagePath Path of the original image
     * @param outputImagePath Path to save the resized image
     * @throws IOException
     */
    private static void createImageInDifferentFormat(String inputImagePath, String outputImagePath, int maxWidth, int maxHeight)
            throws IOException {

        File inputFile = new File(inputImagePath);
        inputImage = ImageIO.read(inputFile);
        double targetPercent = min(
                (double) maxWidth / (double) inputImage.getWidth(),
                (double) maxHeight / (double) inputImage.getHeight());
        resize(outputImagePath, targetPercent);
    }

    /**
    * Resizes an image by a percentage of original size (proportional).
    * @param outputImagePath Path to save the resized image
    * @param percent a double number specifies percentage of the output image over the input image.
    * @throws IOException
    */
    private static void resize(String outputImagePath, double percent) throws IOException {

        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        resize(outputImagePath, scaledWidth, scaledHeight);
    }

    /**
     * Resizes an image to a absolute width and height (the image may not be
     * proportional)
     * @param outputImagePath Path to save the resized image
     * @param scaledWidth absolute width in pixels
     * @param scaledHeight absolute height in pixels
     * @throws IOException
     */
    private static void resize(String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath
                .lastIndexOf(".") + 1);

        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
}
