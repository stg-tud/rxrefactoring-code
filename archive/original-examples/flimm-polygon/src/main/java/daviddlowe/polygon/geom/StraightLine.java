package daviddlowe.polygon.geom;

import java.awt.Graphics;

/**
 * A straight line.
 * @author David Lowe
 *
 */
public class StraightLine extends Line {
	private Point point1, point2;
	
	public StraightLine(Point point1, Point point2) {
		this.point1 = point1;
		this.point2 = point2;
	}
	
	public StraightLine(Line otherLine) {
		point1 = new Point(otherLine.getStartPoint());
		point2 = new Point(otherLine.getEndPoint());
	}
	
	public Line copy() {
		return new StraightLine(this);
	}
	
	public void reverse() {
		Point temp = point1;
		point1 = point2;
		point2 = temp;
	}
	
	public Point getStartPoint() {
		return point1;
	}

	public Point getEndPoint() {
		return point2;
	}
	
	/**
	 * Set the beginning point of this Line.
	 * @param point 
	 */
	public void setStartPoint(Point point) {
		point1 = point;
	}
	
	/**
	 * Set the end point of this Line.
	 * @param point
	 */
	public void setEndPoint(Point point) {
		point2 = point;
	}
	
	public void paint(Graphics g) {
		g.drawLine(point1.x, point1.y, point2.x, point2.y);
	}

}
