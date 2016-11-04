package model;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import javax.swing.*;

/**
 * Author: Grebiel Jose Ifill Brito
 * Created: 04.11.16 creation date
 */
// RxRefactoring:
// This class doesn't extend SwingWorker anymore
public class TimerWorker
{
    private String totalTime;
    private JLabel timerLabel;
    private JLabel resultLabel;

    // RxRefactoring:
    // This method uses the same parameters as the old constructor
    public Observable<String> createRxTimeWorker(JLabel timerLabel, JLabel resultLabel, String totalTime)
    {
        this.timerLabel = timerLabel;
        this.totalTime = totalTime;
        this.resultLabel = resultLabel;

        // Subscriber needed to update the UI. A parameter needs to be added to doInBackground
        // The process method to update the UI is no longer needed
        Subscriber<String> updateSubscriber = getUpdateSubscriber(this.timerLabel);
        return Observable.fromCallable(() -> doInBackground(updateSubscriber))
                .doOnNext(r -> done(r)) // done called "get()". Therefore this method needs a new parameter with the result
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.immediate());
    }

    private Subscriber<String> getUpdateSubscriber(final JLabel timerLabel)
    {
        return new Subscriber<String>()
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
            public void onNext(String s)
            {
                timerLabel.setText(s);
            }
        };
    }

    private String doInBackground(Subscriber<String> updateSubscriber) throws Exception
    {
        System.out.println("Do in background started");
        String[] split = totalTime.split(":");
        String minutes = split[ 0 ];
        String seconds = split[ 1 ];

        int min = Integer.parseInt(minutes);
        int sec = Integer.parseInt(seconds);
        int totalTimeInSeconds = min * 60 + sec;

        for ( int i = 0; i < totalTimeInSeconds; i++ )
        {
            Thread.sleep(1000L);
            if ( sec == 0 )
            {
                min--;
                sec = 59;
            }
            else
            {
                sec--;
            }
            String currentTime = getCurrentTime(min, sec);
            updateSubscriber.onNext(currentTime);
        }

        return "TIME IS OVER";
    }

    private void done(String result)
    {
        try
        {
            resultLabel.setText(result);
            System.out.println("SwingWorker finished");
        }
        catch ( Exception e )
        {
            System.out.println("Execution cancelled");
        }
    }

    private String getCurrentTime(int min, int sec)
    {
        String currentMin = String.format("%02d", min);
        String currentSec = String.format("%02d", sec);
        return currentMin + ":" + currentSec;
    }
}
