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

package de.jugmuenster.swingbasics;

import java.awt.AWTEvent;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Entry class for starting the demo application. From here all other demos can
 * be started.
 */
public class StartDemos extends JFrame {

    /**
     * TODO: Find better method for determining active window.
     * 
     * Currently the active window is set from outside. There must be a more
     * elegant approach.
     * 
     * @see Utils#display(Class, int, boolean)
     */
    private static JFrame focusedWindow = null;

    public StartDemos() {
	this.getContentPane()
		.setLayout(new GridLayout(Demo.values().length, 1));
	for (Demo d : Demo.values()) {
	    JButton jButton = new JButton(d.create());
	    jButton.setHorizontalAlignment(SwingConstants.LEFT);
	    jButton.setHorizontalTextPosition(SwingConstants.TRAILING);
	    this.getContentPane().add(jButton);
	}
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) throws ClassNotFoundException,
	    InstantiationException, IllegalAccessException,
	    UnsupportedLookAndFeelException {
	setNimbusAsLookAndFeel();
	enableCloseKeyboardShortcut();
	displayMainFrame();
    }

    private static void displayMainFrame() {
	Utils.display(StartDemos.class, JFrame.EXIT_ON_CLOSE, false);
    }

    private static void enableCloseKeyboardShortcut() {
	Toolkit.getDefaultToolkit().addAWTEventListener(
		new CloseWindowKeyboardShortcutListener(),
		AWTEvent.KEY_EVENT_MASK);
    }

    private static void setNimbusAsLookAndFeel() throws ClassNotFoundException,
	    InstantiationException, IllegalAccessException,
	    UnsupportedLookAndFeelException {
	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(info.getName())) {
		UIManager.setLookAndFeel(info.getClassName());
		return;
	    }
	}
	throw new IllegalStateException(
		"Could not activate Nimbus LookAndFeel!");
    }

    public static void setFocusedWindow(JFrame current) {
	StartDemos.focusedWindow = current;
    }

    public static void disposeFocusedWindow() {
	if (focusedWindow == null)
	    return;
	focusedWindow.dispose();
    }

}
