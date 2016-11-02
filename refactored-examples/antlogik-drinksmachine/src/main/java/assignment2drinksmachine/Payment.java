/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

/**
 * This class is the link between the ready state and the brew/refund state.
 * The purpose of this class is to allow the user to insert money or cancel a
 * purchase.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class Payment extends State {

    /**
     * This constructor passes the current state variables to the super class
     * constructor, shows a message displaying the drink selected and shows the
     * price of that drink.
     * @param state is the current state
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    public Payment(State state, DrinksMachine dm) {
        super(state.reset, state.balance, state.prices, state.drinks,
                state.choice);
        Output.printMessage(drinks[choice - 1] + " selected", false);
        dm.insertMoneyMessage(prices[choice - 1] - balance,true);
    }

    @Override
    public void selection(int choice, DrinksMachine dm) {
    }

    /**
     * This method prevents the user from input a wrong coin by performing a
     * control on the value of the input coin. If a coin different from 1, 2, 5
     * 10 or 20 kr. is insert, the user will be informed with a message and will
     * be asked to insert a coin;
     * @param value is an int variable holding the value of the coin input by the
     * user
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    @Override
    public void inputCoin(int value, DrinksMachine dm) {
        int price = prices[choice - 1];
        if (value == 1 || value == 2 || value == 5 || value == 10 ||
                value == 20) {
            balance += value;
            if (balance == price) {
                dm.setState(new Brew(this));
                dm.brewDrink(false);
            } else if (balance > price) {
                this.reset = 0;
                dm.setState(new Refund(this));
                dm.payRefund();
            } else {
                dm.insertMoneyMessage(price - balance,false);
            }
        } else {
            dm.error("You have input an invalid coin!");
            dm.insertMoneyMessage(price - balance,true);
        }

    }

    @Override
    public void brewReady(DrinksMachine dm) {
    }

    @Override
    public void drinkTaken(DrinksMachine dm) {
    }

    /**
     * This method cancels the current transaction. If the user has input any
     * money during the transaction it will be refunded.
     * The state machine will go back to the Ready state in the end.
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    @Override
    public void cancel(DrinksMachine dm) {
        if (balance == 0) {
            dm.setState(new Ready(drinks, prices));
            dm.selectDrinkMessage(false);
        } else {
            SWorker worker = new SWorker(dm);
            this.reset = 1;
            dm.setState(new Refund(this));
            dm.payRefund();
            worker.execute();
        }
    }

    @Override
    public void brewDrink(DrinksMachine dm, boolean addLine) {
    }

    @Override
    public void payRefund(DrinksMachine dm) {
    }
}
