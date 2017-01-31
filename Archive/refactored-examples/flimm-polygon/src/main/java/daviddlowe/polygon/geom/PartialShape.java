package daviddlowe.polygon.geom;

import java.awt.Graphics;
import java.util.Vector;

import daviddlowe.polygon.Application;

public class PartialShape extends Vector<Line>{
	private static final long serialVersionUID = 4681840656342071816L;
	
	public PartialShape() {
		super();
	}
	
	public PartialShape(int estimatedSize) {
		super(estimatedSize);
	}
	
	public PartialShape(PartialShape otherPartialShape) {
		for (Line line: otherPartialShape) {
			add(line.copy());
		}
	}

	public Line getFirstLine() {
		return firstElement();
	}
	
	public Line getLastLine() {
		return lastElement();
	}
	
	public String toString() {
		StringBuffer output = new StringBuffer("[");
		for (Line line: this) {
			output.append("{" + line + "}, ");
		}
		output.append("]");
		return output.toString();
	}
	
	private PartialShape getSorted() {
		if (Application.debug) System.out.println("getSorted():");
		
		if (Application.debugging_delays) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		
		PartialShape finalPartialShape = new PartialShape();
		int size;
		do {
			if (Application.debug) System.out.println("Begin do iteration");
			PartialShape partialShape = new PartialShape(this);
			for (Line done: finalPartialShape) {
				partialShape.remove(done);
			}
			if (Application.debug) System.out.println("partialShape: " + partialShape);
			Line firstPipe = partialShape.firstElement();
			Line currentPipe = partialShape.firstElement();
			if (Application.debug) System.out.println(currentPipe);
			partialShape.remove(0);
			finalPartialShape.add(firstPipe);
			
			// must save the size because lines will reduce in size
			size = partialShape.size();
			for (int ii=0; ii < size; ii++) {
				if (Application.debug) {
					System.out.println("partialShape: " + partialShape);
					System.out.println(currentPipe);
				}
				// search for another line that meets the lastPoint of currentPipe
				boolean found = false;
				for (int jj=0; jj < partialShape.size(); jj++) {
					if (partialShape.get(jj).getStartPoint().isNear(currentPipe.getEndPoint())) {
						currentPipe = partialShape.get(jj);
						partialShape.remove(jj);
						finalPartialShape.add(currentPipe);
						found = true;
						break;
					} else if (partialShape.get(jj).getEndPoint().isNear(currentPipe.getEndPoint())) {
						currentPipe = partialShape.get(jj);
						partialShape.remove(jj);
						currentPipe.reverse();
						finalPartialShape.add(currentPipe);
						found = true;
						break;
					}
				}
				if (!found) {
					break;
				}
			}
		} while(finalPartialShape.size() < size);
		if (Application.debug) {
			System.out.println();
			System.out.println("finalPartialShape: " + finalPartialShape);
			System.out.println();
		}
		
		return finalPartialShape;
	}
	
	public void sort() {
		PartialShape sorted = getSorted();
		clear();
		for (Line line: sorted) {
			add(line);
		}
	}
	
	public static class NotStraightLinesException extends Exception {
		private static final long serialVersionUID = -6225591222854630869L;
		public NotStraightLinesException(Throwable cause) {
			super(cause);
		}
	}
	
	public void forceConnect() throws NotStraightLinesException {
		// connect the lines
		try {
			for (int ii=0; ii < size(); ii++) {
				((StraightLine) get(ii)).setEndPoint(new Point(get((ii + 1) % size()).getStartPoint()));
			}
		} catch (ClassCastException ee) {
			throw new NotStraightLinesException(ee);
		}
	}
	
	public boolean isPolygon() {
		if (size() <= 2) {
			return false;
		}
		PartialShape sorted = getSorted();
		for (int ii = 0; ii < sorted.size(); ii++) {
			if (!sorted.get(ii).isConnectedTo(sorted.get((ii+1) % sorted.size()))) {
				return false;
			}
		}
		return true;
	}
	
	public void paint(Graphics g) {
		for (Line line: this) {
			line.paint(g);
		}
	}

}
