package daviddlowe.polygon;

/**
 * The interface for all observers of DrawAreas.
 * @author David Lowe
 *
 */
public interface DrawAreaListener {
	
	/**
	 * This method is called whenever a new line has been drawn.
	 */
	public void lineAdded();
	
	/**
	 * Some exceptions in DrawArea's methods are caught and passed to this method for handling.
	 * @param cause
	 */
	public void drawAreaError(Throwable cause);
	
	/**
	 * Once the DrawArea's mode has been swapped from DRAWING to STRAIGHT or vice-versa, this 
	 * method is called (even if the attempted swap failed).
	 */
	public void swapped();
	
	/**
	 * If the lines drawn by the user do not make a polygon when a swap is requested, this method
	 * is called in addition to swapped().
	 */
	public void invalidPolygonError();
	
	/**
	 * If no lines have been drawn by the user when a swap is requested, this method is called in
	 * addition to swapped().
	 */
	public void noLinesError();
	
	
}
