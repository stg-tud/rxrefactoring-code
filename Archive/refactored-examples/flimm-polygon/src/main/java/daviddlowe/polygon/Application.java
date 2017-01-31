package daviddlowe.polygon;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.util.concurrent.*;


/**
 * Run the application.
 * @author David Lowe
 *
 */
public class Application {
	/** If this variable is set to true, various debugging information will be printed to stdout. */
	public static boolean debug = false;
	
	/** If this variable is set to true, Thread.sleep will be called in some complex methods
	 * to simulate a slow computer and to check that the threading works as intended.
	 */
	public static boolean debugging_delays = false;
	
	
	/** Run SwingWorkers sequentially */
	// RxRefactoring: executor service no longer needed
//	public static ExecutorService sequentialExecutorService = Executors.newSingleThreadExecutor();
	
	
	public static void main(String args[]) {
		
		for (String arg: args) {
			if (arg.equalsIgnoreCase("--debug")) {
				debug = true;
			} else if (arg.equalsIgnoreCase("--delays")) {
				debugging_delays = true;
			}
		}
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				ApplicationFrame frame = new ApplicationFrame();
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});

	}
}
