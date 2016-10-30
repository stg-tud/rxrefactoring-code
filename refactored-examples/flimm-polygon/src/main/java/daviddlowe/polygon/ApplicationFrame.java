package daviddlowe.polygon;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import daviddlowe.polygon.DrawArea.ModeType;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * The main window of the application.
 * @author David Lowe
 *
 */
public class ApplicationFrame extends JFrame implements ActionListener, DrawAreaListener {
	/**
	 * All classes that implement Serializable should have a serialVersionUID, even though
	 * I am not going to be serialising any objects in this project. Please ignore this
	 * field from now on. */
	private static final long serialVersionUID = -2567679034285986329L;

	/** The area of the window where the drawing takes place, a sub-class of JPanel */
	private DrawArea drawArea;
	
	/**
	 * This button will have the label "Straighten" or "Back" depending on whether the drawArea's
	 * mode is DRAWING or STRAIGHT
	 */
	private JButton swapButton;
	
	/**
	 * This button will clear the window and allow the user to restart.
	 */
	private JButton clearButton;
	
	
	public ApplicationFrame() {
		super("Polygon coursework");
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			System.err.println("Error setting native look and feel");
			e.printStackTrace();
		}
		
		
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());		
		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getContentPane().add(pane);
		
		drawArea = new DrawArea();
		drawArea.addListener(this);
		pane.add(drawArea, BorderLayout.CENTER);
		
		swapButton = new JButton("Straighten");
		swapButton.addActionListener(this);
		pane.add(swapButton, BorderLayout.NORTH);
		
		clearButton = new JButton("Clear");
		clearButton.setEnabled(false);
		clearButton.addActionListener(this);
		pane.add(clearButton, BorderLayout.SOUTH);
		
		setSize(600, 600);
		setResizable(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == swapButton) {
			clearButton.setEnabled(false);
			swapButton.setEnabled(false);
			// Swap from DRAWING to STRAIGHT modes and vice-versa
			drawArea.swap();
		} else if (e.getSource() == clearButton) {
			clear();
		}
	}
	
	/**
	 * Clear the drawArea.
	 */
	private void clear() {
		Observable.fromCallable(()-> clearSync())
				.subscribeOn(Schedulers.computation())
				.observeOn(Schedulers.immediate())
				.doOnCompleted(() -> updateGui())
				.subscribe();
	}

	// RxRefactoring: extract SpringWorker.doInBackground() as Method if number of lines > 1
	private Void clearSync()
	{
		drawArea.clear();
		return null;
	}

	// RxRefactoring: extract SpringWorker.done() as Method if number of lines > 1
	private void updateGui()
	{
		clearButton.setEnabled(false);
		swapButton.setText("Straighten");
		drawArea.repaint();
	}

	@Override
	public void lineAdded() {
		clearButton.setEnabled(true);
	}
	
	@Override
	public void swapped() {
		// Make sure the label of swapButton is up-to-date.
		if (drawArea.getMode() == ModeType.DRAWING) {
			swapButton.setText("Straighten");
		} else {
			swapButton.setText("Back");
		}
		swapButton.setEnabled(true);
		clearButton.setEnabled(true);
	}
	
	@Override
	public void drawAreaError(Throwable cause) {
		JOptionPane.showMessageDialog(this, "Error", "Polygon coursework", JOptionPane.ERROR_MESSAGE);
		cause.printStackTrace();
	}
	
	@Override
	public void invalidPolygonError() {
		// Not polygons, show error message
		int rv = JOptionPane.showOptionDialog(this, 
				"Invalid polygons",
				"Polygon coursework",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				new Object[] {"Erase drawing", "Try again"},
				"Try again");
		if (rv == 0) {
			clear();
		}
	}
	
	@Override
	public void noLinesError() {
		JOptionPane.showMessageDialog(this, "Nothing drawn on the window", "Polygon coursework",
				JOptionPane.ERROR_MESSAGE);
	}
}
