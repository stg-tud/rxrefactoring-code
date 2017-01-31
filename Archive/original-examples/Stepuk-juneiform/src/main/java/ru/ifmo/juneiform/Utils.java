/*
 * Copyright 2011-2012 The Juneiform Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ifmo.juneiform;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.log4j.Logger;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

/**
 *
 * @author Ivan Stepuk
 * @author Oleg "xplt" Kuznetsov
 * @author felleet
 */
public final class Utils {

    public static final int BUFFER_SIZE = 2048;
    private static final Logger log = Logger.getLogger(Utils.class);

    private Utils() {
    }

    public static String readTextFile(File file) throws IOException {
        FileReader in = new FileReader(file);
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.append(buffer, 0, length);
        }
        return out.toString();
    }

    public static void writePlainFile(File file, String content) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.close();
    }

    public static void writeOpenDocument(File file, String content, List<Image> images) throws IOException {
        try {
            OdfTextDocument odt = OdfTextDocument.newTextDocument();
            String[] paragraphs = content.split("\\n");

            for (String p : paragraphs) {
                odt.newParagraph(p);
                odt.newParagraph();
            }
            
            if (!images.isEmpty()) {
                File tempFile;

                for (Image image : images) {
                    Random r = new Random();
                    tempFile = new File(System.getProperty("java.io.tmpdir") + "/temp-" + r.nextInt(10000) + ".png");
                    URI uri = new URI(tempFile.getAbsolutePath());
                    
                    BufferedImage bi = (BufferedImage) image;                    
                    ImageIO.write(bi, "png", tempFile);
                    
                    odt.newImage(uri);
                    //to shift image location a bit.
                    for (int i = 0; i < 10; i++) {
                        odt.newParagraph();
                    }
                    
                }
            }
            odt.save(file);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(L10n.forString("could.not.save"));
        }
    }

    public static double getAspectRatio(Image image) {
        return ((double) image.getWidth(null)) / image.getHeight(null);
    }

    public static double getAspectRatio(int width, int height) {
        return ((double) width) / height;
    }

    public static Dimension scaleDimension(Dimension dimension, double factor) {
        return new Dimension(
                (int) (factor * dimension.getWidth()),
                (int) (factor * dimension.getHeight()));
    }

    public static FileFilter openFileFilter() {
        List<String> acceptableExtensionsList = new ArrayList<String>();
        StringBuilder extensionsDescriptions = new StringBuilder();
        extensionsDescriptions.append("(");
        
        for (AcceptableImageExtension extension : AcceptableImageExtension.values()) {
            acceptableExtensionsList.add(extension.name());
            extensionsDescriptions.append("*.");
            extensionsDescriptions.append(extension.name());
            extensionsDescriptions.append(", ");
        }
        extensionsDescriptions.delete(extensionsDescriptions.length() - 2, extensionsDescriptions.length());  //We do not need last ", " symbols, so lets cut them off
        extensionsDescriptions.append(")");
        String[] acceptableExtensions = (String[])acceptableExtensionsList.toArray(new String[acceptableExtensionsList.size()]);
        
        return new FileNameExtensionFilter(L10n.forString("filter.images") + " " + extensionsDescriptions,
                acceptableExtensions);
    }

    public static List<FileFilter> saveFileFilters() {
        List<FileFilter> result = new ArrayList<FileFilter>();
        result.add(new FileNameExtensionFilter(L10n.forString("filter.text"), "txt"));
        result.add(new FileNameExtensionFilter(L10n.forString("filter.odt"), "odt"));
        return result;
    }

    public static ComboBoxModel languageComboBoxModel() {
        return new DefaultComboBoxModel(
                new String[]{
                        L10n.forString("lang.eng"),
                        L10n.forString("lang.rus"),
                        L10n.forString("lang.both")
                });
    }    
}
