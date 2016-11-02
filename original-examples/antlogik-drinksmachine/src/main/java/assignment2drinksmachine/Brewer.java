package assignment2drinksmachine;

import javax.swing.SwingWorker;

/**
 *The purpose of this class is to simulate the waiting time for the machine to
 * prepare the drink, by using a timer.
 *  @author Jeppe Laursen & Manuel Maestrini
 */
public class Brewer {

    private DrinksMachine dm;

    /**
     * This method make the current thread sleep for 10 seconds
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    public void brew(DrinksMachine dm) {

        this.dm = dm;

        SWorker worker = new SWorker();
        worker.execute();
        

    }

    private class SWorker extends SwingWorker {

        @Override
        protected Object doInBackground() throws Exception {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void done() {
            dm.brewReady();
        }
    }
}
