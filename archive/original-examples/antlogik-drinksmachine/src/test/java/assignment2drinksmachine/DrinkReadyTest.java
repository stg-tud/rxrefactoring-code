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
public class DrinkReadyTest {

    public DrinkReadyTest() {
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
     * Test of drinkTaken method, of class DrinkReady.
     */
    @Test
    public void testDrinkTaken() {
        System.out.println("drinkTaken");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State result = new Ready(drinks, prices);
        State expResult = new DrinkReady(new Ready(drinks, prices));
        expResult.choice = 2;
        expResult.balance = 10;
        result.choice = 2;
        result.balance = 10;
        result = new DrinkReady(result);
        assertEquals(expResult, result);
        result.drinkTaken(dm);
        assertEquals(expResult, result);
    }
}
