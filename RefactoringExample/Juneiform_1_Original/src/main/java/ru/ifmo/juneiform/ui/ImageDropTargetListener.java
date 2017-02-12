/*
 * Copyright 2011 The Juneiform Team.
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
package ru.ifmo.juneiform.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import ru.ifmo.juneiform.AcceptableImageExtension;
import ru.ifmo.juneiform.Controller;

/**
 *
 * @author Oleg "xplt" Kuznetsov
 */
public class ImageDropTargetListener implements DropTargetListener {
    private static final Logger logger = Logger.getLogger(ImageDropTargetListener.class);
    private JPanel previewsPanel;
    private Controller controller;
    
    public ImageDropTargetListener(JPanel previewsPanel, Controller controller) {
        this.controller = controller;
        this.previewsPanel = previewsPanel;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (!isDragAcceptable(dtde)) {
            dtde.rejectDrag();
            return;
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        List<File> filesToProcess = addFiles(dtde);

        if (!isEmptyAfterFilteration(filesToProcess)) {
            processFiles(filesToProcess);
        }                   
    }

    private List<File> addFiles(DropTargetDropEvent dtde) {
        Transferable transferable = dtde.getTransferable();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        List<File> filesToProcess = new ArrayList<File>();

        for (int i = 0; i < flavors.length; i++) {
            DataFlavor dataFlavor = flavors[i];
            filesToProcess.addAll(extractFileByFlavor(dataFlavor, transferable));           
        }
        return filesToProcess;
    }
    
    private List<File> addFiles(DropTargetDragEvent dtde) {
        Transferable transferable = dtde.getTransferable();
        List<File> filesToProcess = new ArrayList<File>();
        List<DataFlavor> filetypes = dtde.getCurrentDataFlavorsAsList();
            
        for (DataFlavor dataFlavor : filetypes) {
            filesToProcess.addAll(extractFileByFlavor(dataFlavor, transferable));
        }
        return filesToProcess;
    }
    
    private List<File> extractFileByFlavor(DataFlavor dataFlavor, Transferable transferable) {
        List<File> extractedFiles = new ArrayList<File>();

        try {
            DataFlavor linuxFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
            
            /**
             * Hack for Drag'n'Drop support on Linux with Java6
             */  
            if (dataFlavor.equals(linuxFlavor)) {
                String data = (String)transferable.getTransferData(linuxFlavor);

                for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) {
                    String token = st.nextToken().trim();

                    if (token.startsWith("#") || token.isEmpty()) {
                         continue;
                    }

                    try {
                        File file = new File(new URI(token));
                        extractedFiles.add(file);
                    } catch (URISyntaxException ex) {
                        logger.log(Level.ERROR, ex);
                    }
                }
            } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
                try {
                    List<File> list = (List<File>) transferable.getTransferData(dataFlavor);

                    for (File file : list) {
                        extractedFiles.add(file);
                    }
                } catch (UnsupportedFlavorException ex) {
                    logger.log(Level.ERROR, ex);
                } catch (IOException ex) {
                    logger.log(Level.ERROR, ex);
                }
            }
        } catch (UnsupportedFlavorException ex) {
            logger.log(Level.ERROR, ex);
        } catch (IOException ex) {
            logger.log(Level.ERROR, ex);
        } catch (ClassNotFoundException ex) {
            logger.log(Level.ERROR, ex);
        }
        return extractedFiles;
    }
    
    private boolean isEmptyAfterFilteration(List<File> filesToFilter) {
        boolean keepFile = false;

        if (!filesToFilter.isEmpty()) {
            for (Iterator<File> it = filesToFilter.iterator(); it.hasNext();) {
                File file = it.next();

                for (AcceptableImageExtension extension : AcceptableImageExtension.values()) {
                    if (FilenameUtils.getExtension(file.getName().toLowerCase()).equals(extension.toString())) {
                        keepFile = true;
                        continue;
                    }
                }

                if (keepFile) {
                    keepFile = false;
                } else {
                    it.remove();
                }
            }
        } else {
            logger.log(Level.WARN, "Empty queue!");
            return true;
        }
        
        if (filesToFilter.isEmpty()) {
            return true;
        } else {
            return false;
        }        
    }
    
    private void processFiles(List<File> filesToProcess) {
        if (!filesToProcess.isEmpty()) {
            controller.processOpenedFiles((File[])filesToProcess.toArray(new File[filesToProcess.size()]));
        } else {
            logger.log(Level.WARN, "Trying to process empty queue!");
        }
    }
    
    private boolean isDragAcceptable(DropTargetDragEvent dtde) {
        List<File> filesToProcess = addFiles(dtde);
            
        if (containsUsefulItems(filesToProcess)) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean containsUsefulItems(List<File> queuedFiles) {
        if (!queuedFiles.isEmpty()) {
            
            for (int i = 0; i < queuedFiles.size(); i++) {
                for (AcceptableImageExtension extension : AcceptableImageExtension.values()) {

                    if (FilenameUtils.getExtension(queuedFiles.get(i).getName().toLowerCase()).equals(extension.toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
