package ru.ifmo.juneiform;

/**
 *
 * @author Ivan Stepuk
 */
public enum Language {

    ENGLISH("eng"),
    RUSSIAN("rus"),
    RUSSIAN_AND_ENGLISH("ruseng");
    private String shortening;

    private Language(String shortening) {
        this.shortening = shortening;
    }

    public String getShortening() {
        return shortening;
    }
}
