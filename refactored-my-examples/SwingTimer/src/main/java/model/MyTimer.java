package model;

import rx.Observable;
import rx.Subscription;

import javax.swing.*;

/**
 * Author: Grebiel Jose Ifill Brito
 * Created: 04.11.16 creation date
 */
public class MyTimer
{
    // RxRefactoring:
    // TimeWorker is only used in one method. Therefore it doesn't need to be a class field
    // The subscription needs to be unsubscribed on cancel. Therefore we need to keep the reference in a private field
    private Subscription timeWorkerSubscription;

    public void cancelTimer()
    {
        timeWorkerSubscription.unsubscribe();
    }

    // RxRefactoring:
    // The constructor for TimeWorker doesn't have paremters any more.
    // The parameters are used to create the Observable
    // The reference to the subscription must be kept, so we can unsubscribe it
    public void startTimer(JLabel timerLabel, JLabel resultLabel, String validatedTime)
    {
        TimerWorker timerWorker = new TimerWorker();
        Observable<String> rxTimeWorker = timerWorker.createRxTimeWorker(timerLabel, resultLabel, validatedTime);
        timeWorkerSubscription = rxTimeWorker.subscribe();
    }
}
