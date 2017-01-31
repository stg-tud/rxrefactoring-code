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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import ru.ifmo.juneiform.ocr.OCRException;
import ru.ifmo.juneiform.ui.Preview;
import ru.ifmo.juneiform.ui.View;

/**
 *
 * @author Ivan Stepuk
 * @author felleet
 */
public class Controller implements ViewInteractor, PreviewActionListener {

    private static final Logger log = Logger.getLogger(Controller.class);
    private Editor editor;
    private View view;
    private List<Document> documents = new ArrayList<Document>();
    private Settings settings;

    public Controller(Editor editor, Settings settings) {
        this.editor = editor;
        this.settings = settings;
    }

    @Override
    public void injectView(View view) {
        editor.injectView(view);
        this.view = view;
    }

    public void processOpenedFiles(final File... files) {
        view.setStatus(L10n.forString("status.loading"));
        DocumentLoader documentLoader = new DocumentLoader(settings) {

            @Override
            public void fetchResult(List<Document> result) {
                for (Document doc : result) {
                    try {
                        addDocument(doc);
                        Preview preview = doc.getPreview();
                        view.addPreview(preview);
                        preview.addActionListener(Controller.this);
                    } catch (AlreadyOpenedException ex) {
                        log.debug("Already opened");
                    }
                }
                view.clearStatus(L10n.forString("status.loading"));
            }
        };
        documentLoader.load(files);
    }

    public void performOCR() throws OCRException {
        editor.performOCR();
    }

    public void addDocument(Document document) throws AlreadyOpenedException {
        for (Document exists : documents) {
            if (exists.equals(document)) {
                throw new AlreadyOpenedException();
            }
        }
        documents.add(document);
    }

    public Document selectedDocument() {
        int index = view.getSelectedPreviewIndex();
        if (index >= 0) {
            return documents.get(index);
        }
        return null;
    }

    public void removeSelectedDocument() {
        if (selectedDocument() != null) {
            editor.closeDocument();
            documents.remove(selectedDocument());
            view.removePreview(view.getSelectedPreviewIndex());
        }
    }

    @Override
    public void actionPerformed(Preview preview) {
        Document document = documents.get(view.getSelectedPreviewIndex());
        view.setLanguage(document.getLanguage());
        if (!document.equals(editor.getOpenedDocument())) {
            editor.openDocument(document);
        }
    }

    public void zoomIn() {
        editor.zoomIn();
    }

    public void zoomOut() {
        editor.zoomOut();
    }

    public void rotateLeft() {
        editor.rotateLeft();
    }

    public void rotateRight() {
        editor.rotateRight();
    }

    public void originalSize() {
        editor.originalSize();
    }

    public void applyChanges() {
        editor.getOpenedDocument().setText(view.getOutput());
    }

    public void processSavedFile(File selectedFile, String filter) {
        try {
            if (filter.contains("txt")) {
                Utils.writePlainFile(addExtensionIfNeeded(selectedFile, "txt"),
                        editor.getOpenedDocument().getText());
            } else if (filter.contains("odt")) {
                Utils.writeOpenDocument(addExtensionIfNeeded(selectedFile, "odt"),
                        editor.getOpenedDocument().getText(), getImagesForSingleDocument());
            } else {
                log.warn("Unknown file filter");
            }
        } catch (IOException ex) {
            view.showErrorDialog(L10n.forString("msg.could.not.save"),
                    L10n.forString("msg.could.not.save"));
        }
    }

    public void processSavedFiles(File selectedFile, String filter) {
        try {
            if (filter.contains("txt")) {
                Utils.writePlainFile(addExtensionIfNeeded(selectedFile, "txt"),
                        getTextFromAllDocuments());
            } else if (filter.contains("odt")) {
                Utils.writeOpenDocument(addExtensionIfNeeded(selectedFile, "odt"),
                        getTextFromAllDocuments(), getImagesFromAllDocuments());
            } else {
                log.warn("Unknown file filter");
            }
        } catch (IOException ex) {
            view.showErrorDialog(L10n.forString("msg.could.not.save"),
                    L10n.forString("msg.could.not.save"));
        }
    }

    public void processScanImage() {
        File file = editor.scanImage();
        if (file.exists()) {
            this.processOpenedFiles(file);
        }
    }

    public void deleteOverlays() {
        try {
            editor.getOpenedDocument().deleteOverlays();

        } catch (Exception e) {
            // TO-DO: show error dialog instead of logging
            log.warn("NPE. Document not opened or list of overlays is empty");
        }

    }

    private File addExtensionIfNeeded(File file, String extension) {
        String filename = file.getPath();
        if (!filename.endsWith(".".concat(extension))) {
            filename = filename.concat(".").concat(extension);
        }
        File fresh = new File(filename);
        file.renameTo(fresh);
        return fresh;
    }

    private String getTextFromAllDocuments() {
        StringBuilder sb = new StringBuilder();
        for (Document document : documents) {
            sb.append(document.getText()).append("\n");
        }
        return sb.toString();
    }

    private List<Image> getImagesForSingleDocument() {
        List<Image> images;

        images = getImages(editor.getOpenedDocument());

        return images;
    }

    private List<Image> getImagesFromAllDocuments() {
        List<Image> images = new ArrayList<Image>();

        for (Document document : documents) {
            for (Image image : getImages(document)) {
                images.add(image);
            }
        }
        return images;
    }

    private List<Image> getImages(Document document) {
        List images = new ArrayList<Image>();
        BufferedImage bi = (BufferedImage) document.getImage();
        int imageWidth = bi.getWidth();
        int imageHeith = bi.getHeight();
        
        for (Overlay overlay : document.getOverlays()) {
            
            if (overlay.getOverlayType() == OverlayType.PICTURE) {
                if (overlay.getxEndCoordinate() > imageWidth) {
                    overlay.setxEndCoordinate(imageWidth);
                }
                
                if (overlay.getyEndCoordinate() > imageHeith) {
                    overlay.setyEndCoordinate(imageHeith);
                }
                                
                images.add(bi.getSubimage(overlay.getxStartCoordinate(), overlay.getyStartCoordinate(),
                        overlay.getxEndCoordinate() - overlay.getxStartCoordinate(), overlay.getyEndCoordinate()
                        - overlay.getyStartCoordinate()));
            }
        }
        return images;
    }
}
