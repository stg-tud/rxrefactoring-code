/*******************************************************************************
 * Copyright 2012 Andreas Reichart. Distributed under the terms of the GNU General Public License.
 * 
 * This file is part of DeExifier.
 * 
 * DeExifier is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DeExifier is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DeExifier. If not,
 * see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package reichart.andreas.deexifier;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class DeExifierGUI extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -7527819461571225138L;
    private static final String VERSION = "Version 1.1";
    private JMenu mnFile;
    private JMenu mnEdit;
    private JButton btnAdd;
    private JButton btnRemove;
    private JButton buttonPlay;
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private JCheckBox chkBoxRemoveIPTC;
    private JCheckBox chkBoxRemoveExif;
    private JButton btnPrefs;
    private JButton button;
    private JRadioButton chkBoxScaleNone;
    private JRadioButton chkBoxScaleRelative;
    private JRadioButton chkBoxScaleFixed;
    private JTextField txtFieldFixed;
    private JTextField txtFieldScaling;
    private JLabel labelScalingPercent;
    private JLabel labelScalingPixel;
    private JLabel labelWidth;
    private JLabel lblScaling;
    private JLabel statusBar;
    private JSlider qualitySlider;
    private JCheckBox chkBoxRename;
    private JCheckBox chkBoxRenamePath;
    private JTextField txtFieldSuffix;
    private JTextField txtFieldPath;
    private JButton savePathButton;
    private File savePath;
    private ArrayList<File> files;
    private JProgressBar progressBar;
    private JLabel lblStatus = new JLabel();;
    private JMenuItem mnitemAddFile;
    private JMenuItem mnitemRemoveFile;
    private JMenuItem mnitemProcessFiles;
    private JMenuItem mnitemAbout;
    private JCheckBox chkBoxRecompress;
    private JTextField txtFieldQuality;
    private int qualityValue;
    private String[] jpgSuffix = { "jpg", "jpeg" }; // use .ignoreCase!

    /**
     * Launch the application.
     * 
     * @param args
     */
    public static void main(String[] args) {
	try {
	    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
	    // System.setProperty("awt.useSystemAAFontSettings", "on");
	    // System.setProperty("swing.aatext", "true");

	} catch (Throwable e) {
	    e.printStackTrace();
	}

	EventQueue.invokeLater(new Runnable() {
	    public void run() {

		try {
		    DeExifierGUI frame = new DeExifierGUI();
		    frame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Create the frame.
     */
    public DeExifierGUI() {
	files = new ArrayList<File>(); // init the ArrayList where wo keep the files
	list = new JList<String>();
	listModel = new DefaultListModel<String>();

	initGUI();

    }

    /**
     * Init the GUI
     */
    private void initGUI() {

	UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Ubuntu", Font.PLAIN, 10));
	Font defaultFont = new Font("Arial", Font.PLAIN, 11);
	Font lucidaFont = new Font("Lucida Sans", Font.PLAIN, 11);

	setBounds(new Rectangle(30, 30, 456, 540));
	setTitle("DeExifier by Andreas Reichart");
	// setFont(new Font("Bangwool", Font.PLAIN, 12));

	setResizable(false);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	getContentPane().setLayout(null);

	JPanel mainPanel = new JPanel();
	mainPanel.setBounds(6, 6, 450, 530);
	getContentPane().add(mainPanel);
	mainPanel.setLayout(null);

	JMenuBar menuBar = new JMenuBar();
	menuBar.setBounds(0, 0, 445, 23);
	mainPanel.add(menuBar);

	mnFile = new JMenu("File");
	menuBar.add(mnFile);

	mnitemAddFile = new JMenuItem("Add File");
	mnFile.add(mnitemAddFile);
	mnitemAddFile.addActionListener(menuButtonListener);

	mnitemRemoveFile = new JMenuItem("Remove File");
	mnFile.add(mnitemRemoveFile);
	mnitemRemoveFile.addActionListener(menuButtonListener);

	mnEdit = new JMenu("Edit");
	menuBar.add(mnEdit);

	mnitemProcessFiles = new JMenuItem("Process Files");
	mnEdit.add(mnitemProcessFiles);
	mnitemProcessFiles.addActionListener(menuButtonListener);

	JMenu mnHelp = new JMenu("Help");
	menuBar.add(mnHelp);

	mnitemAbout = new JMenuItem("About DeExifier");
	mnHelp.add(mnitemAbout);
	mnitemAbout.addActionListener(menuButtonListener);

	JToolBar toolBar = new JToolBar();
	toolBar.setDoubleBuffered(true);
	toolBar.setBorderPainted(false);
	toolBar.setFloatable(false);
	toolBar.setBounds(0, 25, 445, 50);
	mainPanel.add(toolBar);

	btnAdd = new JButton("");
	btnAdd.setContentAreaFilled(true);
	btnAdd.setBorderPainted(false);
	btnAdd.setToolTipText("Add images");
	btnAdd.setIcon(new ImageIcon(getClass().getResource("/picBtnJpegAdd.png")));
	btnAdd.setPreferredSize(new Dimension(50, 49));
	btnAdd.setBounds(new Rectangle(0, 0, 0, 50));
	toolBar.add(btnAdd);
	btnAdd.addActionListener(menuButtonListener);

	btnRemove = new JButton("");
	btnRemove.setIcon(new ImageIcon(getClass().getResource("/picBtnJpegRem.png")));
	btnRemove.setToolTipText("Remove Images");
	btnRemove.setPreferredSize(new Dimension(50, 49));
	btnRemove.setContentAreaFilled(true);
	btnRemove.setBounds(new Rectangle(0, 0, 0, 50));
	btnRemove.setBorderPainted(false);
	btnRemove.addActionListener(menuButtonListener);
	toolBar.add(btnRemove);

	buttonPlay = new JButton("");
	buttonPlay.setIcon(new ImageIcon(getClass().getResource("/btnPlay.png")));
	buttonPlay.setToolTipText("Start the process");
	buttonPlay.setPreferredSize(new Dimension(50, 49));
	buttonPlay.setContentAreaFilled(true);
	buttonPlay.setBounds(new Rectangle(0, 0, 0, 50));
	buttonPlay.setBorderPainted(false);
	buttonPlay.addActionListener(menuButtonListener);
	toolBar.add(buttonPlay);

	button = new JButton("");
	button.setEnabled(false);
	button.setFocusPainted(false);
	button.setFocusTraversalKeysEnabled(false);
	button.setFocusable(false);
	button.setRequestFocusEnabled(false);
	button.setRolloverEnabled(false);
	button.setVerifyInputWhenFocusTarget(false);
	button.setMaximumSize(new Dimension(200, 12));
	button.setMinimumSize(new Dimension(200, 12));
	button.setPreferredSize(new Dimension(200, 12));
	button.setBorderPainted(false);
	toolBar.add(button);

	btnPrefs = new JButton("");
	btnPrefs.setIcon(new ImageIcon(getClass().getResource("/btnPrefs.png")));
	btnPrefs.setToolTipText("Preferences");
	btnPrefs.setPreferredSize(new Dimension(50, 49));
	btnPrefs.setContentAreaFilled(false);
	btnPrefs.setBounds(new Rectangle(0, 0, 0, 50));
	btnPrefs.setBorderPainted(false);
	btnPrefs.setEnabled(false);
	toolBar.add(btnPrefs);

	list.setModel(listModel);

	JScrollPane scrollPane = new JScrollPane(list);
	scrollPane.setBounds(0, 76, 445, 259);
	mainPanel.add(scrollPane);

	@SuppressWarnings("unused")
	DropTarget scrollPaneDropTarget = new DropTarget(scrollPane, dropTargetListener);

	// FileTransferHandler handler = new FileTransferHandler();
	// mainPanel.setTransferHandler(handler);

	JPanel panel = new JPanel();
	panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	panel.setDoubleBuffered(false);
	panel.setFont(new Font("Lucida Sans", Font.PLAIN, 10));
	panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Remove Entries",
		TitledBorder.LEADING, TitledBorder.TOP, null, null));
	panel.setBounds(5, 345, 115, 65);
	mainPanel.add(panel);
	panel.setLayout(new GridLayout(0, 1, 0, 0));

	chkBoxRemoveIPTC = new JCheckBox("IPTC");
	chkBoxRemoveIPTC.setFont(new Font("Lucida Sans", Font.PLAIN, 11));
	chkBoxRemoveIPTC.setToolTipText("Remove all IPTC entries");
	chkBoxRemoveIPTC.setSelected(false);
	panel.add(chkBoxRemoveIPTC);

	chkBoxRemoveExif = new JCheckBox("Exif");
	chkBoxRemoveExif.setFont(new Font("Lucida Sans", Font.PLAIN, 11));
	chkBoxRemoveExif.setToolTipText("Remove all Exif entries");
	chkBoxRemoveExif.setSelected(true);
	// TODO: for the moment we can remove Exif only ...
	chkBoxRemoveExif.setEnabled(false);
	chkBoxRemoveIPTC.setEnabled(false);
	String milestoneTooltip = "For this milestone only the removement of Exif data is supported.";
	panel.setToolTipText(milestoneTooltip);
	chkBoxRemoveExif.setToolTipText(milestoneTooltip);
	chkBoxRemoveIPTC.setToolTipText(milestoneTooltip);
	// END
	panel.add(chkBoxRemoveExif);

	JPanel panelFileRename = new JPanel();
	panelFileRename.setFont(new Font("Lucida Sans", Font.PLAIN, 10));
	panelFileRename.setDoubleBuffered(false);
	panelFileRename.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	panelFileRename.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Rename Files",
		TitledBorder.LEADING, TitledBorder.TOP, null, null));
	panelFileRename.setBounds(5, 416, 205, 77);
	mainPanel.add(panelFileRename);
	panelFileRename.setLayout(null);

	chkBoxRename = new JCheckBox("Add Suffix");
	chkBoxRename.addActionListener(enableListener);
	chkBoxRename.setBounds(6, 17, 80, 27);
	chkBoxRename.setToolTipText("Add a suffix to the end of the filename");
	chkBoxRename.setFont(new Font("Lucida Sans", Font.PLAIN, 11));
	chkBoxRename.setSelected(true);
	panelFileRename.add(chkBoxRename);

	txtFieldSuffix = new JTextField("-deEx");
	txtFieldSuffix.setBounds(93, 17, 102, 27);
	panelFileRename.add(txtFieldSuffix);
	txtFieldSuffix.setColumns(10);

	txtFieldPath = new JTextField();
	txtFieldPath.setColumns(10);
	txtFieldPath.setBounds(55, 43, 115, 27);
	panelFileRename.add(txtFieldPath);
	txtFieldPath.setEnabled(false);

	chkBoxRenamePath = new JCheckBox("Path");
	chkBoxRenamePath.setBounds(6, 44, 54, 27);
	chkBoxRenamePath.setFont(new Font("Lucida Sans", Font.PLAIN, 11));
	chkBoxRenamePath.setToolTipText("Destination diretory");
	chkBoxRenamePath.addActionListener(enableListener);
	panelFileRename.add(chkBoxRenamePath);

	savePathButton = new JButton("");
	savePathButton.setBounds(175, 43, 20, 27);
	panelFileRename.add(savePathButton);
	savePathButton.setEnabled(false);
	savePathButton.addActionListener(pathButtonListener);

	JPanel panelScaling = new JPanel();
	panelScaling.setFont(new Font("Lucida Sans", Font.PLAIN, 10));
	panelScaling.setDoubleBuffered(false);
	panelScaling.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	panelScaling.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Scaling",
		TitledBorder.LEADING, TitledBorder.TOP, null, null));
	panelScaling.setBounds(211, 345, 230, 100);
	mainPanel.add(panelScaling);

	statusBar = new JLabel();
	statusBar.setBounds(6, 505, 434, 20);
	statusBar.setBackground(new Color(220, 220, 220));
	statusBar.setFont(new Font("Lucida Sans", Font.PLAIN, 10));
	statusBar.setDoubleBuffered(false);
	statusBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	statusBar.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

	mainPanel.add(statusBar);

	ButtonGroup scaleButtonGroup = new ButtonGroup();
	panelScaling.setLayout(null);

	labelWidth = new JLabel("Width");
	labelWidth.setFont(lucidaFont);
	labelWidth.setBounds(91, 66, 50, 25);
	panelScaling.add(labelWidth);

	txtFieldFixed = new JTextField();
	txtFieldFixed.setBounds(141, 61, 60, 27);
	panelScaling.add(txtFieldFixed);
	txtFieldFixed.setColumns(10);
	txtFieldFixed.setFont(defaultFont);

	chkBoxScaleNone = new JRadioButton("None");
	chkBoxScaleNone.setSelected(true);
	chkBoxScaleNone.setBounds(9, 18, 70, 25);
	chkBoxScaleNone.setToolTipText("No scaling");
	chkBoxScaleNone.setFont(lucidaFont);
	chkBoxScaleNone.addActionListener(scalingListener);
	panelScaling.add(chkBoxScaleNone);
	scaleButtonGroup.add(chkBoxScaleNone);

	chkBoxScaleRelative = new JRadioButton("Relative");
	chkBoxScaleRelative.setBounds(9, 42, 70, 25);
	chkBoxScaleRelative.setToolTipText("Scale to a defined percentage: values 1-100");
	chkBoxScaleRelative.setFont(lucidaFont);
	chkBoxScaleRelative.addActionListener(scalingListener);

	labelScalingPercent = new JLabel("%");
	labelScalingPercent.setBounds(205, 42, 20, 15);
	labelScalingPercent.setFont(lucidaFont);
	panelScaling.add(labelScalingPercent);
	panelScaling.add(chkBoxScaleRelative);
	scaleButtonGroup.add(chkBoxScaleRelative);

	chkBoxScaleFixed = new JRadioButton("Fixed");
	chkBoxScaleFixed.setBounds(9, 66, 70, 25);
	chkBoxScaleFixed.setToolTipText("Scale to a defined width");
	chkBoxScaleFixed.setFont(lucidaFont);
	chkBoxScaleFixed.addActionListener(scalingListener);
	panelScaling.add(chkBoxScaleFixed);
	scaleButtonGroup.add(chkBoxScaleFixed);

	lblScaling = new JLabel("Scaling");
	lblScaling.setBounds(91, 42, 50, 25);
	lblScaling.setFont(lucidaFont);
	panelScaling.add(lblScaling);

	txtFieldScaling = new JTextField();
	txtFieldScaling.setColumns(10);
	txtFieldScaling.setBounds(141, 37, 60, 27);
	txtFieldScaling.setFont(defaultFont);
	txtFieldScaling.addFocusListener(rescaleListener);
	panelScaling.add(txtFieldScaling);

	labelScalingPixel = new JLabel("px");
	labelScalingPixel.setFont(lucidaFont);
	labelScalingPixel.setBounds(205, 66, 20, 15);
	panelScaling.add(labelScalingPixel);
	chkBoxScaleNone.doClick();

	JPanel panelCompression = new JPanel();
	panelCompression.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
		"Re-Compression & Quality", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	panelCompression.setBounds(211, 448, 230, 45);

	mainPanel.add(panelCompression);
	panelCompression.setLayout(null);
	String qualityTooltip = "<html>Values 1-10<br>High value = High quality, Larger image<br><br>Always enabled when scaling images.<br>With no scaling requested recompression is optional.</html>";

	chkBoxRecompress = new JCheckBox();
	chkBoxRecompress.setBounds(10, 16, 20, 21);
	chkBoxRecompress.addActionListener(enableListener);
	chkBoxRecompress.setSelected(false);
	chkBoxRecompress.setToolTipText(qualityTooltip);
	panelCompression.add(chkBoxRecompress);

	txtFieldQuality = new JTextField(4);
	txtFieldQuality.setBounds(170, 12, 40, 27);
	txtFieldQuality.setHorizontalAlignment(JTextField.RIGHT);
	txtFieldQuality.setEnabled(false);
	panelCompression.add(txtFieldQuality);

	qualitySlider = new JSlider();
	qualitySlider.setMinimum(1);
	qualitySlider.setMaximum(100);
	qualitySlider.setBounds(40, 16, 120, 21);
	qualitySlider.setEnabled(false);
	qualitySlider.setToolTipText(qualityTooltip);
	panelCompression.add(qualitySlider);
	qualitySlider.addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(ChangeEvent arg0) {
		JSlider changedSlider = (JSlider) arg0.getSource();
		qualityValue = changedSlider.getValue();
		txtFieldQuality.setText(Integer.toString(qualityValue));
	    }
	});

	txtFieldQuality.addFocusListener(qualityListener);
	txtFieldQuality.setText("75");
	qualitySlider.setValue(75);

	progressBar = new JProgressBar();
	progressBar.setBounds(0, 1, 200, 16);
	progressBar.setVisible(false);
	statusBar.add(progressBar);

	lblStatus.setFont(new Font("DejaVu Sans", Font.PLAIN, 9));
	lblStatus.setBounds(212, 3, 214, 15);
	statusBar.add(lblStatus);

    }

    /**
     * Listener for changes in the scaleButtonGroup
     */
    ActionListener scalingListener = new ActionListener() {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    clearStatusLabel();
	    JRadioButton clickedBox = (JRadioButton) arg0.getSource();
	    if (clickedBox.equals(chkBoxScaleNone)) {
		lblScaling.setEnabled(false);
		labelWidth.setEnabled(false);
		txtFieldFixed.setEnabled(false);
		txtFieldScaling.setEnabled(false);
	    } else if (clickedBox.equals(chkBoxScaleRelative)) {
		lblScaling.setEnabled(true);
		labelWidth.setEnabled(false);
		txtFieldFixed.setEnabled(false);
		txtFieldScaling.setEnabled(true);
		chkBoxRecompress.setSelected(true);
		qualitySlider.setEnabled(true);
		txtFieldQuality.setEnabled(true);
	    } else if (clickedBox.equals(chkBoxScaleFixed)) {
		lblScaling.setEnabled(false);
		labelWidth.setEnabled(true);
		txtFieldFixed.setEnabled(true);
		txtFieldScaling.setEnabled(false);
		chkBoxRecompress.setSelected(true);
		qualitySlider.setEnabled(true);
		txtFieldQuality.setEnabled(true);
	    }
	}
    };

    /**
     * ActionListener for some clicks on JCheckBoxes
     */
    ActionListener enableListener = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent arg0) {
	    JCheckBox clickedCheckBox = (JCheckBox) arg0.getSource();
	    if (clickedCheckBox.equals(chkBoxRename)) {
		if (chkBoxRename.isSelected()) {
		    txtFieldSuffix.setEnabled(true);
		} else
		    txtFieldSuffix.setEnabled(false);
	    }
	    if (clickedCheckBox.equals(chkBoxRenamePath)) {
		if (chkBoxRenamePath.isSelected()) {
		    txtFieldPath.setEnabled(true);
		    savePathButton.setEnabled(true);
		} else {
		    txtFieldPath.setEnabled(false);
		    savePathButton.setEnabled(false);
		}
	    }
	    if (clickedCheckBox.equals(chkBoxRecompress)) {
		if (chkBoxRecompress.isSelected()) {
		    txtFieldQuality.setEnabled(true);
		    qualitySlider.setEnabled(true);
		} else {
		    if (chkBoxScaleNone.isSelected()) {
			txtFieldQuality.setEnabled(false);
			qualitySlider.setEnabled(false);
		    } else {
			chkBoxRecompress.setSelected(true);
		    }
		}
	    }
	}
    };

    /**
     * FocusListener for changes in the JTextField txtFieldQuality
     */
    FocusListener qualityListener = new FocusListener() {
	@Override
	public void focusLost(FocusEvent arg0) {
	    JTextField updatedTextField = (JTextField) arg0.getSource();
	    String updatedTextFieldString = updatedTextField.getText();
	    try {
		int updatedQuality = Integer.parseInt(updatedTextFieldString);
		if (updatedQuality >= 0 && updatedQuality < 101) {
		    statusBar.setText("");
		    qualityValue = updatedQuality;
		    qualitySlider.setValue(qualityValue);
		} else
		    setErrorMessage();
	    } catch (NumberFormatException nfExc) {
		setErrorMessage();
	    }

	}

	@Override
	public void focusGained(FocusEvent arg0) {
	}

	private void setErrorMessage() {
	    // buttonPlay.setEnabled(false);
	    statusBar.setText("Valid values range from 0-100. Default value = 75.");
	    qualitySlider.setValue(75);
	    qualityValue = 75;
	}
    };

    /**
     * FocusListener to check if the rescaleValues are within 1-100
     */
    FocusListener rescaleListener = new FocusListener() {

	@Override
	public void focusLost(FocusEvent arg0) {
	    JTextField lostTextField = (JTextField) arg0.getSource();
	    String updatedString = lostTextField.getText();
	    if (updatedString.equals(""))
		return;

	    int updatedIntValue = 0;
	    try {
		updatedIntValue = Integer.parseInt(updatedString);
	    } catch (NumberFormatException e) {
		errorMessage();
	    }
	    if ((updatedIntValue > 0 && updatedIntValue < 101)) {
		statusBar.setText("");

	    } else {
		errorMessage();
	    }
	}

	@Override
	public void focusGained(FocusEvent arg0) {
	    // we do nothing
	}

	private void errorMessage() {
	    // TODO: make the error more visible ... blink or whatever!
	    String errorMessage = "Valid scaling values range from 1-100";
	    statusBar.setText(errorMessage);
	    txtFieldScaling.setText("");

	}
    };

    /**
     * ActionListener for selecting destination path
     */
    ActionListener pathButtonListener = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent arg0) {
	    JFileChooser chooser;
	    chooser = new JFileChooser();
	    chooser.setDialogTitle("Select destination directory");
	    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);

	    if (chooser.showOpenDialog(savePathButton) == JFileChooser.APPROVE_OPTION) {
		savePath = chooser.getSelectedFile();
		txtFieldPath.setText(savePath.toString());
	    }
	}
    };

    private boolean isJpeg(File file) {
	// String suffixSeparator = System.getProperty("files.separator");
	String extensionShort = file.getName().substring(file.getName().length() - 3);
	String extensionLong = file.getName().substring(file.getName().length() - 4);

	for (String string : jpgSuffix) {
	    if (string.equalsIgnoreCase(extensionShort) || string.equals(extensionLong))
		return true;
	}
	return false;
    }

    private void clearStatusLabel() {
	lblStatus.setText("");
    }

    /**
     * ActionListener for add, remove, process and prefs buttons/menuItems
     */
    ActionListener menuButtonListener = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent arg0) {
	    Component clickedButton = (Component) arg0.getSource();

	    if (clickedButton.equals(btnAdd) || clickedButton.equals(mnitemAddFile)) {

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choose files to deExify");
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(new FileFilter() {

		    @Override
		    public String getDescription() {
			return "JPEG Files";
		    }

		    @Override
		    public boolean accept(File arg0) {
			if (arg0.isDirectory())
			    return true;
			return isJpeg(arg0);
		    }
		});

		if (chooser.showOpenDialog(btnAdd) == JFileChooser.APPROVE_OPTION) {
		    File[] chosenFiles = chooser.getSelectedFiles();
		    for (File f : chosenFiles) {
			listModel.addElement(f.getAbsolutePath().toString()); // ListModel
			files.add(f); // add File to the FileArrayList
		    }
		    list.setModel(listModel); // update the List with the changed ListModel
		}
	    }

	    if (clickedButton.equals(buttonPlay) || clickedButton.equals(mnitemProcessFiles)) {
		if (!chkBoxRenamePath.isSelected())
		    // if no savePath is selected, we use the path of our first file!
		    savePath = files.get(0).getParentFile();
		Remover remover = new Remover();
		String addSuffix;
		if (chkBoxRename.isSelected()) {
		    addSuffix = txtFieldSuffix.getText();
		} else {
		    addSuffix = "";
		}

		boolean recompress = false;
		if (chkBoxRecompress.isSelected()) {
		    recompress = true;
		}

		remover.setParams(files, savePath, qualityValue, recompress, addSuffix, list, progressBar, lblStatus);
		if (chkBoxScaleFixed.isSelected()) {
		    remover.setWidth(Integer.parseInt(txtFieldFixed.getText()));
		} else if (chkBoxScaleRelative.isSelected() ) {
		    remover.setScale(Integer.parseInt(txtFieldScaling.getText()));
		}

		remover.setRemoveOptions(chkBoxRemoveExif.isSelected(), chkBoxRemoveIPTC.isSelected());

		// progressBar.setMaximum(files.size());

			//RxRefactoring: the PropertyChangeListener cannot longer be used
//		remover.addPropertyChangeListener(new PropertyChangeListener() {
//
//		    @Override
//		    public void propertyChange(PropertyChangeEvent arg0) {
//			if (arg0.getPropertyName().equals("state")) {
//			    if (arg0.getNewValue().equals(StateValue.STARTED)) {
//				progressBar.setVisible(true);
//			    } else {
//				progressBar.setVisible(false);
//				progressBar.setValue(0);
//			    }
//			}
//			if (arg0.getPropertyName().equals("progress")) {
//			    progressBar.setValue((int) arg0.getNewValue());
//			}
//
//		    }
//		});
			// RxRefactoring: start async task with subscribe instead of execute
		remover.createRxObservable().subscribe();
	    }

	    if (clickedButton.equals(btnPrefs)) {
		// TODO Preferences button and preferences menu
	    }

	    if (clickedButton.equals(mnitemAbout)) {
		AboutJFrame aboutFrame = new AboutJFrame(VERSION);
		aboutFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		aboutFrame.setVisible(true);
	    }

	    if (clickedButton.equals(btnRemove) || clickedButton.equals(mnitemRemoveFile)) {
		if (!list.isSelectionEmpty()) {
		    int[] selectedIndices = list.getSelectedIndices();
		    for (int i : selectedIndices) {
			listModel.remove(i);
		    }
		    list.clearSelection();
		}
	    }
	}

    };

    DropTargetListener dropTargetListener = new DropTargetListener() {
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	    // nothing
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
	    dtde.acceptDrop(DnDConstants.ACTION_COPY); // accept copy drop
	    Transferable tr = dtde.getTransferable();
	    DataFlavor[] flavors = tr.getTransferDataFlavors();

	    for (DataFlavor flavor : flavors) { // check all flavors
		if (flavor.isFlavorJavaFileListType()) { // check if one of the flavors is
							 // FileListType

		    List<File> addedFiles = null;
		    try {
			addedFiles = ((List<File>) tr.getTransferData(flavor));
		    } catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		    }

		    for (int i = 0; i < addedFiles.size(); i++) {
			if (new MimetypesFileTypeMap().getContentType(addedFiles.get(i)).equalsIgnoreCase(
				"application/octet-stream")) {
			    if (isJpeg(addedFiles.get(i))) {
				listModel.addElement(((File) addedFiles.get(i)).getAbsolutePath().toString());
				files.add(addedFiles.get(i));
				list.setModel(listModel);
			    }
			}
		    }
		}
	    }
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	    // nothing

	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	    // nothing

	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	    // nothing

	}
    };

}
