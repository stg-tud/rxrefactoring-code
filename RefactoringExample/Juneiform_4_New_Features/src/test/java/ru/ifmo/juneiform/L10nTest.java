package ru.ifmo.juneiform;

import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ivan Stepuk
 */
public class L10nTest {

    @Test
    public void testForEnglishString() {
        Locale.setDefault(Locale.ENGLISH);
        String expected = "File";
        String actual = L10n.forString("menu.file");
        assertEquals(expected, actual);
    }
    
    @Test
    public void testForRussianString() {
        Locale.setDefault(new Locale("ru"));
        String expected = "Файл";
        String actual = L10n.forString("menu.file");
        assertEquals(expected, actual);
    }
    
    @Test
    public void testForMissingString() {
        Locale.setDefault(Locale.ENGLISH);
        String expected = "<?>";
        String actual = L10n.forString("some.missing.string");
        assertEquals(expected, actual);
    }
}
