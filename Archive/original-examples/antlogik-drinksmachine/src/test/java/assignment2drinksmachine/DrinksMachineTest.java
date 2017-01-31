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
public class DrinksMachineTest {

    public DrinksMachineTest() {
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
     * Test of selection method, of class DrinksMachine.
     */
    @Test
    public void testSelection() {
        System.out.println("selection");
        String[] drinks = new String[]{"Coffee","Caffe Latte","Cappuccino"};
        int[] prices = new int[]{5,8,10};
        DrinksMachine dm = new DrinksMachine(drinks,prices);

        State expResult = new Ready(drinks,prices);
        State result = dm.getState();
        assertEquals(expResult, result);

        dm.selection(2);
        expResult.choice = 2;
        expResult=new Payment(expResult, dm);
        
        result = dm.getState();
        assertEquals(expResult, result);
    }


    /**
     * Test of inputCoin method, of class DrinksMachine.
     */
    @Test
    public void testInputCoin() {
        System.out.println("inputCoin");
        String[] drinks = new String[]{"Coffee","Caffe Latte","Cappuccino"};
        int[] prices = new int[]{5,8,10};
        DrinksMachine dm = new DrinksMachine(drinks,prices);
        Ready rdy = new Ready(drinks,prices);

        dm.selection(1);
        rdy.choice=1;
        State expResult = new Payment(rdy, dm);
        State result = dm.getState();
        assertEquals(expResult, result);

        expResult.balance=5;
        dm.inputCoin(5);
        expResult=new Brew(expResult);
        result = dm.getState();
        assertEquals(expResult, result);
    }

    /**
     * Test of cancel method, of class DrinksMachine.
     */
    @Test
    public void testCancel() {
        System.out.println("cancel");
        String[] drinks = new String[]{"Coffee","Caffe Latte","Cappuccino"};
        int[] prices = new int[]{5,8,10};
        DrinksMachine dm = new DrinksMachine(drinks,prices);
        Ready rdy = new Ready(drinks,prices);

        dm.selection(2);
        rdy.choice = 2;
        State expResult = new Payment(rdy, dm);
        State result = dm.getState();
        assertEquals(expResult, result);

        dm.cancel();
        expResult=new Ready(drinks,prices);
        result = dm.getState();
        assertEquals(expResult, result);

        rdy.balance = 5;
        dm.cancel();
        expResult=new Ready(drinks,prices);
        result = dm.getState();
        assertEquals(expResult, result);
    }


    /**
     * Test of setState method, of class DrinksMachine.
     */
    @Test
    public void testSetState() {
        System.out.println("setState");

        String[] drinks = new String[]{"Coffee","Caffe Latte","Cappuccino"};
        int[] prices = new int[]{5,8,10};
        State ready = new Ready(drinks, prices);
        DrinksMachine dm = new DrinksMachine(drinks,prices);
        
        State expResult = new Ready(drinks,prices);
        State result = dm.getState();
        assertEquals(expResult, result);

        dm.selection(1);
        expResult.choice = 1;
        expResult = new Payment(expResult, dm);
        result = dm.getState();
        assertEquals(expResult, result);

        dm.setState(ready);
        expResult = new Ready(drinks,prices);
        result = dm.getState();
        assertEquals(expResult, result);

        dm.selection(2);
        expResult.choice = 2;
        expResult = new Payment(expResult, dm);
        result = dm.getState();
        assertEquals(expResult, result);

    }

    /**
     * Test of getStatus method, of class DrinksMachine.
     */
    @Test
    public void testGetState() {
        System.out.println("getStatus");
        String[] drinks = new String[]{"Coffee","Caffe Latte","Cappuccino"};
        int[] prices = new int[]{5,8,10};
        DrinksMachine dm = new DrinksMachine(drinks,prices);
        State expResult = new Ready(drinks,prices);
        State result = dm.getState();
        assertEquals(expResult,result);

        dm.selection(2);
        expResult.choice = 2;
        expResult=new Payment(expResult,dm);
        result=dm.getState();
        assertEquals(expResult, result);
    }

}