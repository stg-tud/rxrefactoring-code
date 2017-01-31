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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.SwingWorker;

/**
 * Simulate a calculation without using {@link SwingWorker}.
 * <p>
 * Expected effect is that whenever the calculation is started via
 * {@link #runCalculation()} the gui will freeze and provide no more feedback.
 * To the user this will seem like the application has frozen, and he will
 * shortly lose temper and kill it.
 */
public class CalculateWithoutSwingWorker extends Calculate {

    private static final long serialVersionUID = 1L;

    CalculateWithoutSwingWorker(SwingWorkerDemo swingWorkerDemo) {
	super(swingWorkerDemo, "Without SwingWorker");
	this.putValue(Action.SHORT_DESCRIPTION, "<html>"
		+ "Expected effect is that whenever the calculation is "
		+ "started the gui will freeze and provide no more "
		+ "feedback. " + "<br>"
		+ "To the user this will seem like the application has "
		+ "frozen, and he will shortly lose temper and kill it."
		+ "</html>");
    }

    /**
     * This implementation calls {@link #runCalculation()} from <b>within</b>
     * the EDT.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
	try {
	    runCalculation();
	} catch (InterruptedException e1) {
	    e1.printStackTrace();
	}
    }

    /**
     * Provide feedback within the event dispatch thread.
     * <p>
     * Although this approach seems good, this will not work, all feedback will
     * be delayed until the calculation has ended, and then it will be made
     * visible immediately.
     * 
     * @see de.jugmuenster.swingbasics.swingworker.calculation.Calculate#update(int)
     */
    @Override
    void update(final int i) {
	new ProgressMade(this, i).provide();
    }
}