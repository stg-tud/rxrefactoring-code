package assignment2drinksmachine;

/**
 * This is the state machines main class.
 * All input from the user goes through here.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class DrinksMachine {

    /** This State object stores the current state of the state machine */
    protected State state;
    /** This is a class used for delaying the Brew state. */
    protected Brewer brewer;

    /**
     * This constructor creates a Ready object, a Brewer object and throws
     * a message to the UI.
     * @param drinks Array of strings to hold the names of the drinks.
     * @param prices Array of int to hold the prices of the drinks
     */
    public DrinksMachine(String[] drinks, int[] prices) {
        this.state = new Ready(drinks, prices);
        this.brewer = new Brewer();
        selectDrinkMessage(false);
    }

    /**
     * This is an input for the state machine. It is used for passing the
     * users choice of drink to the current state.
     * @param choice The integer choice from the user.
     */
    public void selection(int choice) {
        state.selection(choice, this);
    }

    /**
     * Input to the state machine to signal that the
     * Brewer has finished preparing the drink
     */
    public void brewReady() {
        state.brewReady(this);
    }

    /**
     * Input from the UI that the user has taken the drink from the tray.
     */
    public void drinkTaken() {
        state.drinkTaken(this);
    }

    /**
     * Input from the UI that passes the coin value that the user has inserted.
     * @param value
     */
    public void inputCoin(int value) {
        state.inputCoin(value, this);
    }

    /**
     * Input to tell the state machine to move to the Brew state
     * @param addLine true when wanting to add a string to the current text.
     */
    public void brewDrink(boolean addLine) {
        state.brewDrink(this, addLine);
    }

    /**
     * Input that tells the state machine to pay out the refund.
     */
    public void payRefund() {
        state.payRefund(this);
    }

    /**
     * Input for cancelling the current transaction.
     * This will only give a result in the Ready and Payment states.
     */
    public void cancel() {
        state.cancel(this);
    }

    /**
     * Used for creating an output message telling the user the drink is ready to be taken.
     */
    public void drinkReadyMessage() {
        Output.printMessage("Your drink is ready.\nPlease take it from the tray.", false);
    }

    /**
     * Used for creating an output message telling the user to select a drink.
     * @param addLine true when wanting to add a string to the current text.
     */
    public void selectDrinkMessage(boolean addLine) {
        Output.printMessage("Please select a drink:", addLine);
    }

    /**
     * Used for creating an output message to show how much money to insert.
     * @param value The amount of money to be inserted
     * @param addLine true if wanting to add the string to the current text.
     */
    public void insertMoneyMessage(int value,boolean addLine) {
        Output.printMessage("Insert " + value + " kr:", addLine);
    }

    /**
     * Used for creating an output message telling the user that the drink is being prepared.
     * @param addLine true if wanting to add the string to the current text.
     */
    public void preparingDrinkMessage(boolean addLine) {
        Output.printMessage("Preparing drink. Please wait...", addLine);
    }

    /**
     * Used for creating an output message telling the user how much money has been refunded.
     * @param value Amount of money that has been refunded.
     */
    public void showRefundMessage(int value) {
        Output.printMessage("Refund: " + value + " kr.", false);
    }

    /**
     * Used for creating an output message showing the current balance.
     * @param value The amount of money in the current balance.
     */
    public void showCreditMessage(int value) {
        Output.printMessage("Credit: " + value + " kr.", false);
    }

    /**
     * Assigns the next state to the current state.
     * @param state Next state.
     */
    protected void setState(State state) {
        this.state = state;
    }

    /**
     * Used for outputting an error message.
     * @param msg Error message to print
     */
    public void error(String msg) {
        Output.printMessage(msg, false);
    }

    /**
     * This method is used for opening and closing the hatch.
     * @param status 0 for closed hatch, 1 for open hatch.
     */
    public void toggleHatch(int status) {
        Output.hatch(status);
    }

    /**
     * Get the current state.
     * @return State object is returned.
     */
    public State getState() {
        return state;
    }
}




