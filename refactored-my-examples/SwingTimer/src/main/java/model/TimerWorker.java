package model;

import javax.swing.*;
import java.util.List;

/**
 * Description:
 * Author: Grebiel Jose Ifill Brito
 * Created: 04.11.16 creation date
 */
public class TimerWorker extends SwingWorker<String, String>
{
    private String totalTime;
    private JLabel timerLabel;
    private JLabel resultLabel;

    public TimerWorker(JLabel timerLabel, JLabel resultLabel, String totalTime)
    {
        this.timerLabel = timerLabel;
        this.totalTime = totalTime;
        this.resultLabel = resultLabel;
    }

    @Override
    protected String doInBackground() throws Exception
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
            }            else
            {
                sec--;
            }
            String currentTime = getCurrentTime(min, sec);
            publish(currentTime);
        }

        return "TIME IS OVER";
    }

    @Override
    protected void done()
    {
        try
        {
            String result = get();
            resultLabel.setText(result);
            System.out.println("SwingWorker finished");
        }
        catch ( Exception e )
        {
            System.out.println("Execution cancelled");
        }
        super.done();
    }

    @Override
    protected void process(List<String> chunks)
    {
        for ( String time : chunks )
        {
            timerLabel.setText(time);
        }
    }

    private String getCurrentTime(int min, int sec)
    {
        String currentMin = String.format("%02d", min);
        String currentSec = String.format("%02d", sec);
        return currentMin + ":" + currentSec;
    }
}
