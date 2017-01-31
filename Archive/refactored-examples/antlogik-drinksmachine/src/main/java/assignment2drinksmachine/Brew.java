package assignment2drinksmachine;

/**
 * This is the Brew class. The purpose of this class is to brew the drink and
 * to open the hatch in order to allow the user to take the drink.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class Brew extends State {

    /**
     * This constructor passes the current state variables to the super class
     * constructor.
     * @param state is the current state
     */
    public Brew(State state) {
        super(state.reset, state.balance, state.prices, state.drinks, state.choice);
    }

    @Override
    public void inputCoin(int value, DrinksMachine dm) {
    }

    /**
     * This method opens the hatch, displays the message informing the user
     * that the drink is ready and changes the state to DrinkReady.
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    @Override
    public void brewReady(DrinksMachine dm) {
        dm.toggleHatch(1);
        dm.drinkReadyMessage();
        dm.setState(new DrinkReady(this));
    }

    @Override
    public void drinkTaken(DrinksMachine dm) {
    }

    @Override
    public void cancel(DrinksMachine dm) {
    }

    @Override
    public void selection(int choice, DrinksMachine dm) {
    }

    /**
     * This method shows a message that inform the user that the machine is
     * preparing the drink and calls the brew method in the brewer class.
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     * @param addLine true when adding the string to the current text.
     */
    @Override
    public void brewDrink(DrinksMachine dm, boolean addLine) {
        dm.preparingDrinkMessage(addLine);
        dm.brewer.brew(dm);
    }

    @Override
    public void payRefund(DrinksMachine dm) {
    }
}
