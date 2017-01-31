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

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Calls the {@link Calculate#runCalculation(Subscriber)} from {@link #doInBackground(Subscriber)}
 * method to actually perform the calculation simulation.
 */
public class CalculationPerformer {

    private final CalculateWithSwingWorker calculateWithSwingWorker;

    CalculationPerformer(CalculateWithSwingWorker calculateWithSwingWorker) {
	this.calculateWithSwingWorker = calculateWithSwingWorker;
    }

    // RxRefactoring: subscriber added to be able to update the UI
    private Object doInBackground(Subscriber<Integer> subscriber) throws Exception {
	this.calculateWithSwingWorker.runCalculation(subscriber);
	return "OK";
    }

    // RxRefactoring: this method is now private. Result parameter added because "get()" was used.
    private void done(Object result) {
	try {
	    this.calculateWithSwingWorker.swingWorkerDemo.appendStatus(result);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    if (ex instanceof java.lang.InterruptedException)
		return;
	}
    }

    // RxRefactoring: creates the observable that is in charge of executin the async task
	public Observable<Object> createRxObservable()
	{
		Subscriber<Integer> subscriber = createUpdateSubscriber();
		return Observable
				.fromCallable(() -> doInBackground(subscriber)) // fromCallable will always return one emission
				.doOnNext(r -> done(r)) // use onNext instead of onCompleted, because the first emission is already the result from doInBackground
				.subscribeOn(Schedulers.computation())
				.observeOn(Schedulers.immediate());
	}

	// RxRefactoring: defines how the UI should be updated
	private Subscriber<Integer> createUpdateSubscriber()
	{
		return new Subscriber<Integer>()
            {
                @Override
                public void onCompleted()
                {

                }

                @Override
                public void onError(Throwable throwable)
                {

                }

                @Override
                public void onNext(Integer integer)
                {
                    calculateWithSwingWorker.swingWorkerDemo.appendPercentageFinished(integer);
                }
            };
	}
}