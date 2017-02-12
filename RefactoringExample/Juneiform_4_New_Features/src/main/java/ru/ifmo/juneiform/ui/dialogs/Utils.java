package ru.ifmo.juneiform.ui.dialogs;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import rx.Observable;
import rx.Subscription;

/**
 * Description: The following files were also added<br>
 * {@link ru.ifmo.juneiform.DocumentLoader}<br>
 * {@link ru.ifmo.juneiform.Editor}
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/18/2017
 */
public class Utils
{
    public static final String LABEL_PREFIX = "Processing Image";
    private static JFrame frame;
    private static Subscription loadingText;

    public static void closeLoadingSpinner()
    {
        frame.setVisible(false);
        frame.dispose();
        loadingText.unsubscribe();
        loadingText = null;
    }

    public static void showLoadingSpinner()
    {
        JFrame frame = new JFrame("Executing OCR");

        URL loadingSpinnerUrl = Utils.class.getResource("/images/ajax-loader.gif");
        ImageIcon loading = new ImageIcon(loadingSpinnerUrl);
        JLabel label = new JLabel(LABEL_PREFIX, JLabel.CENTER);
        label.setIcon(loading);
        frame.add(label);

        loadingText = Observable.interval(500L, TimeUnit.MILLISECONDS)
                .doOnNext(n -> getText(label, n))
                .subscribe();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLocationRelativeTo(null);
        Utils.frame = frame;
        frame.setVisible(true);
    }

    private static void getText(JLabel label, Long n)
    {
        Long numberOfPoints = n % 4;
        int numberOfPointsInt = Integer.parseInt(numberOfPoints.toString());
        label.setText(LABEL_PREFIX + new String(new char[ numberOfPointsInt ]).replace("\0", "."));
    }
}