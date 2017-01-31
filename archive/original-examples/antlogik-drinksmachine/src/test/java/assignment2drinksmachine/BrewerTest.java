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
 * @author manuelmaestrini
 */
public class BrewerTest {

    public BrewerTest() {
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
     * Test of brew method, of class Brewer.
     */
    @Test
    public void testBrew() throws InterruptedException {
        System.out.println("brew");
        String[] drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        int[] prices = new int[]{5, 8, 10};
        DrinksMachine dm = new DrinksMachine(drinks, prices);
        State rdy = dm.getState();
        rdy.balance = 5;
        rdy.choice = 1;
        dm.setState(new Brew(rdy));
        Brewer brewer = new Brewer();
        State expResult = new DrinkReady(rdy);
        expResult.choice = 1;
        expResult.balance = 5;
        brewer.brew(dm);
        synchronized(brewer){
            brewer.wait(11000);
        }
        State result = dm.getState();
        assertNotNull("Search timed out", brewer);
        assertEquals(expResult, result);
    }

}