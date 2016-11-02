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

import javax.swing.JFrame;

/**
 * Creates a new instance of a demo class and displays it at the center of the
 * screen.
 */
public class DemoInitiator implements Runnable {
    private final int defaultCloseOperation;
    private final boolean center;
    private final Class<? extends JFrame> c;

    public DemoInitiator(int defaultCloseOperation, boolean center,
	    Class<? extends JFrame> c) {
	this.defaultCloseOperation = defaultCloseOperation;
	this.center = center;
	this.c = c;
    }

    public void run() {
	final JFrame swingWorkerDemo;
	try {
	    swingWorkerDemo = c.newInstance();
	    swingWorkerDemo.setDefaultCloseOperation(defaultCloseOperation);
	    swingWorkerDemo.pack();
	    if (center)
		swingWorkerDemo.setLocationRelativeTo(null);
	    swingWorkerDemo.setVisible(true);
	    swingWorkerDemo.addWindowFocusListener(new FocusedWindowUpdater(
		    swingWorkerDemo));
	} catch (InstantiationException e) {
	    e.printStackTrace(); // TODO
	} catch (IllegalAccessException e) {
	    e.printStackTrace(); // TODO
	}
    }
}