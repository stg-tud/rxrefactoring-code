/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2drinksmachine;

/**
 *The purpose of this class is to manage the output messages.
 * @author Jeppe Laursen & Manuel Maestrini
 */
public class Output {

    /**
     * This method shows the message that the caller method passes.
     * @param msg The string that has to be shown to the user.
     * @param addLine true when wanting to add the string to the current text.
     */
    public static void printMessage(String msg, boolean addLine) {
        try {
            DrinksMachineApp.mframe.setOutputMsg(msg, addLine);
        } catch (Exception e) {
        }

    }

    /**
     * This method opens or closes the hatch based on the
     * status passed by the caller method.
     * @param status is an int variable that holds the value '0' when the hatch
     * is closed and '1' when the hatch is open.
     */
    public static void hatch(int status) {
        try {
            switch (status) {
                case 0:
                    DrinksMachineApp.mframe.closeTray();
                    break;
                case 1:
                    DrinksMachineApp.mframe.openTray();
                    break;
                default:
            }
        } catch (Exception e) {
        }
    }
}
