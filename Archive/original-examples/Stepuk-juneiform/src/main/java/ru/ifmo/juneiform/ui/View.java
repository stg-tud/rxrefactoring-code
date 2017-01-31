package ru.ifmo.juneiform.ui;

import java.awt.Cursor;
import javax.swing.JComponent;
import ru.ifmo.juneiform.Language;

/**
 *
 * @author Ivan Stepuk
 */
public interface View {

    void setStatus(String message);

    void clearStatus(String message);

    void addPreview(Preview preview);

    void removePreview(int index);

    Preview getSelectedPreview();
    
    int getSelectedPreviewIndex();

    void fillInput(JComponent component);

    void fillOutput(String text);

    String getOutput();

    void setLanguage(Language language);

    void changeCursor(Cursor cursor);

    void showErrorDialog(String title, String text);
}
