package ru.ifmo.juneiform;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author Ivan Stepuk
 */
public final class L10n {

    public static final String BUNDLE = "l10n";
    public static final String MISSING_STRING = "<?>";

    private L10n() {
    }

    public static String forString(String key) {
        String result = null;
        try {
            result = ResourceBundle.getBundle(BUNDLE).getString(key);
        } catch (MissingResourceException ex) {
            result = MISSING_STRING;
        }
        return result;
    }
}
