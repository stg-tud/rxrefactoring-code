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
public class RefundTest {

    public RefundTest() {
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
     * Test of payRefund method, of class Refund.
     */
    @Test
    public void testPayRefund() {
        System.out.println("payRefund");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State result, expResult;
        int choice, balance, reset;

        for (choice = 1; choice <= drinks.length; choice++) {
            balance = 5;
            reset = 1;
            result = new Ready(drinks, prices);
            result.choice = choice;
            result = new Payment(result, dm);
            result.balance = balance;
            result.reset = reset;
            result = new Refund(result);
            result.payRefund(dm);
            result = dm.getState();
            expResult = new Ready(drinks, prices);
            assertEquals(expResult, result);

            balance = 10;
            reset = 0;
            result = new Ready(drinks, prices);
            result.choice = choice;
            result = new Payment(result, dm);
            result.balance = balance;
            result.reset = reset;
            result = new Refund(result);
            result.payRefund(dm);
            result = dm.getState();
            expResult.choice = choice;
            expResult = new Payment(expResult, dm);
            expResult.balance = balance;
            expResult = new Brew(expResult);
            assertEquals(expResult, result);
        }
    }
}
