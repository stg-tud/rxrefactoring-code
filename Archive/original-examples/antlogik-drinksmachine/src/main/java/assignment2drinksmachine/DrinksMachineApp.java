package assignment2drinksmachine;

/**
 * This class is the main UI used for implementing the DrinksMachine.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class DrinksMachineApp {

    public static DrinksMachine dm;
    public static String[] drinks;
    public static int[] prices;
    public static frameMain mframe;

    public static void main(String[] args) {
        drinks = new String[]{"Coffee", "Caffe Latte", "Cappuccino"};
        prices = new int[]{5, 8, 10};

        mframe = new frameMain();
        dm = new DrinksMachine(drinks, prices);
    }
}
