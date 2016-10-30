package daviddlowe.polygon.geom;

import java.awt.event.MouseEvent;

public class Point {
	public int x, y;
	public static final int FORGIVENESS_RADIUS = 20;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(MouseEvent event) {
		x = event.getX();
		y = event.getY();
	}
	
	public Point(Point otherPoint) {
		x = otherPoint.x;
		y = otherPoint.y;
	}
	
	public boolean isNear(Point otherPoint) {
		// sqrt((x2 - x1)^2 + (y2 - y1)^2
		if (Math.pow(otherPoint.x - x, 2) + Math.pow(otherPoint.y - y, 2) <= FORGIVENESS_RADIUS * FORGIVENESS_RADIUS) { 
			return true;
		}
		return false;
	}
}
