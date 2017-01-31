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
public class BrewTest {

    public BrewTest() {
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
     * Test of brewReady method, of class Brew.
     */
    @Test
    public void testBrewReady() {
        System.out.println("brewReady");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State rdy = new Ready(drinks, prices);
        Brew brew = new Brew(rdy);
        brew.brewReady(dm);
        State expResult = new DrinkReady(rdy);
        State result = dm.getState();
        assertEquals(expResult, result);
    }

    /**
     * Test of brewDrink method, of class Brew.
     */
    @Test
    public void testBrewDrink() throws InterruptedException {
        System.out.println("brewDrink");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State rdy = new Ready(drinks, prices);
        rdy.balance = 5;
        rdy.choice = 1;
        Brew brew = new Brew(rdy);
        dm.setState(brew);
        brew.brewDrink(dm, true);
        synchronized(brew){
            brew.wait(11000);
        }
        State expResult = new DrinkReady(rdy);
        State result = dm.getState();
        assertEquals(expResult, result);
    }
}