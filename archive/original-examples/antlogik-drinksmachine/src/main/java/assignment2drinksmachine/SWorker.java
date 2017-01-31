/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

import javax.swing.SwingWorker;

/**
 * A timer class used for delaying certain operations.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class SWorker extends SwingWorker {

    private DrinksMachine dm;

    /**
     * Assigns the param to the private instance member so that it can be used by the done() method.
     * @param dm DrinksMachine object used to make a callback to continue the state machine.
     */
    public SWorker(DrinksMachine dm) {
        this.dm = dm;
    }

    @Override
    protected Object doInBackground() throws Exception {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected void done() {
        this.dm.selectDrinkMessage(false);
    }
}
