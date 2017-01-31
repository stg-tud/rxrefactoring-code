/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

/**
 * This state refunds any excess money in the balance.
 * It moves to Brew or Ready depending on wether cancel has been previously
 * called or not.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class Refund extends State {

    /**
     * This constructor passes the current state variables to the super class
     * constructor.
     * @param state is the current state
     */
    public Refund(State state) {
        super(state.reset, state.balance, state.prices, state.drinks, state.choice);
    }

    @Override
    public void inputCoin(int value, DrinksMachine dm) {
    }

    @Override
    public void brewReady(DrinksMachine dm) {
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
     * This method performs a refund and based on the value of the variable
     * reset moves to the Ready state or to the brew state.
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    @Override
    public void payRefund(DrinksMachine dm) {
        if (reset == 1) {
            reset = 0;
            dm.showRefundMessage(balance);
            dm.setState(new Ready(this.drinks, this.prices));
            dm.selectDrinkMessage(true);
        } else {
            reset = 0;
            dm.showRefundMessage(balance - prices[choice - 1]);
            dm.setState(new Brew(this));
            dm.brewDrink(true);
        }
    }

    @Override
    public void brewDrink(DrinksMachine dm, boolean addLine) {
    }
}
