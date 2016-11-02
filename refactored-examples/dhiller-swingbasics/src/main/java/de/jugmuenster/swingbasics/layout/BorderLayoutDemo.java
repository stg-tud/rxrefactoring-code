/*
 * Copyright (c) 2011, Java User Group Münster, NRW, Germany, 
 * http://www.jug-muenster.de
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  - 	Redistributions of source code must retain the above copyright notice, this 
 * 	list of conditions and the following disclaimer.
 *  - 	Redistributions in binary form must reproduce the above copyright notice, 
 * 	this list of conditions and the following disclaimer in the documentation 
 * 	and/or other materials provided with the distribution.
 *  - 	Neither the name of the Java User Group Münster nor the names of its contributors may 
 * 	be used to endorse or promote products derived from this software without 
 * 	specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.jugmuenster.swingbasics.layout;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Demonstrates usage of a {@link BorderLayout}. Creates five areas which are
 * oriented into the five possible positions. Each area has a colored border to
 * make it visible.
 */
public class BorderLayoutDemo extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Object[][] DATA = new Object[][] {
	    { 1, "Unit Testing with Java", 1, 34.99, 34.99 },
	    { 2, "Head First Design Patterns", 1, 29.99, 29.99 }, };
    private static final Object[] COLUMN_NAMES = new Object[] { "Position",
	    "article", "pieces", "€/piece", "€ total" };

    public BorderLayoutDemo() {
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(createCustomerSelection(), BorderLayout.NORTH);
	getContentPane().add(createPositionsOverview());
	getContentPane().add(createCustomerOperations(), BorderLayout.WEST);
	getContentPane().add(createPositionOperations(), BorderLayout.EAST);
	getContentPane().add(createButtons(), BorderLayout.SOUTH);
    }

    private JPanel createButtons() {
	final JPanel buttons = new JPanel();
	buttons.add(new JButton("Save"));
	buttons.add(new JButton("Cancel"));
	buttons.setBorder(BorderFactory.createLineBorder(Color.RED));
	return buttons;
    }

    private JPanel createPositionsOverview() {
	final JScrollPane jScrollPane = new JScrollPane(new JTable(
		new DefaultTableModel(DATA, COLUMN_NAMES)));
	final JPanel jPanel = new JPanel();
	jPanel.add(jScrollPane);
	jPanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA));
	return jPanel;
    }

    private JPanel createCustomerSelection() {
	final JPanel customerSelection = new JPanel();
	customerSelection.add(new JLabel("Customer"));
	final JComboBox combobox = new JComboBox(new DefaultComboBoxModel(
		new Object[] { "JUG Münster" }));
	combobox.setSelectedIndex(0);
	customerSelection.add(combobox);
	customerSelection.setBorder(BorderFactory.createLineBorder(Color.BLUE));
	return customerSelection;
    }

    private JPanel createCustomerOperations() {
	final JPanel buttonsWest = new JPanel();
	buttonsWest.setLayout(new BoxLayout(buttonsWest, BoxLayout.PAGE_AXIS));
	buttonsWest.add(new JLabel("Customer"));
	buttonsWest.add(new JButton("Edit"));
	buttonsWest.add(new JButton("Delete"));
	buttonsWest.setBorder(BorderFactory.createLineBorder(Color.CYAN));
	return buttonsWest;
    }

    private JPanel createPositionOperations() {
	final JPanel buttonsEast = new JPanel();
	buttonsEast.setLayout(new BoxLayout(buttonsEast, BoxLayout.PAGE_AXIS));
	buttonsEast.add(new JLabel("Positions"));
	buttonsEast.add(new JButton("Edit"));
	buttonsEast.add(new JButton("Delete"));
	buttonsEast.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
	return buttonsEast;
    }

}
