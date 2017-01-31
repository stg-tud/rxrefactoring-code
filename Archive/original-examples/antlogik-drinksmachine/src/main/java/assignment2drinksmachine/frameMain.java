/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * This is the GUI.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class frameMain extends JFrame {

    private JPanel leftPanel, rightPanel, buttonsPanel, cancelPanel, refundPanel;
    private JPanel coinsPanel, trayPanel, outputPanel, mainPanel;
    private JButton btnDrink1, btnDrink2, btnDrink3, btnCancel, btnTray;
    private JButton kr1, kr2, kr5, kr10, kr20;
    private JLabel lblDrink1, lblDrink2, lblDrink3, lblRefund;
    private JTextArea txtOutput;
    private ImageIcon bgImage;
    private GridBagConstraints cButtonsPanel, cLeftPanel, cRightPanel, cCancelPanel;
    private GridBagConstraints cCoinsPanel, cTrayPanel, cOutputPanel, cRefundPanel;
    private GridBagConstraints cBtnDrink1, cBtnDrink2, cBtnDrink3, cBtnCancel;
    private GridBagConstraints cLblDrink1, cLblDrink2, cLblDrink3, cTxtOutput;
    private GridBagConstraints cBtnTray, cLblRefund, cMainPanel;
    private GridBagConstraints cKr1, cKr2, cKr5, cKr10, cKr20;
    private Cursor cursorHand = new Cursor(Cursor.HAND_CURSOR);
    private Container cp;
    private JScrollPane scrollPane;
    private Toolkit toolkit;

    /**
     * All the GUI elements are created and arranged in the constructor.
     */
    public frameMain() {
        /*
         * Due to Windows and Mac OS X having different designs in their frames,
         * we need to differ the sizes for the layout to be the same in both cases.
         * Windows have thick borders on their frames, while OS X dont have any.
         */
        String osName = System.getProperty("os.name");
        if (osName.indexOf("Windows") != -1) {
            if (osName.indexOf("XP") != -1) {
                this.setSize(510, 418);
            } else {
                this.setSize(515, 428);
            }
        } else {
            this.setSize(500, 400);
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new GridBagLayout());
        //this.setResizable(false);
        cp = this.getContentPane();
        cp.setBackground(Color.gray);

        toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        setLocation((size.width - getWidth()) / 2, (size.height - getHeight()) / 2);


        /*
         * JPanels
         */

        bgImage = new ImageIcon(getClass().getResource("/images/bg.png"));

        mainPanel = new JPanel(new GridBagLayout()) {

            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(bgImage.getImage(), 0, 0, null);

                Dimension d = getSize();
                g.drawImage(bgImage.getImage(), 0, 0, d.width, d.height, null);

                Point p = scrollPane.getViewport().getViewPosition();
                g.drawImage(bgImage.getImage(), p.x, p.y, null);

                super.paintComponent(g);
            }
        }/**/;
        mainPanel.setOpaque(false);
        mainPanel.setPreferredSize(new Dimension(500, 390));
        mainPanel.setBackground(Color.white);
        scrollPane = new JScrollPane(mainPanel);

        leftPanel = new JPanel(new GridBagLayout());
        rightPanel = new JPanel(new GridBagLayout());
        buttonsPanel = new JPanel(new GridBagLayout());
        cancelPanel = new JPanel(new GridBagLayout());
        coinsPanel = new JPanel(new GridBagLayout());
        trayPanel = new JPanel(new GridBagLayout());
        outputPanel = new JPanel(new GridBagLayout());
        refundPanel = new JPanel(new GridBagLayout());

        leftPanel.setOpaque(false);
        rightPanel.setOpaque(false);
        buttonsPanel.setOpaque(false);
        outputPanel.setOpaque(false);
        coinsPanel.setOpaque(false);
        refundPanel.setOpaque(false);
        trayPanel.setOpaque(false);
        cancelPanel.setOpaque(false);/**/

        /*//These colours are used for easy layout overview
        leftPanel.setBackground(Color.blue);
        rightPanel.setBackground(Color.red);
        buttonsPanel.setBackground(Color.green);
        cancelPanel.setBackground(Color.yellow);
        coinsPanel.setBackground(Color.orange);
        trayPanel.setBackground(Color.cyan);
        outputPanel.setBackground(Color.magenta);
        refundPanel.setBackground(Color.pink);/**/

        /*
         * Components
         */

        btnDrink1 = new JButton(DrinksMachineApp.drinks[0]);
        btnDrink1.setCursor(cursorHand);
        btnDrink1.setPreferredSize(new Dimension(100, 25));
        btnDrink1.setMargin(new Insets(2, 1, 2, 1));
        btnDrink1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.selection(1);
            }
        });
        btnDrink2 = new JButton(DrinksMachineApp.drinks[1]);
        btnDrink2.setCursor(cursorHand);
        btnDrink2.setPreferredSize(new Dimension(100, 25));
        btnDrink2.setMargin(new Insets(2, 1, 2, 1));
        btnDrink2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.selection(2);
            }
        });
        btnDrink3 = new JButton(DrinksMachineApp.drinks[2]);
        btnDrink3.setCursor(cursorHand);
        btnDrink3.setPreferredSize(new Dimension(100, 25));
        btnDrink3.setMargin(new Insets(2, 1, 2, 1));
        btnDrink3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.selection(3);
            }
        });
        btnCancel = new JButton("Cancel");
        btnCancel.setCursor(cursorHand);
        btnCancel.setPreferredSize(new Dimension(135, 25));
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.cancel();
            }
        });

        btnTray = new JButton(new ImageIcon(getClass().getResource("/images/tray_closed.png")));
        btnTray.setCursor(cursorHand);
        btnTray.setPreferredSize(new Dimension(165, 255));
        btnTray.setBorderPainted(false);
        btnTray.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.drinkTaken();
            }
        });

        kr1 = new JButton(new ImageIcon(getClass().getResource("/images/1kr.png")));
        kr1.setContentAreaFilled(false);
        kr1.setPreferredSize(new Dimension(46, 46));
        kr1.setBorderPainted(false);
        kr1.setCursor(cursorHand);
        kr1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.inputCoin(1);
            }
        });

        kr2 = new JButton(new ImageIcon(getClass().getResource("/images/2kr.png")));
        kr2.setContentAreaFilled(false);
        kr2.setPreferredSize(new Dimension(46, 46));
        kr2.setBorderPainted(false);
        kr2.setCursor(cursorHand);
        kr2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.inputCoin(2);
            }
        });

        kr5 = new JButton(new ImageIcon(getClass().getResource("/images/5kr.png")));
        kr5.setContentAreaFilled(false);
        kr5.setPreferredSize(new Dimension(46, 46));
        kr5.setBorderPainted(false);
        kr5.setCursor(cursorHand);
        kr5.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.inputCoin(5);
            }
        });

        kr10 = new JButton(new ImageIcon(getClass().getResource("/images/10kr.png")));
        kr10.setContentAreaFilled(false);
        kr10.setPreferredSize(new Dimension(46, 46));
        kr10.setBorderPainted(false);
        kr10.setCursor(cursorHand);
        kr10.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.inputCoin(10);
            }
        });

        kr20 = new JButton(new ImageIcon(getClass().getResource("/images/20kr.png")));
        kr20.setContentAreaFilled(false);
        kr20.setPreferredSize(new Dimension(46, 46));
        kr20.setBorderPainted(false);
        kr20.setCursor(cursorHand);
        kr20.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DrinksMachineApp.dm.inputCoin(20);
            }
        });

        lblDrink1 = new JLabel(DrinksMachineApp.prices[0] + "kr");
        lblDrink2 = new JLabel(DrinksMachineApp.prices[1] + "kr");
        lblDrink3 = new JLabel(DrinksMachineApp.prices[2] + "kr");

        txtOutput = new JTextArea("Output messages from the statemachine.\nMultiline...");
        txtOutput.setPreferredSize(new Dimension(300, 50));
        txtOutput.setEditable(false);
        txtOutput.setOpaque(false);
        txtOutput.setAutoscrolls(false);
        txtOutput.setMargin(new Insets(8, 15, 8, 15));

        lblRefund = new JLabel(new ImageIcon(getClass().getResource("/images/refund.png")));
        lblRefund.setPreferredSize(new Dimension(85, 64));

        /*
         * GridBagConstraints
         */

        cMainPanel = new GridBagConstraints();
        cMainPanel.fill = GridBagConstraints.VERTICAL;
        cMainPanel.weighty = 1;
        cMainPanel.anchor = GridBagConstraints.FIRST_LINE_START;

        cLeftPanel = new GridBagConstraints();
        cLeftPanel.anchor = GridBagConstraints.FIRST_LINE_START;
        cLeftPanel.fill = GridBagConstraints.BOTH;
        cLeftPanel.weightx = 0.6;
        cLeftPanel.weighty = 1;
        cLeftPanel.gridx = 0;
        cLeftPanel.gridy = 0;

        cRightPanel = new GridBagConstraints();
        cRightPanel.anchor = GridBagConstraints.FIRST_LINE_END;
        cRightPanel.fill = GridBagConstraints.BOTH;
        cRightPanel.weightx = 0.4;
        cRightPanel.weighty = 1;
        cRightPanel.gridx = 1;
        cRightPanel.gridy = 0;

        cButtonsPanel = new GridBagConstraints();
        cButtonsPanel.anchor = GridBagConstraints.PAGE_START;
        if (osName.indexOf("Windows") != -1) {
            cButtonsPanel.insets = new Insets(20, 2, 2, 25);
            cButtonsPanel.ipadx = 10;
        } else {
            cButtonsPanel.insets = new Insets(20, 2, 2, 18);
        }
        cButtonsPanel.gridx = 0;
        cButtonsPanel.gridy = 0;
        cButtonsPanel.weighty = 0.1;

        cCancelPanel = new GridBagConstraints();
        cCancelPanel.anchor = GridBagConstraints.PAGE_END;
        cCancelPanel.fill = GridBagConstraints.HORIZONTAL;
        cCancelPanel.gridx = 0;
        cCancelPanel.gridy = 1;
        if (osName.indexOf("Windows") != -1) {
            cCancelPanel.insets = new Insets(0, 0, 0, 30);
        } else {
            cCancelPanel.insets = new Insets(0, 0, 0, 18);
        }

        cCoinsPanel = new GridBagConstraints();
        cCoinsPanel.anchor = GridBagConstraints.PAGE_END;
        cCoinsPanel.gridx = 1;
        cCoinsPanel.gridy = 1;
        cCoinsPanel.weighty = 0.3;
        cCoinsPanel.insets = new Insets(20, 42, 10, 10);

        cTrayPanel = new GridBagConstraints();
        cTrayPanel.anchor = GridBagConstraints.LAST_LINE_START;
        cTrayPanel.gridx = 0;
        cTrayPanel.gridy = 1;
        cTrayPanel.gridheight = 2;
        cTrayPanel.weighty = 1;
        cTrayPanel.insets = new Insets(0, 5, 0, 0);

        cOutputPanel = new GridBagConstraints();
        cOutputPanel.anchor = GridBagConstraints.CENTER;
        cOutputPanel.gridwidth = 2;
        cOutputPanel.gridheight = 1;
        cOutputPanel.weighty = 0.1;
        cOutputPanel.insets = new Insets(10, 0, 0, 0);

        cRefundPanel = new GridBagConstraints();
        cRefundPanel.anchor = GridBagConstraints.PAGE_END;
        cRefundPanel.gridx = 1;
        cRefundPanel.gridy = 2;
        cRefundPanel.weighty = 0.1;
        cRefundPanel.insets = new Insets(0, 52, 10, 18);

        cBtnCancel = new GridBagConstraints();
        cBtnCancel.anchor = GridBagConstraints.PAGE_END;
        cBtnCancel.fill = GridBagConstraints.HORIZONTAL;
        cBtnCancel.weightx = 1;
        cBtnCancel.insets = new Insets(0, 5, 8, 8);

        cBtnDrink1 = new GridBagConstraints();
        cBtnDrink1.gridx = 1;
        cBtnDrink1.gridy = 0;
        cBtnDrink1.weightx = 0.8;

        cBtnDrink2 = new GridBagConstraints();
        cBtnDrink2.gridx = 1;
        cBtnDrink2.gridy = 1;
        cBtnDrink2.weightx = 0.8;

        cBtnDrink3 = new GridBagConstraints();
        cBtnDrink3.gridx = 1;
        cBtnDrink3.gridy = 2;
        cBtnDrink3.weightx = 0.8;

        cLblDrink1 = new GridBagConstraints();
        cLblDrink1.gridx = 0;
        cLblDrink1.gridy = 0;
        cLblDrink1.weightx = 0.2;

        cLblDrink2 = new GridBagConstraints();
        cLblDrink2.gridx = 0;
        cLblDrink2.gridy = 1;
        cLblDrink2.weightx = 0.2;

        cLblDrink3 = new GridBagConstraints();
        cLblDrink3.gridx = 0;
        cLblDrink3.gridy = 2;
        cLblDrink3.weightx = 0.2;

        cTxtOutput = new GridBagConstraints();
        cTxtOutput.anchor = GridBagConstraints.LINE_START;
        cTxtOutput.insets = new Insets(18, 5, 10, 15);

        cLblRefund = new GridBagConstraints();
        cLblRefund.anchor = GridBagConstraints.LAST_LINE_END;

        cBtnTray = new GridBagConstraints();
        cBtnTray.anchor = GridBagConstraints.LAST_LINE_START;
        cBtnTray.insets = new Insets(0, 15, 10, 0);

        cKr1 = new GridBagConstraints();
        cKr1.anchor = GridBagConstraints.CENTER;
        cKr1.gridx = 0;
        cKr1.gridy = 2;
        cKr1.insets = new Insets(1, 1, 1, 1);

        cKr2 = new GridBagConstraints();
        cKr2.anchor = GridBagConstraints.CENTER;
        cKr2.gridx = 0;
        cKr2.gridy = 1;
        cKr2.insets = new Insets(1, 1, 1, 1);

        cKr5 = new GridBagConstraints();
        cKr5.anchor = GridBagConstraints.CENTER;
        cKr5.gridx = 0;
        cKr5.gridy = 0;
        cKr5.insets = new Insets(1, 1, 1, 1);

        cKr10 = new GridBagConstraints();
        cKr10.anchor = GridBagConstraints.CENTER;
        cKr10.gridx = 1;
        cKr10.gridy = 1;
        cKr10.insets = new Insets(1, 1, 1, 1);

        cKr20 = new GridBagConstraints();
        cKr20.anchor = GridBagConstraints.CENTER;
        cKr20.gridx = 1;
        cKr20.gridy = 0;
        cKr20.insets = new Insets(1, 1, 1, 1);

        rightPanel.add(buttonsPanel, cButtonsPanel);
        buttonsPanel.add(btnDrink1, cBtnDrink1);
        buttonsPanel.add(btnDrink2, cBtnDrink2);
        buttonsPanel.add(btnDrink3, cBtnDrink3);

        buttonsPanel.add(lblDrink1, cLblDrink1);
        buttonsPanel.add(lblDrink2, cLblDrink2);
        buttonsPanel.add(lblDrink3, cLblDrink3);

        rightPanel.add(cancelPanel, cCancelPanel);
        cancelPanel.add(btnCancel, cBtnCancel);

        leftPanel.add(outputPanel, cOutputPanel);
        leftPanel.add(trayPanel, cTrayPanel);
        leftPanel.add(coinsPanel, cCoinsPanel);
        leftPanel.add(refundPanel, cRefundPanel);

        refundPanel.add(lblRefund, cLblRefund);

        trayPanel.add(btnTray, cBtnTray);
        outputPanel.add(txtOutput, cTxtOutput);

        coinsPanel.add(kr1, cKr1);
        coinsPanel.add(kr2, cKr2);
        coinsPanel.add(kr5, cKr5);
        coinsPanel.add(kr10, cKr10);
        coinsPanel.add(kr20, cKr20);

        mainPanel.add(leftPanel, cLeftPanel);
        mainPanel.add(rightPanel, cRightPanel);

        cp.add(mainPanel, cMainPanel);

        this.setVisible(true);
    }

    /**
     * Shows the parsed string in the text area. If needed the string can also
     * be added as a new line to the text area, preserving the current text.
     * @param message The String to be showed in the text area.
     * @param addLine If true the string will be added to the current text in the text area.
     */
    public void setOutputMsg(String message, boolean addLine) {
        if (addLine == true) {
            txtOutput.setText(txtOutput.getText() + "\n" + message);
        } else {
            txtOutput.setText(message);
        }
    }

    /**
     * Closes the tray by showing the closed image on the button component.
     */
    public void closeTray() {
        btnTray.setIcon(new ImageIcon(getClass().getResource("/images/tray_closed.png")));

    }

    /**
     * Opens the tray by showing the open image on the button component.
     */
    public void openTray() {
        btnTray.setIcon(new ImageIcon(getClass().getResource("/images/tray_open.png")));
    }
}
