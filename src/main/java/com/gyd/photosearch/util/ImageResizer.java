package com.gyd.photosearch.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.min;

public class ImageResizer {

    private static int THUMBNAIL_MAX_HEIGHT = 400;
    private static int THUMBNAIL_MAX_WIDTH = 400;

    private static BufferedImage inputImage;


    /**
     * Create thumbnail image
     * @param inputImagePath Path of the original image
     * @param outputImagePath Path to save the resized image
     * @throws IOException
     */
    public static void createThumbnailImage(String inputImagePath, String outputImagePath)
            throws IOException {

        File inputFile = new File(inputImagePath);
        inputImage = ImageIO.read(inputFile);
        double targetPercent = min(
                (double) THUMBNAIL_MAX_WIDTH / (double) inputImage.getWidth(),
                (double) THUMBNAIL_MAX_HEIGHT / (double) inputImage.getHeight());
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
