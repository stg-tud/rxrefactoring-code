/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

import java.util.Arrays;

/**
 * The abstract State class has all the instance members needed for the drinks
 * machine to work.
 * It also has constructors for assigning the value when going from
 * state to state.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public abstract class State {

    /** Used for toggling if cancel has been called when running the Refund code.*/
    protected int reset;
    /** Stores the balance of the current purchase.*/
    protected int balance;
    /** Array of the drink prices.*/
    protected int[] prices;
    /** Array of the drink names.*/
    protected String[] drinks;
    /** Stores the users choice of drink.*/
    protected int choice;

    /**
     * Takes the names and prices for the drink drinks and makes a copy of the
     * arrays. This way you cannot change the values from outside the drinks
     * machine.
     * @param drinks Array of string with the names of the drinks in the machine.
     * @param prices Array of int with the drink prices.
     */
    public State(String[] drinks, int[] prices) {
        this.drinks = Arrays.copyOf(drinks, drinks.length);
        this.prices = Arrays.copyOf(prices, prices.length);
    }

    /**
     * Assigns the param values to the correct instance members.
     * @param reset Next states reset value
     * @param balance Next states balance value
     * @param prices Next states prices array
     * @param drinks Next states drinks array
     * @param choice Next states choice value
     */
    public State(int reset, int balance, int[] prices, String[] drinks, int choice) {
        this.balance = balance;
        this.drinks = drinks;
        this.prices = prices;
        this.reset = reset;
        this.choice = choice;
    }

    /**
     * Method for selecting a drink.
     * @param choice The users choice.
     * @param dm The drinks machine.
     */
    public abstract void selection(int choice, DrinksMachine dm);

    /**
     * Method for inputting a coin.
     * @param value The value of the inputted coin.
     * @param dm The drinks machine.
     */
    public abstract void inputCoin(int value, DrinksMachine dm);

    /**
     * Called when the brew is ready.
     * @param dm The drinks machine.
     */
    public abstract void brewReady(DrinksMachine dm);

    /**
     * Called when the user takes the drink.
     * @param dm The drinks machine.
     */
    public abstract void drinkTaken(DrinksMachine dm);

    /**
     * Will make the state machine brew the drink that has been chosen.
     * @param dm The drinks machine.
     * @param addLine true if the string has to be added to the current text.
     */
    public abstract void brewDrink(DrinksMachine dm, boolean addLine);

    /**
     * Refunds the appropriate amount of money.
     * @param dm The drinks machine.
     */
    public abstract void payRefund(DrinksMachine dm);

    /**
     * Cancels the current transaction.
     * @param dm The drinks machine.
     */
    public abstract void cancel(DrinksMachine dm);

    /**
     * Compares the parsed objects variables with the owners variables.
     * @param obj Object to be compared.
     * @return True or false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final State other = (State) obj;
        if (this.reset != other.reset) {
            return false;
        }
        if (this.balance != other.balance) {
            return false;
        }
        if (!Arrays.equals(this.prices, other.prices)) {
            return false;
        }
        if (!Arrays.deepEquals(this.drinks, other.drinks)) {
            return false;
        }
        if (this.choice != other.choice) {
            return false;
        }
        return true;
    }

    /**
     * Creates the hash code based on variables.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.reset;
        hash = 41 * hash + this.balance;
        hash = 41 * hash + Arrays.hashCode(this.prices);
        hash = 41 * hash + Arrays.deepHashCode(this.drinks);
        hash = 41 * hash + this.choice;
        return hash;
    }
}

