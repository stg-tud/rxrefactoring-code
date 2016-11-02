/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

/**
 * This is the first state of the state machine. And also the default state that
 * will always be returned to.
 * There are three input options in this state. selection, inputCoin and cancel.
 * Selection will go the Payment state. inputCoin will stay in the state and
 * increase the balance. A cancel input will result in a refund if any money has
 * been inserted.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class Ready extends State {

    private SWorker worker;

    /**
     * This constructor passes the current state variables to the super class
     * constructor.
     * @param state is the current state
     */
    public Ready(State state) {
        super(state.reset, state.balance, state.prices, state.drinks, state.choice);
    }

    /**
     * This constructor pass the 2 arrays to the super class constructor.
     * @param drinks Array of Strings which holds the drinks available .
     * @param prices Array of integer which holds the price of the drinks.
     */
    public Ready(String[] drinks, int[] prices) {
        super(drinks, prices);
    }

    /**
     * This method prevents eventual wrong inputs from the user, comparing
     * the drink number with the lenght of the array; In case the input number
     * is bigger than the size of the array a message will be prompt to the user
     * who will be asked again to choose a drink, otherwise it moves to the
     * next state.
     * @param choice is an int variable holding the number which identify the
     * drink chosen by the user.
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    @Override
    public void selection(int choice, DrinksMachine dm) {
        worker = new SWorker(dm);
        worker.cancel(true);
        if (choice > prices.length || choice <= 0) {
            dm.error("The selected drink does not exist.");
            dm.selectDrinkMessage(true);
        } else {
            this.choice = choice;
            int price = prices[choice - 1];
            if (balance == price) {
                dm.setState(new Brew(this));
                dm.brewDrink(false);
            } else if (balance > price) {
                this.reset = 0;
                dm.setState(new Refund(this));
                dm.payRefund();
            } else {
                dm.setState(new Payment(this, dm));
            }
        }
    }

    /**
     * This method prevents the user from input a wrong coing by performing a
     * control on the value of the input coin. If a coin different from 1, 2, 5
     * 10 or 20 kr. is insert, the user will be informed with a message and will
     * be asked to input a coin. Otherwise the input value will be added
     * to the balance and the user will see its current credit, then a message
     * asking to choose a drink will be shown.
     * @param value is an int variable holding the value of the coin input by the
     * user
     * @param dm is the VendingMachine object which allows for calling its
     * methods.
     */
    @Override
    public void inputCoin(int value, DrinksMachine dm) {
        worker = new SWorker(dm);
        worker.cancel(true);
        if (value == 1 || value == 2 || value == 5 || value == 10 || value == 20) {
            balance += value;
            dm.showCreditMessage(balance);
            dm.selectDrinkMessage(true);
        } else {
            dm.error("You have input an invalid coin!");
            dm.selectDrinkMessage(false);
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
     * @param dm is the VendingMachine object which allow us to call its
     * methods.
     */
    @Override
    public void cancel(DrinksMachine dm) {
        if (balance > 0) {
            worker = new SWorker(dm);
            this.reset = 1;
            dm.setState(new Refund(this));
            dm.payRefund();
            worker.execute();
        } else {
            dm.selectDrinkMessage(false);
        }

    }

    @Override
    public void brewDrink(DrinksMachine dm, boolean addLine) {
    }

    @Override
    public void payRefund(DrinksMachine dm) {
    }
}
