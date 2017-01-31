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

import rx.Subscriber;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.SwingWorker;

/**
 * Simulate a calculation using {@link SwingWorker}.
 * <p>
 * Expected effect is that whenever the calculation is started via
 * {@link #runCalculation(Subscriber)} the gui will provide feedback immediately. To the
 * user this will seem like the application is still responsive even when it is
 * working.
 */
public class CalculateWithSwingWorker extends Calculate {

    private static final long serialVersionUID = 1L;

    // RxRefactoring: SwingWorker is no longer needed
    private CalculationPerformer calculationPerformer;

    CalculateWithSwingWorker(SwingWorkerDemo swingWorkerDemo) {
	super(swingWorkerDemo, "With SwingWorker");
	this.putValue(Action.SHORT_DESCRIPTION, "<html>"
		+ "Expected effect is that whenever the calculation is "
		+ "started the gui will provide feedback immediately. "
		+ "<br>"
		+ "To the user this will seem like the application is "
		+ "still responsive even when it is working." + "</html>");
    }

    /**
     * Creates a {@link SwingWorker} instance that calls
     * {@link #runCalculation(Subscriber)} from outside the EDT.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    // RxRefactoring: start execution by subscribing the Observable
    public void actionPerformed(ActionEvent e) {
        calculationPerformer = new CalculationPerformer(this);
        calculationPerformer.createRxObservable().subscribe();
    }
}