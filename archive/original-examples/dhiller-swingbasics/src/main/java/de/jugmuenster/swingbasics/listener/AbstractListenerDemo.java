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

package de.jugmuenster.swingbasics.listener;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.jugmuenster.swingbasics.Utils;

/**
 * Base class for listener demos.
 */
public abstract class AbstractListenerDemo extends JFrame {

    /**
     * Area where event data will be displayed.
     */
    protected final JTextArea eventDisplay = new JTextArea();

    public AbstractListenerDemo() throws HeadlessException {
	super();
	this.getContentPane().setLayout(new BorderLayout());
	eventDisplay.setWrapStyleWord(true);
	eventDisplay.setLineWrap(true);
	JScrollPane jScrollPane = new JScrollPane(eventDisplay);
	jScrollPane.setPreferredSize(new Dimension(640, 400));
	this.getContentPane().add(jScrollPane);
	this.getContentPane().add(createDemoComponents(), BorderLayout.SOUTH);
    }

    /**
     * Called when the demo is instantiated. Subclasses can create their demo
     * components within a container such as a {@link JPanel}.
     * 
     * @return the panel containing the demo components
     */
    protected abstract Component createDemoComponents();

    /**
     * Displays event data through a text component.
     * <p>
     * <b>This method is not thread safe!</b>
     * 
     * @param e
     */
    public void appendEvent(AWTEvent e) {
	eventDisplay.append(Utils.newDateFormat().format(new Date()) + ": " + e
		+ "\n\n");
    }

}