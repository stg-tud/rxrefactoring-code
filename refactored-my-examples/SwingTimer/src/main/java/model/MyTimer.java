package model;

import javax.swing.*;

/**
 * Author: Grebiel Jose Ifill Brito
 * Created: 04.11.16 creation date
 */
public class MyTimer
{
    private TimerWorker timerWorker;

    public void cancelTimer()
    {
        timerWorker.cancel(true);
    }

    public void startTimer(JLabel timerLabel, JLabel resultLabel, String validatedTime)
    {
        timerWorker = new TimerWorker(timerLabel, resultLabel, validatedTime);
        timerWorker.execute();
    }
}
