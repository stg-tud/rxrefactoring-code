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

package de.jugmuenster.swingbasics.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Demonstrates some basic components with {@link GridBagLayout}.
 */
public class ComponentsDemo extends JFrame {

    private static final int IPAD_ZERO = 0;
    private static final double WEIGHT_ZERO = 0.0;
    private static final Insets INSETS_2_PX = new Insets(2, 2, 2, 2);
    private static final long serialVersionUID = 1L;

    public ComponentsDemo() {
	super("ComponentsDemo");
	this.getContentPane().setLayout(new GridBagLayout());
	setUpNameRow();
	setUpAgeRow();
    }

    private void setUpNameRow() {
	this.add(new JLabel("Name"), new GridBagConstraints(0, 0, 1, 1,
		WEIGHT_ZERO, WEIGHT_ZERO, GridBagConstraints.WEST,
		GridBagConstraints.NONE, INSETS_2_PX, IPAD_ZERO, IPAD_ZERO));
	this.add(new JTextField("Name"), new GridBagConstraints(1, 0, 1, 1,
		WEIGHT_ZERO, WEIGHT_ZERO, GridBagConstraints.WEST,
		GridBagConstraints.NONE, INSETS_2_PX, IPAD_ZERO, IPAD_ZERO));
    }

    private void setUpAgeRow() {
	this.add(new JLabel("Age"), new GridBagConstraints(0, 1, 1, 1,
		WEIGHT_ZERO, WEIGHT_ZERO, GridBagConstraints.WEST,
		GridBagConstraints.NONE, INSETS_2_PX, IPAD_ZERO, IPAD_ZERO));
	this.add(new JFormattedTextField(new Integer(0)),
		new GridBagConstraints(1, 1, 1, 1, WEIGHT_ZERO, WEIGHT_ZERO,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			INSETS_2_PX, IPAD_ZERO, IPAD_ZERO));
    }

}
