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

package de.jugmuenster.swingbasics.listener.concept;


/**
 * Demo to show the basics of the listener concept. Uses the nomenclature of
 * Observer and Observable.
 * <p>
 * The <b>observer</b> is the client that is interested in receiving updates
 * from a server. The <b>observable</b> is the server that fires updates to all
 * interested parties.
 * <p>
 * Each observer attaches a <b>listener</b> to the observable that acts as a
 * call back for the clients to be notified.
 * <p>
 * Whenever the server changes its state it notifies all clients by calling the
 * corresponding listener method.
 */
public class ListenerDemo {

    public static void main(String[] args) {

	final Observable observable = new Observable();

	final Observer firstObserver = new Observer("First");
	observable.addStateListener(firstObserver.myStateListener);

	final Observer secondObserver = new Observer("Second");
	observable.addStateListener(secondObserver.myStateListener);

	/**
	 * Output:
	 * 
	 * <pre>
	 * &quot;First&quot;.handleStateChanged( StateEvent [state=ON] )
	 * &quot;Second&quot;.handleStateChanged( StateEvent [state=ON] )
	 * </pre>
	 */
	observable.fireStateChanged(new StateEvent("ON"));

	/**
	 * Output:
	 * 
	 * <pre>
	 * &quot;First&quot;.handleStateChanged( StateEvent [state=OFF] )
	 * &quot;Second&quot;.handleStateChanged(StateEvent[state = OFF])
	 * </pre>
	 */
	observable.fireStateChanged(new StateEvent("OFF"));

	observable.removeStateListener(firstObserver.myStateListener);

	/**
	 * Output:
	 * 
	 * <pre>
	 * &quot;Second&quot;.handleStateChanged(StateEvent[state = ON])
	 * </pre>
	 */
	observable.fireStateChanged(new StateEvent("ON"));

	/**
	 * Output:
	 * 
	 * <pre>
	 * &quot;Second&quot;.handleStateChanged(StateEvent[state = OFF])
	 * </pre>
	 */
	observable.fireStateChanged(new StateEvent("OFF"));

	observable.removeStateListener(secondObserver.myStateListener);

    }
}
