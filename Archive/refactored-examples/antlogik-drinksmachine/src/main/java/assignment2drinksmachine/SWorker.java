/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

import rx.Observable;
import rx.schedulers.Schedulers;

import javax.swing.SwingWorker;

/**
 * A timer class used for delaying certain operations.
 * @author Jeppe Laursen & Manuel Maestrini
 */
// RxRefactoring: it doesn't extend SwingWorker anymore
public class SWorker {

    private DrinksMachine dm;

    /**
     * Assigns the param to the private instance member so that it can be used by the done() method.
     * @param dm DrinksMachine object used to make a callback to continue the state machine.
     */
    public SWorker(DrinksMachine dm) {
        this.dm = dm;
    }

    // RxRefactoring: create observable using existing methods
    public Observable<Object> createRxObservable()
    {
        return Observable.fromCallable(() -> doInBackground())
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.immediate())
                .onErrorResumeNext(Observable.empty())
                .doOnCompleted(() -> done());
    }

    // RxRefactoring: method can be private (Override must be removed)
    private Object doInBackground() throws Exception {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        return null;
    }

    // RxRefactoring: method can be private (Override must be removed)
    private void done() {
        this.dm.selectDrinkMessage(false);
    }
}
