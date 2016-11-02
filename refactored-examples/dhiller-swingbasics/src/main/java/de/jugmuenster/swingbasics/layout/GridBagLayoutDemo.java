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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;

/**
 * A demo showing some components arranged via a {@link GridBagLayout}
 * resembling an input form.
 * <p>
 * The content pane (see {@link JFrame#getContentPane()}) itself has a
 * {@link BorderLayout}. At the center there is a JPanel acting as container
 * with a {@link GridBagLayout}. This has the components resembling the input
 * form. These components are added in such a way that they will resize as
 * needed when the size of the frame is changed.
 * <p>
 * At the bottom there is another {@link JPanel} containing the buttons.
 */
public class GridBagLayoutDemo extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final double RESIZE_FULL_X = 1.0;
    private static final double RESIZE_THREE_QUARTERS_X = 0.75;
    private static final double RESIZE_HALF_X = 0.5;
    private static final double NO_RESIZE_X = 0.0;

    private static final double RESIZE_FULL_Y = 1.0;

    private static final Resizing RESIZE_FULL_FOR_X_AND_Y = new Resizing(
	    RESIZE_FULL_X, RESIZE_FULL_Y);
    private static final double NO_RESIZE_Y = 0.0;

    private static final Resizing FULL_X_RESIZE = new Resizing(RESIZE_FULL_X,
	    NO_RESIZE_Y);

    private static final Resizing RESIZE_THREE_QUARTERS_X_NO_Y = new Resizing(
	    RESIZE_THREE_QUARTERS_X, NO_RESIZE_Y);

    private static final Resizing RESIZE_HALF_X_NO_Y = new Resizing(
	    RESIZE_HALF_X, NO_RESIZE_Y);

    private static final Resizing NO_RESIZING = new Resizing(NO_RESIZE_X,
	    NO_RESIZE_Y);

    private static final int NO_PADDING_X = 0;
    private static final int NO_PADDING_Y = 0;

    private static final int NO_FILL = GridBagConstraints.NONE;
    private static final int FILL_HORIZONTAL = GridBagConstraints.HORIZONTAL;
    private static final int FILL_HORIZONTAL_AND_VERTICAL = GridBagConstraints.BOTH;

    private static final int ONE_CELL_GRIDWIDTH_X = 1;
    private static final int ONE_CELL_GRIDWIDTH_Y = 1;

    private static final int DEFAULT_COMPONENT_ORIENTATION = GridBagConstraints.WEST;

    private static final Insets TWO_PIXELS_IN_ALL_DIRECTIONS = new Insets(2, 2,
	    2, 2);

    private static final class Resizing {
	final double x;
	final double y;

	Resizing(double resizeX, double resizeY) {
	    this.x = resizeX;
	    this.y = resizeY;
	}
    }

    public GridBagLayoutDemo() {
	super("GridBagLayoutDemo");
	this.getContentPane().setLayout(new BorderLayout());

	// BorderLayout.CENTER is default is nothing is specified
	this.getContentPane().add(createInputForm());

	this.getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createInputForm() {
	final JPanel center = new JPanel(new GridBagLayout());
	addLabelAndFirstTextFieldWithFullXResize(center);
	addLabelAndSecondTextFieldWithFullXResize(center);
	addLabelWithFulLResizingTextArea(center);
	makePanelDistinguishableByToolTipAndColoredBorder(center, "GridBagLayout", Color.RED);
	return center;
    }

    private void makePanelDistinguishableByToolTipAndColoredBorder(final JPanel center, String tooltipText, Color borderColor) {
	center.setToolTipText(tooltipText);
	center.setBorder(BorderFactory.createLineBorder(borderColor));
    }

    private void addLabelAndSecondTextFieldWithFullXResize(final JPanel center) {
	center.add(new JLabel("Feld 2"),
		newNoResizeAndNoFillConstraintsForGridPosition(0, 1));
	center.add(
		new JTextField("Feld 2"),
		newConstraintsForGridPosition(1, 1, FULL_X_RESIZE,
			FILL_HORIZONTAL));
    }

    private void addLabelAndFirstTextFieldWithFullXResize(final JPanel center) {
	center.add(new JLabel("Feld 1"),
		newNoResizeAndNoFillConstraintsForGridPosition(0, 0));
	center.add(
		new JTextField("Feld 1"),
		newConstraintsForGridPosition(1, 0, FULL_X_RESIZE,
			FILL_HORIZONTAL));
    }

    private void addLabelWithFulLResizingTextArea(final JPanel center) {
	center.add(new JLabel("Feld 3"),
		newNoResizeAndNoFillConstraintsForGridPosition(0, 2));
	center.add(
		new JScrollPane(new JTextArea("Feld 3")),
		newConstraintsForGridPosition(1, 2, RESIZE_FULL_FOR_X_AND_Y,
			FILL_HORIZONTAL_AND_VERTICAL));
    }

    private GridBagConstraints newNoResizeAndNoFillConstraintsForGridPosition(
	    final int xPositionInGrid, final int yPositionInGrid) {
	return newConstraintsForGridPosition(xPositionInGrid, yPositionInGrid,
		NO_RESIZING, NO_FILL);
    }

    private GridBagConstraints newConstraintsForGridPosition(
	    final int xPositionInGrid, final int yPositionInGrid,
	    final Resizing resizing, final int fillCellsInGrid) {
	return new GridBagConstraints(xPositionInGrid, yPositionInGrid,
		ONE_CELL_GRIDWIDTH_X, ONE_CELL_GRIDWIDTH_Y, resizing.x,
		resizing.y, DEFAULT_COMPONENT_ORIENTATION, fillCellsInGrid,
		TWO_PIXELS_IN_ALL_DIRECTIONS, NO_PADDING_X, NO_PADDING_Y);
    }

    private JPanel createButtonPanel() {
	final JPanel buttons = new JPanel();
	buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
	buttons.add(Box.createHorizontalGlue());
	buttons.add(new JButton("OK"));
	buttons.add(new JButton("Cancel"));
	makePanelDistinguishableByToolTipAndColoredBorder(buttons, "BoxLayout",
		Color.BLUE);
	return buttons;
    }

}
