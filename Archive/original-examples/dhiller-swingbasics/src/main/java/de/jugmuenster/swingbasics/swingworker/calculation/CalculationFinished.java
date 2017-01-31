package de.jugmuenster.swingbasics.swingworker.calculation;

/**
 * Used to provide gui feedback after the calculation is started. Re-enables
 * gui elements.
 */
public class CalculationFinished extends ThreadSafeFeedback {

    private final Calculate calculate;

    CalculationFinished(Calculate calculate) {
        this.calculate = calculate;
    }

    @Override
    public void run() {
        this.calculate.swingWorkerDemo.withoutSwingWorkerButton
    	    .setEnabled(true);
        this.calculate.swingWorkerDemo.withSwingWorkerButton
    	    .setEnabled(true);
        this.calculate.swingWorkerDemo.textarea.append("FINISHED!\n");
    }
}