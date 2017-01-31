/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package TomatEpsilon;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author frederickhannouf
 */
public class Misc {
    // <editor-fold defaultstate="collapsed" desc="initComponentSize : Initialise Frame or Panel size">
    public static void initComponentSize(Object _component) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 940;
        int h = 480;
        if (_component instanceof JComponent) {
            JComponent component = (JComponent) _component;
            component.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
            component.setSize(w, h);
            component.setOpaque(false);
            component.show();
        } else if (_component instanceof JFrame) {
            JFrame component = (JFrame) _component;
            component.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
            component.setSize(w, h);
            component.show();
        }
    }// </editor-fold>
}
