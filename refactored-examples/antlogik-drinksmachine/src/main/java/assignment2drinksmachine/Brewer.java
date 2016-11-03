package assignment2drinksmachine;

import rx.Observable;
import rx.schedulers.Schedulers;

import javax.swing.SwingWorker;

/**
 *The purpose of this class is to simulate the waiting time for the machine to
 * prepare the drink, by using a timer.
 *  @author Jeppe Laursen & Manuel Maestrini
 */
public class Brewer {

    private DrinksMachine dm;

    /**
     * This method make the current thread sleep for 10 seconds
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    public void brew(DrinksMachine dm) {

        this.dm = dm;
        // RxRefactoring: subscribe instead of execute
        createRxObservable().subscribe();
    }

    // RxRefactoring: create private observable because the execute method was called in this class
    private Observable<Object> createRxObservable()
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
            Thread.sleep(10000);
        } catch (Exception e) {
        }
        return null;
    }

    // RxRefactoring: method can be private (Override must be removed)
    private void done() {
            dm.brewReady();
        }

}
