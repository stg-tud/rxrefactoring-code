/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeppelaursen
 */
public class ReadyTest {

    public ReadyTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of selection method, of class Ready.
     */
    @Test
    public void testSelection() {
        System.out.println("selection");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State instance = new Ready(drinks, prices);
        State expResult = new Ready(drinks, prices);

        int choice = 0;
        instance.selection(choice, dm);
        instance = dm.getState();
        assertEquals(expResult, instance);

        dm.setState(new Ready(drinks, prices));
        instance = dm.getState();
        choice = 1;
        instance.selection(choice, dm);
        instance = dm.getState();
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        assertEquals(expResult, instance);

        dm.setState(new Ready(drinks, prices));
        instance = dm.getState();
        choice = 2;
        instance.selection(choice, dm);
        instance = dm.getState();
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        assertEquals(expResult, instance);

        dm.setState(new Ready(drinks, prices));
        instance = dm.getState();
        choice = 3;
        instance.selection(choice, dm);
        instance = dm.getState();
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        assertEquals(expResult, instance);

        dm.setState(new Ready(drinks, prices));
        instance = dm.getState();
        choice = 4;
        instance.selection(choice, dm);
        instance = dm.getState();
        expResult = new Ready(drinks, prices);
        assertEquals(expResult, instance);

        dm.setState(new Ready(drinks, prices));
        instance = dm.getState();
        choice = -1;
        instance.selection(choice, dm);
        instance = dm.getState();
        expResult = new Ready(drinks, prices);
        assertEquals(expResult, instance);
    }

    /**
     * Test of inputCoin method, of class Ready.
     */
    @Test
    public void testInputCoin() {
        System.out.println("inputCoin");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        int[] coins = new int[]{0, 1, 2, 5, 10, 20, -1};
        int value;
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State instance;
        State expResult = new Ready(drinks, prices);

        /**
         * Testing for inputting individual coins.
         * Balance is reset between each assert.
         */
        for (int i = 0; i < coins.length; i++) {
            value = coins[i];
            dm.setState(new Ready(drinks, prices));
            instance = dm.getState();
            instance.inputCoin(value, dm);
            if (value == -1) {
                expResult.balance = 0;
            } else {
                expResult.balance = value;
            }
            assertEquals(expResult, instance);
        }

        /**
         * Testing for adding the coins to the balance.
         */
        expResult = new Ready(drinks, prices);
        for (int i = 0; i < coins.length; i++) {
            value = coins[i];
            instance = dm.getState();
            instance.inputCoin(value, dm);
            if (value > 0) {
                expResult.balance += value;
            }
            assertEquals(expResult, instance);
        }
    }

    /**
     * Test of cancel method, of class Ready.
     */
    @Test
    public void testCancel() {
        System.out.println("cancel");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State instance = new Ready(drinks, prices);
        State expResult = new Ready(drinks, prices);

        instance.inputCoin(5, dm);
        expResult.balance = 5;
        assertEquals(expResult, instance);

        expResult = new Ready(drinks, prices);
        instance.cancel(dm);
        instance = dm.getState();
        assertEquals(expResult, instance);
    }
}
