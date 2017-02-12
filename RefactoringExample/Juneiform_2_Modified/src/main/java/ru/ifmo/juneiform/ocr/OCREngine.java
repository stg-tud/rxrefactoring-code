package ru.ifmo.juneiform.ocr;

import java.io.File;

/**
 *
 * @author Ivan Stepuk
 */
public interface OCREngine {

    String performOCR(File imageFile) throws OCRException;

    String performOCR(String pathToImage) throws OCRException;
}
