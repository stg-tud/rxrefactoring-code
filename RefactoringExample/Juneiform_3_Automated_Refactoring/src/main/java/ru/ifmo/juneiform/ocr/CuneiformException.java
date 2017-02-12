package ru.ifmo.juneiform.ocr;

/**
 *
 * @author Ivan Stepuk
 */
public class CuneiformException extends OCRException {

    /**
     * Creates a new instance of <code>CuneiformException</code> without detail message.
     */
    public CuneiformException() {
    }


    /**
     * Constructs an instance of <code>CuneiformException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CuneiformException(String msg) {
        super(msg);
    }
}
