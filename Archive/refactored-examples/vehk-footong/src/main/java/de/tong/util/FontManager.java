package de.tong.util;

import rx.Observable;
import rx.schedulers.Schedulers;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FontManager {

    public static Map<String, Font> fontMap = new HashMap<String, Font>();

    public static void loadFonts() {

		// RxRefactoring
		Observable
				.fromCallable(() -> doInBackground()) // fromCallable will always return one emission
				.subscribeOn(Schedulers.computation())
				.observeOn(Schedulers.immediate())
				.doOnNext(asyncResult -> done(asyncResult)) // use onNext instead of onCompleted, because the first emission is already the result from doInBackground
				.subscribe();
    }

    // RxRefactoring: changed to static, because it is called from a static method
	private static Map<String, Font> doInBackground() throws Exception {
		System.out.println("Loading fonts...");
		Map<String, Font> fonts = new HashMap<String, Font>();

		fonts.put("timerFont", new Font("Ubuntu", Font.BOLD, 40));
		fonts.put("headerFont", new Font("Ubuntu", Font.BOLD, 24));
		fonts.put("buttonFont", new Font("Ubuntu", Font.BOLD, 30));
		fonts.put("pointsFont", new Font("Consolas", Font.BOLD, 30));

		return fonts;
	}

	// RxRefactoring: parameter added due to the "get()" method, try catch block removed (needed because of get())
	// RxRefactoring: changed to static, because it is called from a static method
	private static void done(Map<String, Font> result) {
			fontMap = result;
			System.out.println("Fonts loaded.");
	}

    public static Font getFont(String key) {
	return fontMap.get(key);
    }

}
