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

import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Demonstrates thread-safe manipulation of swing components.
 */
class EDTSafeCalls {

    void safelyManipulateSwingComponent() {

	SwingUtilities.invokeLater(new Runnable() {

	    /**
	     * Manipulate your component within this method.
	     * 
	     * @see java.lang.Runnable#run()
	     */
	    @Override
	    public void run() {
		// TODO manipulate here
	    }
	});

    }

    void calculateThenPublishFeedbackSafelyThroughSwingComponent() {

	new SwingWorker<Object, Object>() {

	    /**
	     * Data retrieval or long running calculations go here.
	     * 
	     * @return result of calculation to be processed in
	     *         {@link javax.swing.SwingWorker#done()}
	     * 
	     * @see javax.swing.SwingWorker#doInBackground()
	     */
	    protected Object doInBackground() throws Exception {
		return null;
	    }

	    /**
	     * Provide visual feedback for the outcome of
	     * {@link javax.swing.SwingWorker#doInBackground()}.
	     * 
	     * @see javax.swing.SwingWorker#done()
	     */
	    protected void done() {
		try {
		    final Object doInBackgroundResult = get();
		    System.out.println(doInBackgroundResult);
		} catch (InterruptedException e) {
		    handleInterrupt(e);
		} catch (ExecutionException e) {
		    handleExceptionFromDoInBackground(e);
		}
	    }

	    /**
	     * Exception handling from doInBackground here
	     * 
	     * @param e
	     *            the exception
	     */
	    private void handleExceptionFromDoInBackground(ExecutionException e) {
		e.printStackTrace();
	    }

	    /**
	     * Interrupt handling here.
	     * 
	     * @param e
	     *            the intterupted exception
	     */
	    private void handleInterrupt(InterruptedException e) {
		e.printStackTrace(); // handle interrupts here
	    }

	}.execute();

    }
}