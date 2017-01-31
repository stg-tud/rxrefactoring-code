package de.tong;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.tong.gui.GamePanel;
import de.tong.util.FontManager;

/**
 * 
 * The main class for the software project 'Tong'. The main method in this class
 * creates a new JFrame and will set its content pane to our {@link GamePanel}
 * class.
 * 
 * 
 * @author Wolfgang Müller
 * 
 * 
 */
public class Tong {

    public static void main(String[] args) {

	FontManager.loadFonts();

	File profile = new File("Profiles");
	if (!profile.exists()) {
	    profile.mkdir();
	}

	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {

		JFrame f = new JFrame("fooTong");
		GamePanel pan = new GamePanel(f);

		f.setContentPane(pan);
		f.setResizable(false);

		// Pack the frame, make it visible and exit on frame close.
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    }
	});

    }
}
