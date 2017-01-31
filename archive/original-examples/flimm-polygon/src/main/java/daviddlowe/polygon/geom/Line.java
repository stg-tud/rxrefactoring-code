package daviddlowe.polygon.geom;

import java.awt.Graphics;

/**
 * A Line object is a straight or a curved line. Its defining properties are a beginning and end point.
 * @author David Lowe
 *
 */
public abstract class Line {
	/**
	 * Return a representation of this Line.
	 * @return
	 */
	public String toString() {
		return "startPoint: " + getStartPoint().x + ", " + getStartPoint().y + " endPoint: " + getEndPoint().x + ", " + getEndPoint().y;

	}
	
	/**
	 * Return whether the start or end point of this Line are near the start or end points of the
	 * other Line.
	 * @param otherLine The other Line to compare to.
	 * @return
	 */
	public boolean isConnectedTo(Line otherLine) {
		return (getStartPoint().isNear(otherLine.getStartPoint()) || 
				getStartPoint().isNear(otherLine.getEndPoint()) || 
				getEndPoint().isNear(otherLine.getStartPoint()) ||
				getEndPoint().isNear(otherLine.getEndPoint()));
	}
	
	/**
	 * @return A duplicate copy of this Line object.
	 */
	public abstract Line copy();
	
	/**
	 * @return The first point of this Line.
	 */
	public abstract Point getStartPoint();
	
	/**
	 * @return The last point of this Line.
	 */
	public abstract Point getEndPoint();
	
	/**
	 * Reverse the order of the points of the Line, so that the start point becomes
	 * the end point and vice-versa.
	 */
	public abstract void reverse();
	
	/**
	 * Paint this Line.
	 * @param g The Graphics canvas to paint the Line on.
	 */
	public abstract void paint(Graphics g);

}
