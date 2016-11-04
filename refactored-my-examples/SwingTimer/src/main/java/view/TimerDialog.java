package view;

import model.Keys;
import model.MyTimer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TimerDialog extends JDialog
{
    private JPanel contentPane;
    private JButton cancelButton;
    private JButton startButton;
    private JLabel timerLabel;
    private JLabel resultLabel;

    public TimerDialog()
    {
        Properties properties = getProperties();
        String appName = (String) properties.get(Keys.APP_NAME);
        setTitle(appName);
        setContentPane(contentPane);
        setModal(true);
        String initialTime = (String) properties.get(Keys.INITIAL_TIME);
        MyTimer myTimer = new MyTimer();

        startButton.addActionListener(e -> myTimer.startTimer(timerLabel, resultLabel, initialTime));
        cancelButton.addActionListener(e -> {
            myTimer.cancelTimer();
            resultLabel.setText("");
            timerLabel.setText(initialTime);
        });

    }

    private static Properties getProperties()
    {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = loader.getResourceAsStream(Keys.PROPERTIES_FILE))
        {
            properties.load(in);
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return properties;
    }

    public static void main(String[] args)
    {
        TimerDialog dialog = new TimerDialog();
        centerDialog(dialog);
        dialog.setPreferredSize(new Dimension(400,200));
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private static void centerDialog(TimerDialog dialog)
    {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - dialog.getWidth()) / 2;
        final int y = (screenSize.height - dialog.getHeight()) / 2;
        dialog.setLocation(x, y);
    }
}
