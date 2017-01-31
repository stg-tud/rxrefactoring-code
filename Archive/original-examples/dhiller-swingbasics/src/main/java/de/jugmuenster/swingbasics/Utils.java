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

import java.text.SimpleDateFormat;
import java.util.Random;

import javax.swing.JFrame;

/**
 * Utility class with convenience methods.
 */
public class Utils {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSS";
    public static final Random random = new Random();

    public static void display(final Class<? extends JFrame> c,
	    final int defaultCloseOperation, final boolean center) {
	java.awt.EventQueue.invokeLater(new DemoInitiator(defaultCloseOperation,
		center, c));

    }

    /**
     * @return a new DateFormat instance
     * @see #DATE_FORMAT
     */
    public static SimpleDateFormat newDateFormat() {
	return new SimpleDateFormat(DATE_FORMAT);
    }

    /**
     * @param min
     * @param max
     * @return a random value between the min and max values
     */
    public static int randomValueBetween(final int min, final int max) {
        final int range = max - min;
        return Math.abs(random.nextInt() % range) + min;
    }

    /**
     * Waits a random amount of time
     * 
     * @param min
     *            the time to wait at least
     * @param max
     *            the time to wait at most
     */
    public static void waitMSecsBetween(int min, int max) {
        final int waitMSecs = randomValueBetween(min, max);
        final Object mutex = new Object();
        synchronized (mutex) {
            try {
        	mutex.wait(waitMSecs);
            } catch (InterruptedException e) {
        	return;
            }
        }
    }

}
