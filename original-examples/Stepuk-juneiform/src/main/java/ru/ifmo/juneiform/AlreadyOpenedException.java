package ru.ifmo.juneiform;

/**
 *
 * @author Ivan Stepuk
 */
public class AlreadyOpenedException extends Exception {

    /**
     * Creates a new instance of <code>AlreadyOpenedException</code> without detail message.
     */
    public AlreadyOpenedException() {
    }


    /**
     * Constructs an instance of <code>AlreadyOpenedException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AlreadyOpenedException(String msg) {
        super(msg);
    }
}
