package ru.ifmo.juneiform.ocr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import ru.ifmo.juneiform.Language;
import ru.ifmo.juneiform.Settings;
import ru.ifmo.juneiform.Utils;

/**
 *
 * @author Ivan Stepuk
 */
public class Cuneiform implements OCREngine {

    private static final Logger log = Logger.getLogger(Cuneiform.class);
    private Language language;
    private File output;
    private Settings settings;
    
    public Cuneiform (Settings settings) {
        this.settings = settings;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setOutputFile(File output) {
        this.output = output;
    }

    public String performOCR(String pathToImage, boolean cleanup) throws CuneiformException {
        String result = performOCR(pathToImage);
        if (cleanup) {
            output.delete();
        }
        return result;
    }

    @Override
    public String performOCR(File imageFile) throws CuneiformException {
        return performOCR(imageFile.getAbsolutePath());
    }

    @Override
    public String performOCR(String pathToImage) throws CuneiformException {
        List<String> command = new ArrayList<String>();
        command.add(settings.getPathToCuneiform().toString());
        command.add("-l");
        command.add(language.getShortening());
        command.add("-o");
        try {
            command.add(output.getCanonicalPath().replaceAll(" ", "\\ "));
            command.add(pathToImage.replaceAll(" ", "\\ "));
        } catch (IOException ex) {
            log.error("Error retrieving file path", ex);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            throw new CuneiformException("Could not run specified command: "
                    + processBuilder.command());
        }

        int result = -1;
        try {
            result = process.waitFor();
        } catch (InterruptedException ex) {
            throw new CuneiformException("OCR process was interrupted");
        }

        if (result != 0) {
            throw new CuneiformException("Execution result " + result);
        }

        String recognizedText = null;
        try {
            recognizedText = Utils.readTextFile(output);
        } catch (IOException ex) {
            log.warn("Unable to read output file");
        } finally {
            return recognizedText;
        }
    }
}
