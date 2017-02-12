package ru.ifmo.juneiform;

import java.util.prefs.Preferences;

/**
 *
 * @author Oleg "xplt" Kuznetsov
 */
public class Settings {
    
    private Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    private boolean customPath;
    private boolean firstRun;
    private String pathToCuneiform;
    private int recognitionLanguageId;
    
    public Settings() {
        load();
    }

    public boolean isCustomPath() {
        return customPath;
    }
    
    public boolean isFirstRun() {
        return firstRun;
    }

    public void setCustomPath(boolean customPath) {
        this.customPath = customPath;
    }
    
    public String getPathToCuneiform() {
        return pathToCuneiform;
    }

    public void setPathToCuneiform(String pathToCuneiform) {
        this.pathToCuneiform = pathToCuneiform;
    }

    public int getRecognitionLanguageId() {
        return recognitionLanguageId;
    }

    public void setRecognitionLanguage(int recognitionLanguage) {
        this.recognitionLanguageId = recognitionLanguage;
    }
    
    public void save() {        
        if (isCustomPath()) {
            preferences.putBoolean("custom", true);
            preferences.put("path", pathToCuneiform);
        } else {
            preferences.putBoolean("custom", false);
            preferences.put("path", "cuneiform");
        }
        preferences.putInt("lang", recognitionLanguageId);
    }
    
    /**
     * If program is running for the first time, then fill Settings with the
     * default values
     */
    private void load() {
        firstRun = preferences.getBoolean("launched", true);
        
        if (!firstRun) {
            customPath = preferences.getBoolean("custom", false);

            if (isCustomPath()) {
                pathToCuneiform = preferences.get("path", "cuneiform");
            } else {
                pathToCuneiform = "cuneiform";
            }
            recognitionLanguageId = preferences.getInt("lang", 0);
        } else {
            preferences.putBoolean("custom", false);
            preferences.put("path", "cuneiform");
            preferences.putInt("lang", 0);
            preferences.putBoolean("first", false);
            pathToCuneiform = preferences.get("path", "cuneiform");
            firstRun = false;
        }
    }
}
