package ru.ifmo.juneiform.ocr;

/**
 *
 * @author Ivan Stepuk
 */
public class OCRException extends Exception {

    /**
     * Creates a new instance of <code>OCRException</code> without detail message.
     */
    public OCRException() {
    }


    /**
     * Constructs an instance of <code>OCRException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public OCRException(String msg) {
        super(msg);
    }
}
