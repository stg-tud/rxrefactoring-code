package de.jugmuenster.swingbasics.swingworker.calculation;

/**
 * Used to provide gui feedback before the calculation is started. Disables
 * gui elements to avoid re-clicking.
 */
public class CalculationStarted extends ThreadSafeFeedback {

    private final Calculate calculate;

    CalculationStarted(Calculate calculate) {
        this.calculate = calculate;
    }

    public void run() {
        this.calculate.swingWorkerDemo.withoutSwingWorkerButton
    	    .setEnabled(false);
        this.calculate.swingWorkerDemo.withSwingWorkerButton
    	    .setEnabled(false);
        this.calculate.swingWorkerDemo.textarea.setText("STARTED!\n");
	this.calculate.swingWorkerDemo.textarea
		.append("Buttons should be disabled!\n");
	this.calculate.swingWorkerDemo.textarea
		.append("Please try to resize the window now!\n");
    }
}