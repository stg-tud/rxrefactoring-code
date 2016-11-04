import java.util.Arrays;

/**
 * Rotates an array from the middle(defined) position.
 * 
 * <pre>
 * input : [ 1, 2, 3, 4, 5, a, b, c]
 * output : [a, b, c, 1, 2, 3, 4, 5]
 * 
 * @author Rahul Jain
 * 
 */

public class ArrayRotater {

    public static void main(String[] args) {
        char[] arr = { '1', '2', '3', '4', '5', 'a', 'b', 'c' };
        ArrayRotater rotater = new ArrayRotater();
        rotater.rotate(arr, 0, 5, 8);
        System.out.println(Arrays.toString(arr));
    }

    public void rotate(char[] arr, int first, int middle, int end) {
        int next = middle;
        while (first != next) {
            swap(arr, first++, next++);
            if (next == end) {
                next = middle;
            } else if (first == middle) {
                middle = next;
            }
        }
    }

    private void swap(char[] arr, int c1, int c2) {
        char temp = arr[c1];
        arr[c1] = arr[c2];
        arr[c2] = temp;
    }
}
