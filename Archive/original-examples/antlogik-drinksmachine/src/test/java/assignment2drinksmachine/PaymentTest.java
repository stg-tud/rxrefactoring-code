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
 * @author AntLogiK
 */
public class PaymentTest {

    public PaymentTest() {
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
     * Test of inputCoin method, of class Payment.
     */
    @Test
    public void testInputCoin() {
        System.out.println("inputCoin");

        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        int value, choice;
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State result, expResult;

        choice = 1;
        value = 0;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 1;
        value = 1;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 1;
        value = 2;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 1;
        value = 5;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        expResult = new Brew(expResult);
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 1;
        value = 10;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        expResult = new Brew(expResult);
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 1;
        value = 20;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        expResult = new Brew(expResult);
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 2;
        value = 0;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 2;
        value = 1;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 2;
        value = 2;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 2;
        value = 5;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 2;
        value = 10;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        expResult = new Brew(expResult);
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 2;
        value = 20;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        expResult = new Brew(expResult);
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 3;
        value = 0;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 3;
        value = 1;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 3;
        value = 2;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 3;
        value = 5;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 3;
        value = 10;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        expResult = new Brew(expResult);
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        choice = 3;
        value = 20;
        expResult = new Ready(drinks, prices);
        expResult.choice = choice;
        expResult = new Payment(expResult, dm);
        expResult.balance = value;
        expResult = new Brew(expResult);
        dm.setState(new Ready(drinks, prices));
        dm.selection(choice);
        result = dm.getState();
        result.inputCoin(value, dm);
        result = dm.getState();
        assertEquals(expResult, result);
    }

    /**
     * Test of cancel method, of class Payment.
     */
    @Test
    public void testCancel() {
        System.out.println("cancel");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State expResult;
        State result;

        expResult = new Ready(drinks, prices);
        dm.selection(3);
        result = dm.getState();
        result.cancel(dm);
        result = dm.getState();
        assertEquals(expResult, result);

        expResult = new Ready(drinks, prices);
        dm.setState(new Ready(drinks, prices));
        dm.selection(3);
        expResult.choice=3;
        expResult = new Payment(expResult, dm);
        result = dm.getState();
        assertEquals(expResult, result);
        expResult = new Ready(drinks, prices);
        result.balance = 5;
        result.cancel(dm);
        result = dm.getState();
        assertEquals(expResult, result);

    }
}
