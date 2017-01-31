package ru.ifmo.juneiform;

/**
 *
 * @author Ivan Stepuk
 */
public class DocumentException extends Exception {

    /**
     * Creates a new instance of <code>DocumentException</code> without detail message.
     */
    public DocumentException() {
    }


    /**
     * Constructs an instance of <code>DocumentException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DocumentException(String msg) {
        super(msg);
    }
}
