package ru.ifmo.juneiform;

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import ru.ifmo.juneiform.ocr.Cuneiform;
import ru.ifmo.juneiform.ui.AboutBox;
import ru.ifmo.juneiform.ui.MainFrame;
import ru.ifmo.juneiform.ui.SettingsWindow;

/**
 *
 * @author Ivan Stepuk
 */
public class Main {

    public static void main(String... args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class).info("Could not load Nimbus LAF");
        }

        MutablePicoContainer container = new DefaultPicoContainer(new Caching());
        container.addComponent(Controller.class);
        container.addComponent(Cuneiform.class);
        container.addComponent(Editor.class);
        container.addComponent(MainFrame.class);
        container.addComponent(SettingsWindow.class);
        container.addComponent(Settings.class);
        container.addComponent(AboutBox.class);

        final MainFrame mainFrame = container.getComponent(MainFrame.class);
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                mainFrame.setVisible(true);
            }
        });
    }
}
