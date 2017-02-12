package ru.ifmo.juneiform.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.swing.JComponent;
import ru.ifmo.juneiform.L10n;
import ru.ifmo.juneiform.Language;
import ru.ifmo.juneiform.PreviewActionListener;

/**
 *
 * @author Ivan Stepuk
 */
public final class ViewImpl implements View {

    private MainFrame frame;
    private Deque<String> status = new ArrayDeque<String>();
    private int totalPreviewsHeight = Preview.VERTICAL_GAP;

    public ViewImpl(MainFrame mainFrame) {
        this.frame = mainFrame;
        setStatus(L10n.forString("status.ready"));
    }

    @Override
    public void setStatus(String message) {
        status.addLast(message);
        frame.statusBar.setText(message);
    }

    @Override
    public void clearStatus(String message) {
        status.removeLastOccurrence(message);
        frame.statusBar.setText(status.getLast());
    }

    @Override
    public void addPreview(Preview preview) {
        preview.addActionListener(new PreviewActionListener() {

            @Override
            public void actionPerformed(Preview preview) {
                Preview selectedPreview = getSelectedPreview();
                if (selectedPreview != null) {
                    selectedPreview.setSelected(false);
                }
                preview.setSelected(true);
            }
        });
        //TODO 10 - magic number
        preview.setPreferredSize(new Dimension(frame.previewsPanel.getWidth() - 10, Preview.AUTO));
        totalPreviewsHeight += (preview.getPreferredSize().height + Preview.VERTICAL_GAP);
        frame.previewsPanel.add(preview);
        frame.previewsPanel.setPreferredSize(new Dimension(frame.previewsPanel.getWidth(), totalPreviewsHeight));
        frame.previewsScrollPane.revalidate();
    }

    @Override
    public void removePreview(int index) {
        Preview preview = (Preview) frame.previewsPanel.getComponent(index);
        totalPreviewsHeight -= (preview.getPreferredSize().height + Preview.VERTICAL_GAP);
        frame.previewsPanel.remove(preview);
        frame.previewsPanel.setPreferredSize(new Dimension(frame.previewsPanel.getWidth(), totalPreviewsHeight));
        frame.previewsPanel.repaint();
        frame.previewsScrollPane.revalidate();
    }

    @Override
    public Preview getSelectedPreview() {
        for (Component component : frame.previewsPanel.getComponents()) {
            if (component instanceof Preview && ((Preview) component).isSelected()) {
                return (Preview) component;
            }
        }
        return null;
    }

    @Override
    public int getSelectedPreviewIndex() {
        Component[] components = frame.previewsPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            if (comp instanceof Preview && ((Preview) comp).isSelected()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void fillInput(JComponent component) {
        frame.inputPanel.removeAll();
        if (component != null) {
            frame.inputPanel.add(component);
        }
        frame.inputPanel.repaint();
        frame.inputScrollPane.revalidate();
    }

    @Override
    public void fillOutput(String text) {
        frame.outputTextArea.setText(text);
    }

    @Override
    public String getOutput() {
        return frame.outputTextArea.getText();
    }

    @Override
    public void setLanguage(Language language) {
        frame.languageComboBox.setSelectedIndex(language.ordinal());
    }

    @Override
    public void changeCursor(Cursor cursor) {
        frame.setCursor(cursor);
    }

    @Override
    public void showErrorDialog(String title, String text) {
        ErrorDialog dialog = new ErrorDialog(frame, true);
        dialog.setTitle(title);
        dialog.setText(text);
        dialog.setVisible(true);
    }
}
