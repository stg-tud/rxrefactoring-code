package de.tong.util;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class FontManager {

    public static Map<String, Font> fontMap = new HashMap<String, Font>();
    private static SwingWorker<Map<String, Font>, Void> worker;

    public static void loadFonts() {
	worker = new SwingWorker<Map<String, Font>, Void>() {

	    @Override
	    protected void done() {
		try {
		    fontMap = get();
		    System.out.println("Fonts loaded.");

		} catch (InterruptedException e) {
		    e.printStackTrace();
		} catch (ExecutionException e) {
		    e.printStackTrace();
		}
	    }

	    @Override
	    protected Map<String, Font> doInBackground() throws Exception {
		System.out.println("Loading fonts...");
		Map<String, Font> fonts = new HashMap<String, Font>();

		fonts.put("timerFont", new Font("Ubuntu", Font.BOLD, 40));
		fonts.put("headerFont", new Font("Ubuntu", Font.BOLD, 24));
		fonts.put("buttonFont", new Font("Ubuntu", Font.BOLD, 30));
		fonts.put("pointsFont", new Font("Consolas", Font.BOLD, 30));

		return fonts;
	    }

	};
	worker.run();
    }

    public static Font getFont(String key) {
	return fontMap.get(key);
    }

}
