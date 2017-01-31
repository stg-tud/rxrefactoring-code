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

package de.jugmuenster.swingbasics.swingworker.calculation;

import javax.swing.*;

import java.awt.*;

/**
 * Demo for {@link SwingWorker}. Provides buttons to start calculation with and
 * without a {@link SwingWorker}.
 */
@SuppressWarnings("nls")
public class SwingWorkerDemo extends JFrame {

    private static final long serialVersionUID = 1L;

    static final String PROGRESS = "progress";

    final JTextArea textarea = new JTextArea();
    final JButton withSwingWorkerButton = new JButton(
	    new CalculateWithoutSwingWorker(this));
    final JButton withoutSwingWorkerButton = new JButton(
	    new CalculateWithSwingWorker(this));

    public SwingWorkerDemo() {
	this.setLayout(new BorderLayout());
	textarea.setLineWrap(true);
	final JScrollPane comp = new JScrollPane(textarea);
	comp.setPreferredSize(new Dimension(400, 150));
	this.getContentPane().add(comp);
	final JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
	buttonPanel.add(withSwingWorkerButton);
	buttonPanel.add(withoutSwingWorkerButton);
	this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    void appendPercentageFinished(final int progressInPercent) {
	textarea.append(String.format("Progress %d %% \n", progressInPercent));
    }

    void appendStatus(final Object status) {
	textarea.append(String.format("New status %s\n", status));
    }

}
