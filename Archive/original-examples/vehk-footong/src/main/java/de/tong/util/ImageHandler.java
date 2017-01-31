package de.tong.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This small utility-class makes it possible to load images from the resource
 * directory.
 * 
 * @author Wolfgang Müller
 * 
 */
public class ImageHandler {

    /**
     * 
     * This method loads an image from the local resource directory and returns
     * it to the caller. This method throws an {@link IOException} when the file
     * could not be read.
     * 
     * @param filename
     *            The filename of the image. E.g. "title.png".
     * @return An instance of BufferedImage.
     * 
     */
    public static BufferedImage loadImage(String filename) {
	BufferedImage img = null;

	try {
	    img = ImageIO.read(new File("res/" + filename));

	} catch (IOException e) {

	    e.printStackTrace();

	}
	return img;
    }
}
