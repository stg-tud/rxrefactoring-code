/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

/**
 * This class, once the user takes the drink, shows the seòectdrink message and
 * sets the state back to Ready.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class DrinkReady extends State {

    /**
     *This constructor passes the current state variables to the super class
     * constructor.
     * @param state is the current state
     */
    public DrinkReady(State state) {
        super(state.reset, state.balance, state.prices, state.drinks, state.choice);
    }

    @Override
    public void inputCoin(int value, DrinksMachine dm) {
    }

    @Override
    public void brewReady(DrinksMachine dm) {
    }

    /**
     * This method closes the hatch, display a message informing the user to
     * choose a drink and changes the state to Ready.
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    @Override
    public void drinkTaken(DrinksMachine dm) {
        dm.toggleHatch(0);
        dm.selectDrinkMessage(false);
        dm.setState(new Ready(this.drinks, this.prices));
    }

    @Override
    public void cancel(DrinksMachine dm) {
    }

    @Override
    public void selection(int choice, DrinksMachine dm) {
    }

    @Override
    public void brewDrink(DrinksMachine dm, boolean addLine) {
    }

    @Override
    public void payRefund(DrinksMachine dm) {
    }
}
