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

import java.awt.dnd.DropTarget;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import ru.ifmo.juneiform.*;
import ru.ifmo.juneiform.ocr.OCRException;

/**
 *
 * @author Ivan Stepuk
 * @author Oleg "xplt" Kuznetsov
 * @author felleet
 */
public class MainFrame extends javax.swing.JFrame {
    private Controller controller;
    private View view;
    private SettingsWindow settingsWindow;
    private Settings settings;
    private AboutBox aboutBox;
    private DropTarget dropTarget;
    private InputPanel drawingPanel = InputPanel.getInstance();
    private OverlayType drawState = OverlayType.NONE;

    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();
        
        for (FileFilter ff : Utils.saveFileFilters()) {
            saveDialog.addChoosableFileFilter(ff);
        }
        rotateLeftButton.setVisible(false);
        rotateRightButton.setVisible(false);
    }

    public MainFrame(Controller controller, SettingsWindow settingsWindow, Settings settings, AboutBox aboutBox) {
        this();
        this.controller = controller;
        this.view = new ViewImpl(this);
        this.controller.injectView(view);
        this.settingsWindow = settingsWindow;
        this.settings = settings;
        this.aboutBox = aboutBox;
        dropTarget = new DropTarget(previewsPanel, new ImageDropTargetListener(previewsPanel, controller));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        openDialog = new javax.swing.JFileChooser();
        saveDialog = new javax.swing.JFileChooser();
        toolBar = new javax.swing.JToolBar();
        openDocumentButton = new javax.swing.JButton();
        removeDocumentButton = new javax.swing.JButton();
        separator0 = new javax.swing.JToolBar.Separator();
        zoomInButton = new javax.swing.JButton();
        originalSizeButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        rotateLeftButton = new javax.swing.JButton();
        rotateRightButton = new javax.swing.JButton();
        scanImage = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        selectPictureAreaButton = new javax.swing.JButton();
        deleteOverlaysAreaButton = new javax.swing.JButton();
        separator1 = new javax.swing.JToolBar.Separator();
        languageLabel = new javax.swing.JLabel();
        languageComboBox = new javax.swing.JComboBox();
        ocrButton = new javax.swing.JButton();
        applyChangesButton = new javax.swing.JButton();
        separator2 = new javax.swing.JToolBar.Separator();
        saveButton = new javax.swing.JButton();
        saveAllButton = new javax.swing.JButton();
        statusBar = new javax.swing.JLabel();
        previewsScrollPane = new javax.swing.JScrollPane();
        previewsPanel = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        inputScrollPane = new javax.swing.JScrollPane();
        inputPanel = InputPanel.getInstance();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openImageMenuItem = new javax.swing.JMenuItem();
        closeImageMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAllMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        settingsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        originalSizeMenuItem = new javax.swing.JMenuItem();
        ocrMenu = new javax.swing.JMenu();
        performOCRMenuItem = new javax.swing.JMenuItem();
        applyChangesMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        openDialog.setAcceptAllFileFilterUsed(false);
        openDialog.setDialogTitle(L10n.forString("title.open")); // NOI18N
        openDialog.setFileFilter(Utils.openFileFilter());
        openDialog.setMultiSelectionEnabled(true);

        saveDialog.setAcceptAllFileFilterUsed(false);
        saveDialog.setDialogTitle(L10n.forString("title.save")); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Juneiform");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        openDocumentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/folder_add.png"))); // NOI18N
        openDocumentButton.setToolTipText(L10n.forString("menu.file.open")); // NOI18N
        openDocumentButton.setFocusable(false);
        openDocumentButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openDocumentButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openDocumentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDocumentButtonActionPerformed(evt);
            }
        });
        toolBar.add(openDocumentButton);

        removeDocumentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete.png"))); // NOI18N
        removeDocumentButton.setToolTipText(L10n.forString("menu.file.close")); // NOI18N
        removeDocumentButton.setFocusable(false);
        removeDocumentButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeDocumentButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeDocumentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDocumentButtonActionPerformed(evt);
            }
        });
        toolBar.add(removeDocumentButton);
        toolBar.add(separator0);

        zoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/magnifier_zoom_in.png"))); // NOI18N
        zoomInButton.setToolTipText(L10n.forString("menu.edit.zoom.in")); // NOI18N
        zoomInButton.setFocusable(false);
        zoomInButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomInButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInButtonActionPerformed(evt);
            }
        });
        toolBar.add(zoomInButton);

        originalSizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/magnifier.png"))); // NOI18N
        originalSizeButton.setToolTipText(L10n.forString("menu.edit.original")); // NOI18N
        originalSizeButton.setFocusable(false);
        originalSizeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        originalSizeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        originalSizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                originalSizeButtonActionPerformed(evt);
            }
        });
        toolBar.add(originalSizeButton);

        zoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/magnifier_zoom_out.png"))); // NOI18N
        zoomOutButton.setToolTipText(L10n.forString("menu.edit.zoom.out")); // NOI18N
        zoomOutButton.setFocusable(false);
        zoomOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutButtonActionPerformed(evt);
            }
        });
        toolBar.add(zoomOutButton);

        rotateLeftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_rotate_anticlockwise.png"))); // NOI18N
        rotateLeftButton.setFocusable(false);
        rotateLeftButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotateLeftButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rotateLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateLeftButtonActionPerformed(evt);
            }
        });
        toolBar.add(rotateLeftButton);

        rotateRightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_rotate_clockwise.png"))); // NOI18N
        rotateRightButton.setFocusable(false);
        rotateRightButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotateRightButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rotateRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateRightButtonActionPerformed(evt);
            }
        });
        toolBar.add(rotateRightButton);

        scanImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/scanner.png"))); // NOI18N
        scanImage.setFocusable(false);
        scanImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        scanImage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        scanImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanImageActionPerformed(evt);
            }
        });
        toolBar.add(scanImage);
        toolBar.add(jSeparator4);

        selectPictureAreaButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/picture.png"))); // NOI18N
        selectPictureAreaButton.setToolTipText(L10n.forString("menu.edit.select.text")); // NOI18N
        selectPictureAreaButton.setFocusable(false);
        selectPictureAreaButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        selectPictureAreaButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        selectPictureAreaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPictureAreaButtonActionPerformed(evt);
            }
        });
        toolBar.add(selectPictureAreaButton);

        deleteOverlaysAreaButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/layer_delete.png"))); // NOI18N
        deleteOverlaysAreaButton.setToolTipText(L10n.forString("menu.edit.delete.overlays")); // NOI18N
        deleteOverlaysAreaButton.setFocusable(false);
        deleteOverlaysAreaButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteOverlaysAreaButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteOverlaysAreaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteOverlaysAreaButtonActionPerformed(evt);
            }
        });
        toolBar.add(deleteOverlaysAreaButton);
        toolBar.add(separator1);

        languageLabel.setText(L10n.forString("msg.language")); // NOI18N
        toolBar.add(languageLabel);

        languageComboBox.setModel(Utils.languageComboBoxModel());
        languageComboBox.setMaximumSize(new java.awt.Dimension(174, 28));
        languageComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageComboBoxActionPerformed(evt);
            }
        });
        toolBar.add(languageComboBox);

        ocrButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/document_inspect.png"))); // NOI18N
        ocrButton.setToolTipText(L10n.forString("menu.ocr.ocr")); // NOI18N
        ocrButton.setFocusable(false);
        ocrButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ocrButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ocrButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ocrButtonActionPerformed(evt);
            }
        });
        toolBar.add(ocrButton);

        applyChangesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/document_editing.png"))); // NOI18N
        applyChangesButton.setToolTipText(L10n.forString("menu.ocr.apply")); // NOI18N
        applyChangesButton.setFocusable(false);
        applyChangesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        applyChangesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        applyChangesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyChangesButtonActionPerformed(evt);
            }
        });
        toolBar.add(applyChangesButton);
        toolBar.add(separator2);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/disk.png"))); // NOI18N
        saveButton.setToolTipText(L10n.forString("menu.file.save")); // NOI18N
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        toolBar.add(saveButton);

        saveAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/disk_multiple.png"))); // NOI18N
        saveAllButton.setToolTipText(L10n.forString("menu.file.save.all")); // NOI18N
        saveAllButton.setFocusable(false);
        saveAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllButtonActionPerformed(evt);
            }
        });
        toolBar.add(saveAllButton);

        getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

        statusBar.setText("Status");
        statusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(statusBar, java.awt.BorderLayout.PAGE_END);

        previewsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        previewsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        previewsScrollPane.setPreferredSize(new java.awt.Dimension(160, 570));

        previewsPanel.setBackground(java.awt.Color.darkGray);
        previewsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, Preview.HORIZONTAL_GAP, Preview.VERTICAL_GAP));
        previewsScrollPane.setViewportView(previewsPanel);

        getContentPane().add(previewsScrollPane, java.awt.BorderLayout.LINE_START);

        splitPane.setDividerLocation(400);
        splitPane.setOneTouchExpandable(true);
        splitPane.setPreferredSize(new java.awt.Dimension(800, 337));

        outputTextArea.setColumns(20);
        outputTextArea.setLineWrap(true);
        outputTextArea.setRows(5);
        outputTextArea.setText(" ");
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setMargin(new java.awt.Insets(4, 4, 4, 4));
        outputScrollPane.setViewportView(outputTextArea);

        splitPane.setRightComponent(outputScrollPane);

        inputPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 4));
        inputScrollPane.setViewportView(inputPanel);

        splitPane.setLeftComponent(inputScrollPane);

        getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

        fileMenu.setText(L10n.forString("menu.file")); // NOI18N

        openImageMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/folder_add.png"))); // NOI18N
        openImageMenuItem.setText(L10n.forString("menu.file.open")); // NOI18N
        openImageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openImageMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openImageMenuItem);

        closeImageMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/delete.png"))); // NOI18N
        closeImageMenuItem.setText(L10n.forString("menu.file.close")); // NOI18N
        closeImageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeImageMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeImageMenuItem);
        fileMenu.add(jSeparator1);

        saveMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/disk.png"))); // NOI18N
        saveMenuItem.setText(L10n.forString("menu.file.save")); // NOI18N
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAllMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/disk_multiple.png"))); // NOI18N
        saveAllMenuItem.setText(L10n.forString("menu.file.save.all")); // NOI18N
        saveAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAllMenuItem);
        fileMenu.add(jSeparator3);

        settingsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/setting_tools.png"))); // NOI18N
        settingsMenuItem.setText(L10n.forString("menu.file.settings")); // NOI18N
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(settingsMenuItem);
        fileMenu.add(jSeparator2);

        quitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/door_open.png"))); // NOI18N
        quitMenuItem.setText(L10n.forString("menu.file.quit")); // NOI18N
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText(L10n.forString("menu.edit")); // NOI18N

        zoomInMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/magnifier_zoom_in.png"))); // NOI18N
        zoomInMenuItem.setText(L10n.forString("menu.edit.zoom.in")); // NOI18N
        zoomInMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(zoomInMenuItem);

        zoomOutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/magnifier_zoom_out.png"))); // NOI18N
        zoomOutMenuItem.setText(L10n.forString("menu.edit.zoom.out")); // NOI18N
        zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(zoomOutMenuItem);

        originalSizeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/magnifier.png"))); // NOI18N
        originalSizeMenuItem.setText(L10n.forString("menu.edit.original")); // NOI18N
        originalSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                originalSizeMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(originalSizeMenuItem);

        menuBar.add(editMenu);

        ocrMenu.setText(L10n.forString("menu.ocr")); // NOI18N

        performOCRMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/document_inspect.png"))); // NOI18N
        performOCRMenuItem.setText(L10n.forString("menu.ocr.ocr")); // NOI18N
        performOCRMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performOCRMenuItemActionPerformed(evt);
            }
        });
        ocrMenu.add(performOCRMenuItem);

        applyChangesMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/document_editing.png"))); // NOI18N
        applyChangesMenuItem.setText(L10n.forString("menu.ocr.apply")); // NOI18N
        applyChangesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyChangesMenuItemActionPerformed(evt);
            }
        });
        ocrMenu.add(applyChangesMenuItem);

        menuBar.add(ocrMenu);

        helpMenu.setText(L10n.forString("menu.help")); // NOI18N

        aboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/small/info_rhombus.png"))); // NOI18N
        aboutMenuItem.setText(L10n.forString("menu.help.about")); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openDocumentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDocumentButtonActionPerformed
        open();
    }//GEN-LAST:event_openDocumentButtonActionPerformed

    private void open() {
        int result = openDialog.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            controller.processOpenedFiles(openDialog.getSelectedFiles());
            openDialog.setSelectedFiles(new File[]{new File("")});
        }
    }

    private void removeDocumentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDocumentButtonActionPerformed
        controller.removeSelectedDocument();
    }//GEN-LAST:event_removeDocumentButtonActionPerformed

    private void ocrButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ocrButtonActionPerformed
        try {
            controller.performOCR();
        } catch (OCRException ex) {
            ErrorDialog errorDialog = new ErrorDialog(this, true);
            errorDialog.setTitle("OCR error");
            errorDialog.setText(ex.getMessage());
            errorDialog.setVisible(true);
        }
    }//GEN-LAST:event_ocrButtonActionPerformed

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        controller.zoomIn();
    }//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        controller.zoomOut();
    }//GEN-LAST:event_zoomOutButtonActionPerformed

    private void originalSizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_originalSizeButtonActionPerformed
        controller.originalSize();
    }//GEN-LAST:event_originalSizeButtonActionPerformed

    private void applyChangesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyChangesButtonActionPerformed
        controller.applyChanges();
    }//GEN-LAST:event_applyChangesButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        save();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void save() {
        int result = saveDialog.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            controller.processSavedFile(saveDialog.getSelectedFile(), saveDialog.getFileFilter().getDescription());
            saveDialog.setSelectedFile(new File(""));
        }
    }

    private void languageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageComboBoxActionPerformed
        if (controller.selectedDocument() != null) {
            controller.selectedDocument().setLanguage(
                    Language.values()[languageComboBox.getSelectedIndex()]);
        }
    }//GEN-LAST:event_languageComboBoxActionPerformed

    private void rotateRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateRightButtonActionPerformed
        controller.rotateRight();
    }//GEN-LAST:event_rotateRightButtonActionPerformed

    private void saveAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllButtonActionPerformed
        saveAll();
    }//GEN-LAST:event_saveAllButtonActionPerformed

    private void saveAll() {
        int result = saveDialog.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            controller.processSavedFiles(saveDialog.getSelectedFile(), saveDialog.getFileFilter().getDescription());
            saveDialog.setSelectedFile(new File(""));
        }
    }

    private void rotateLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateLeftButtonActionPerformed
        controller.rotateLeft();
    }//GEN-LAST:event_rotateLeftButtonActionPerformed

    private void openImageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openImageMenuItemActionPerformed
        open();
    }//GEN-LAST:event_openImageMenuItemActionPerformed

    private void closeImageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeImageMenuItemActionPerformed
        controller.removeSelectedDocument();
    }//GEN-LAST:event_closeImageMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        save();
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void saveAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllMenuItemActionPerformed
        saveAll();
    }//GEN-LAST:event_saveAllMenuItemActionPerformed

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        dispose();
        System.exit(0);
    }//GEN-LAST:event_quitMenuItemActionPerformed

    private void zoomOutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutMenuItemActionPerformed
        controller.zoomOut();
    }//GEN-LAST:event_zoomOutMenuItemActionPerformed

    private void performOCRMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performOCRMenuItemActionPerformed
        try {
            controller.performOCR();
        } catch (OCRException ex) {
            ErrorDialog errorDialog = new ErrorDialog(this, true);
            errorDialog.setTitle(L10n.forString("msg.error"));
            errorDialog.setText(ex.getMessage());
            errorDialog.setVisible(true);
        }
    }//GEN-LAST:event_performOCRMenuItemActionPerformed

    private void applyChangesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyChangesMenuItemActionPerformed
        controller.applyChanges();
    }//GEN-LAST:event_applyChangesMenuItemActionPerformed

    private void zoomInMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInMenuItemActionPerformed
        controller.zoomIn();
    }//GEN-LAST:event_zoomInMenuItemActionPerformed

    private void originalSizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_originalSizeMenuItemActionPerformed
        controller.originalSize();
    }//GEN-LAST:event_originalSizeMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        aboutBox.setModal(true);
        aboutBox.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuItemActionPerformed
        settingsWindow.setModal(true);
        settingsWindow.setVisible(true);
    }//GEN-LAST:event_settingsMenuItemActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        languageComboBox.setSelectedIndex(settings.getRecognitionLanguageId());
    }//GEN-LAST:event_formWindowActivated

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        dispose();
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    private void selectPictureAreaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPictureAreaButtonActionPerformed
        drawState = OverlayType.PICTURE;
        drawingPanel.setOverlayType(drawState);
    }//GEN-LAST:event_selectPictureAreaButtonActionPerformed

    private void scanImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanImageActionPerformed
        controller.processScanImage();
    }//GEN-LAST:event_scanImageActionPerformed

    private void deleteOverlaysAreaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteOverlaysAreaButtonActionPerformed
        controller.deleteOverlays();
    }//GEN-LAST:event_deleteOverlaysAreaButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JMenuItem aboutMenuItem;
    javax.swing.JButton applyChangesButton;
    javax.swing.JMenuItem applyChangesMenuItem;
    javax.swing.JMenuItem closeImageMenuItem;
    javax.swing.JButton deleteOverlaysAreaButton;
    javax.swing.JMenu editMenu;
    javax.swing.JMenu fileMenu;
    javax.swing.JMenu helpMenu;
    javax.swing.JPanel inputPanel;
    javax.swing.JScrollPane inputScrollPane;
    javax.swing.JPopupMenu.Separator jSeparator1;
    javax.swing.JPopupMenu.Separator jSeparator2;
    javax.swing.JPopupMenu.Separator jSeparator3;
    javax.swing.JToolBar.Separator jSeparator4;
    javax.swing.JComboBox languageComboBox;
    javax.swing.JLabel languageLabel;
    javax.swing.JMenuBar menuBar;
    javax.swing.JButton ocrButton;
    javax.swing.JMenu ocrMenu;
    javax.swing.JFileChooser openDialog;
    javax.swing.JButton openDocumentButton;
    javax.swing.JMenuItem openImageMenuItem;
    javax.swing.JButton originalSizeButton;
    javax.swing.JMenuItem originalSizeMenuItem;
    javax.swing.JScrollPane outputScrollPane;
    javax.swing.JTextArea outputTextArea;
    javax.swing.JMenuItem performOCRMenuItem;
    javax.swing.JPanel previewsPanel;
    javax.swing.JScrollPane previewsScrollPane;
    javax.swing.JMenuItem quitMenuItem;
    javax.swing.JButton removeDocumentButton;
    javax.swing.JButton rotateLeftButton;
    javax.swing.JButton rotateRightButton;
    javax.swing.JButton saveAllButton;
    javax.swing.JMenuItem saveAllMenuItem;
    javax.swing.JButton saveButton;
    javax.swing.JFileChooser saveDialog;
    javax.swing.JMenuItem saveMenuItem;
    javax.swing.JButton scanImage;
    javax.swing.JButton selectPictureAreaButton;
    javax.swing.JToolBar.Separator separator0;
    javax.swing.JToolBar.Separator separator1;
    javax.swing.JToolBar.Separator separator2;
    javax.swing.JMenuItem settingsMenuItem;
    javax.swing.JSplitPane splitPane;
    javax.swing.JLabel statusBar;
    javax.swing.JToolBar toolBar;
    javax.swing.JButton zoomInButton;
    javax.swing.JMenuItem zoomInMenuItem;
    javax.swing.JButton zoomOutButton;
    javax.swing.JMenuItem zoomOutMenuItem;
    // End of variables declaration//GEN-END:variables
}
