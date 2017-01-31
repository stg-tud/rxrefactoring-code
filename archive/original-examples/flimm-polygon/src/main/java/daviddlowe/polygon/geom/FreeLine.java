package daviddlowe.polygon.geom;

import java.awt.*;
import java.util.Vector;

/**
 * A curved line.
 * @author David Lowe
 *
 */
public class FreeLine extends Line {
	private Vector<Point> points = new Vector<Point> (5);
	
	public FreeLine() {
		
	}
	
	public Line copy() {
		FreeLine line = new FreeLine();
		for (Point point: points) {
			line.addPoint(new Point(point));
		}
		return line;
	}
	
	public Point getStartPoint() {
		return points.firstElement();
	}
	
	public Point getEndPoint() {
		return points.lastElement();
	}
	
	/**
	 * Add new Point towards which the FreeLine extends
	 * @param point
	 */
	public void addPoint(Point point) {
		points.add(point);
	}
	
	public void reverse() {
		Vector<Point> reversed = new Vector<Point>(points.size());
		for (int ii=points.size() - 1; ii >= 0; ii--) {
			reversed.add(points.get(ii));
		}
		points = reversed;
	}
	
	public void paint(Graphics g) {
		paint(g, null, false, false, false);
	}
	
	/**
	 * Paint the Line.
	 * @param g
	 * @param label Draw this String in the middle of the FreeLine. Use null to skip this option.
	 * @param showDots Paint two dots at the start and end points of the FreeLine.
	 * @param showGradient Paint the FreeLine as a gradient, starting with black and ending with white.
	 * @param showCircle Paint a circle round the start and end points of the FreeLine, indicating how near
	 * another Line should be to be considered connected.
	 */
	public void paint(Graphics g, String label, boolean showDots, boolean showGradient, boolean showCircle) {
		int bottom = points.size() - 1;
		for (int ii = 0; ii < points.size() - 1; ii++) {
			if (showGradient) {
				g.setColor(new Color((float) ii / bottom, (float) ii / bottom, (float) ii / bottom));
			}
			g.drawLine(points.get(ii).x, points.get(ii).y, points.get(ii+1).x, points.get(ii+1).y);
		}
		if (label != null && !label.equals("")) {
			// Draw label in the middle
			g.setColor(Color.BLUE);
			int middle = length() / 2;
			// find the middle segment
			int l = 0;
			for (int ii=0; ii < points.size() - 1; ii++) {
				l += Math.sqrt(Math.pow(points.get(ii).x - points.get(ii + 1).x, 2) +
						Math.pow(points.get(ii).y - points.get(ii + 1).y, 2));
				if (l >= middle) {
					g.drawString(label, (points.get(ii).x + points.get(ii+1).x) / 2,
						(points.get(ii).y + points.get(ii+1).y) / 2);
					break;
				}
					
			}
		}
		if (showDots) {
			g.setColor(Color.RED);
			g.fillOval(points.firstElement().x - 2, points.firstElement().y - 2, 4, 4);
			g.fillOval(points.lastElement().x - 2, points.lastElement().y - 2, 4, 4);
		}
		if (showCircle) {
			g.setColor(Color.BLUE);
			g.drawOval(points.firstElement().x - Point.FORGIVENESS_RADIUS,
					points.firstElement().y - Point.FORGIVENESS_RADIUS,
					Point.FORGIVENESS_RADIUS * 2, Point.FORGIVENESS_RADIUS * 2);
			g.drawOval(points.lastElement().x - Point.FORGIVENESS_RADIUS,
					points.lastElement().y - Point.FORGIVENESS_RADIUS,
					Point.FORGIVENESS_RADIUS * 2, Point.FORGIVENESS_RADIUS * 2);
		}
	}
	
	/**
	 * @return The length of the FreeLine in pixels.
	 */
	public int length() {
		int l = 0;
		for (int ii = 0; ii < points.size() - 1; ii++) {
			l += Math.sqrt(Math.pow(points.get(ii).x - points.get(ii + 1).x, 2)
					+ Math.pow(points.get(ii).y - points.get(ii + 1).y, 2));
		}
		return l;
	}
	
	/**
	 * The number of significant Points in this FreeLine.
	 * @return
	 */
	public int size() {
		return points.size();
	}
}
