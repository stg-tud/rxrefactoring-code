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

import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 * Base {@link Action} implementation to simulate calculation.
 */
public abstract class Calculate extends AbstractAction {

    private static final long serialVersionUID = 1L;

    protected final SwingWorkerDemo swingWorkerDemo;

    Calculate(SwingWorkerDemo swingWorkerDemo, String label) {
	super(label);
	this.swingWorkerDemo = swingWorkerDemo;
    }

    /**
     * Called when the calculation should be started. Will provide visual
     * feedback to show that the calculation is running. After calculation has
     * finished, will provide feedback as well.
     * 
     * @throws InterruptedException
     */
    // RxRefactoring: subscriber needed to update the UI
    public void runCalculation(Subscriber<Integer> subscriber) throws InterruptedException {
	new CalculationStarted(this).provide();
	calculate(subscriber);
	new CalculationFinished(this).provide();
    }

    /**
     * This implementation simulates a long running calculation of about ten
     * seconds with updates during making progress.
     * 
     * @throws InterruptedException
     */
    // RxRefactoring: uses subscriber.onNext to update the UI instead of an update method
    private void calculate(Subscriber<Integer> subscriber) throws InterruptedException {
	for (int i = 1; i <= 100; i++) {
	    Thread.sleep(100);
	    subscriber.onNext(i);
	}
    }

    /**
     * Called whenever an update of the calculation status should be published.
     * 
     * @param i
     *            the progress value as percentage 1 &lt;= i &lt;=100.
     */
    // RxRefactoring: update method no longer needed
//    abstract void update(int i);

}