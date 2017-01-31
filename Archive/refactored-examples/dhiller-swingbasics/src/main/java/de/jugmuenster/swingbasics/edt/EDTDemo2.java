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

package de.jugmuenster.swingbasics.edt;

import java.awt.Dimension;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Try to demonstrate <a href=
 * "http://download.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html"
 * >Event Dispatch Thread</a> issues.
 */
public class EDTDemo2 extends JFrame {

    private static EDTDemo2 edtDemo2;
    private final JTextArea view = new JTextArea();
    private final JScrollPane comp = new JScrollPane(view) {
	public void paintAll(Graphics g) {
	    super.paintAll(g);
	    for (int i = 0; i < 100; i++)
		g.drawLine(i, 0, i, 100);
	    invalidate();
	    revalidate();
	    repaint();
	}
    };

    public EDTDemo2() {
	comp.setPreferredSize(new Dimension(320, 200));
	this.getContentPane().add(comp);
    }

    public static void main(String[] args) throws InterruptedException,
	    InvocationTargetException {
	createAndShowDemoFrame();
	manipulateOutsideEDT();
    }

    private static void manipulateOutsideEDT() throws InterruptedException {
	final Random r = new Random();
	while (true) {
	    synchronized (EDTDemo2.class) {
		EDTDemo2.class.wait(250);
	    }
	    final Dimension d = new Dimension(200 + (r.nextInt() % 100),
		    200 + (r.nextInt() % 100));
	    // XXX: set size outside the EDT
	    edtDemo2.comp.setSize(d);
	    edtDemo2.view.append(d.toString() + "\n");
	}
    }

    private static void createAndShowDemoFrame() throws InterruptedException,
	    InvocationTargetException {
	SwingUtilities.invokeAndWait(new Runnable() {

	    @Override
	    public void run() {
		edtDemo2 = new EDTDemo2();
		edtDemo2.setDefaultCloseOperation(EXIT_ON_CLOSE);
		edtDemo2.pack();
		edtDemo2.setLocationRelativeTo(null);
		edtDemo2.setVisible(true);
	    }
	});
    }

}
