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

package de.jugmuenster.swingbasics.action;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

/**
 * Demonstrates usage of {@link Action} instancen.
 */
public class ActionDemo extends JFrame {

    private final JTextArea textArea = new JTextArea();
    private final AaandAction aaandAction = new AaandAction(textArea);

    public ActionDemo() {
	getContentPane().setLayout(new BorderLayout());
	setUpMenuBarWithAction();
	setUpTextArea();
	setUpButtonWithAction();
    }

    /**
     * Set up the menu bar that uses the action.
     */
    private void setUpMenuBarWithAction() {
	final JMenuBar jMenuBar = new JMenuBar();
	final JMenu actions = new JMenu("Actions");

	// Same action instance used here
	actions.add(new JMenuItem(aaandAction));

	jMenuBar.add(actions);
	this.setJMenuBar(jMenuBar);
    }

    /**
     * Set up the text area that displays the usage of the action.
     */
    private void setUpTextArea() {
	final JScrollPane jScrollPane = new JScrollPane(textArea);
	jScrollPane.setPreferredSize(new Dimension(300, 200));
	this.getContentPane().add(jScrollPane);
    }

    /**
     * Set up a button that uses the action.
     */
    private void setUpButtonWithAction() {
	this.getContentPane().add(new JButton(aaandAction), BorderLayout.SOUTH);
    }

}
