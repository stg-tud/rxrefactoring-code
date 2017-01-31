package daviddlowe.polygon;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;

import javax.swing.*;

import daviddlowe.polygon.geom.*;
import daviddlowe.polygon.geom.Point;

/**
 * This is the area of the window where all the action happens!
 * @author David Lowe
 *
 */
public class DrawArea extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
	private static final long serialVersionUID = -5158775424457600483L;
	
	/** The two values that getMode can return */
	public static enum ModeType {DRAWING, STRAIGHT};
	
	/** The sequence of FreeLines drawn by the user */
	private Vector<FreeLine> freeLines = new Vector<FreeLine>(10);
	/** The polygons generated from freeLines */
	private PartialShape[] polygons;
	
	/** Is the user drawing or viewing results? */
	private ModeType mode = ModeType.DRAWING;
	/** Is the left mouse button down? */
	private boolean mouseDown = false;
	
	/** Popup menu for debugging settings */
	private JPopupMenu popup;
	private JCheckBoxMenuItem menuLineNumbers, menuGradient, menuDots, menuNearnessDots, menuCircle, menuMouse;
	private boolean showLineNumbers = false,
		showGradient = true,
		showDots = true,
		showNearnessDots = true,
		showCircle = false,
		showMouse = false;
	
	/** Pretty round rectangle */
	private Area canvas = null;
	
	/** The current mouse coordinates relative to this DrawArea */
	private int mouseX = 0, mouseY = 0;
	
	/** Whether to show a "please wait" message */
	private AtomicInteger runningWorkers = new AtomicInteger(0);
	
	/** A sequence of DrawAreaListeners */
	private Vector<DrawAreaListener> listeners = new Vector<DrawAreaListener>(1);
	
	public DrawArea() {
		addMouseListener(this);
		addMouseMotionListener(this);
		
		popup = new JPopupMenu();
		menuLineNumbers = new JCheckBoxMenuItem("Show line numbers", showLineNumbers);
		menuGradient = new JCheckBoxMenuItem("Gradient", showGradient);
		menuDots = new JCheckBoxMenuItem("Draw dots", showDots);
		menuNearnessDots = new JCheckBoxMenuItem("Draw green dots if close", showNearnessDots);
		menuCircle = new JCheckBoxMenuItem("Draw nearness circle", showCircle);
		menuMouse = new JCheckBoxMenuItem("Show mouse coordinates", showMouse);
		for (JCheckBoxMenuItem item: new JCheckBoxMenuItem[] {menuLineNumbers, menuGradient, menuDots, menuNearnessDots, menuCircle, menuMouse}) {
			popup.add(item);
			item.addActionListener(this);
		}
	}
	
	/**
	 * Add a DrawAreaListener as an observer of this DrawArea
	 * @param listener
	 */
	public void addListener(DrawAreaListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Switch from DRAWING mode to STRAIGHT mode and vice-versa.
	 */
	public void swap() {
		assert EventQueue.isDispatchThread();
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				if (Application.debug) {
					System.out.println("Number of polygons: " + getPolygons().length);
				}
				if (mode == ModeType.DRAWING) {
					if (freeLines.size() == 0) {
						for (DrawAreaListener listener: listeners) {
							listener.noLinesError();
							System.out.println("no lines");
						}
						return null;
					}
					// switch to STRAIGHT
					polygons = getPolygons();
					if (polygons.length != getPartialShapes().length) {
						for (DrawAreaListener listener: listeners) {
							listener.invalidPolygonError();
						}
					} else {
						mode = ModeType.STRAIGHT;
					}
				} else {
					// switch to DRAWING
					polygons = null;
					mode = ModeType.DRAWING;
				}
				return null;
			}
			@Override
			public void done() {
				runningWorkers.decrementAndGet();
				repaint();
				for (DrawAreaListener listener: listeners) {
					listener.swapped();
				}
			}
		};
		runningWorkers.incrementAndGet();
		repaint();
		Application.sequentialExecutorService.submit(worker);
	}
	
	/**
	 * Returns whether all the partial shapes drawn by the user are polygons.
	 * Warning: this will return true if nothing has been drawn.
	 * @return
	 */
	public boolean areAllShapesPolygons() {
		assert !EventQueue.isDispatchThread();
		PartialShape[] shapes = getPartialShapes();
		for (PartialShape shape: shapes) {
			if (!shape.isPolygon()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @return An array of all the polygons drawn by the user (consisting of FreeLines).
	 */
	public PartialShape[] getPolygons() {
		assert !EventQueue.isDispatchThread();
		PartialShape[] partialShapes = getPartialShapes();
		Vector<PartialShape> polygons = new Vector<PartialShape>(partialShapes.length);
		for (PartialShape partialShape: partialShapes) {
			if (partialShape.isPolygon()) {
				PartialShape newPolygon = new PartialShape(partialShape.size());
				for (Line line: partialShape) {
					newPolygon.add(new StraightLine(line));
				}
				if (Application.debug) System.out.println("newPolygon: " + newPolygon);
				newPolygon.sort();
				if (Application.debug) System.out.println("sort: " + newPolygon);
				try {
					newPolygon.forceConnect();
				} catch (PartialShape.NotStraightLinesException ee) {
					for (DrawAreaListener listener: listeners) {
						listener.drawAreaError(ee);
					}
				}
				if (Application.debug) System.out.println("forceConnect: " + newPolygon);
				polygons.add(newPolygon);
			}
		}
		// convert to array
		PartialShape[] array = new PartialShape[polygons.size()];
		for (int ii=0; ii < array.length; ii++) {
			array[ii] = polygons.get(ii);
		}
		return array;
	}
	
	/**
	 * @return An array of all the PartialShapes drawn by the user. Not all of them are
	 * necessarily polygons.
	 */
	public PartialShape[] getPartialShapes() {
		assert !EventQueue.isDispatchThread();
		if (freeLines.size() == 0) {
			return new PartialShape[] {};
		}
		Vector<PartialShape> partialShapes = new Vector<PartialShape>(2);
		Line firstLine = freeLines.firstElement();
		partialShapes.add(new PartialShape());
		partialShapes.firstElement().add(firstLine);
		for (Line line: freeLines) {
			if (line == firstLine) continue;
			boolean added = false;
			for (PartialShape partialShape: partialShapes) {
				for (Line otherLine: partialShape) {
					if (line.isConnectedTo(otherLine)) {
						partialShape.add(line);
						added = true;
						break;
					}
				}
				if (added) break;
			}
			if (!added) {
				partialShapes.add(new PartialShape());
				partialShapes.lastElement().add(line);
			}
		}
		boolean foundMerge = false;
		do {
			foundMerge = false;
			// connect the partial shapes
			for (PartialShape partialShape: partialShapes) {
				for (PartialShape otherPartialShape: partialShapes) {
					if (otherPartialShape == partialShape) continue;
					for (Line line: partialShape) {
						for (Line otherLine: otherPartialShape) {
							if (line.isConnectedTo(otherLine)) {
								foundMerge = true;
								partialShape.addAll(otherPartialShape);
								partialShapes.remove(otherPartialShape);
								break;
							}
						}
						if (foundMerge) break;
					}
					if (foundMerge) break;
				}
				if (foundMerge) break;
			}
		} while(foundMerge);
		PartialShape[] array = new PartialShape[partialShapes.size()];
		for (int ii=0; ii < partialShapes.size(); ii++) {
			array[ii] = partialShapes.get(ii);
		}
		return array;
	}
	
	/**
	 * Delete all the lines by the user and switch to DRAWING mode.
	 */
	public void clear() {
		assert !EventQueue.isDispatchThread();
		freeLines.clear();
		polygons = null;
		mode = ModeType.DRAWING;
	}
	
	/**
	 * @return DRAWING or STRAIGHT.
	 */
	public ModeType getMode() {
		return mode;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (mode == ModeType.DRAWING && e.getButton() == MouseEvent.BUTTON1) {
			final MouseEvent event = e;
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					// start to draw a new FreeLine
					FreeLine freeLine = new FreeLine();
					freeLine.addPoint(new Point(event));
					freeLines.add(freeLine);
					return null;
				}
				@Override
				public void done() {
					repaint();
				}
			};
			Application.sequentialExecutorService.submit(worker);
			mouseDown = true;
		}
		if (e.isPopupTrigger() && mode == ModeType.DRAWING) {
			// display popup menu
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		final MouseEvent event = e;
		if (mode == ModeType.DRAWING && e.getButton() == MouseEvent.BUTTON1) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					// finish creating a new FreeLine
					freeLines.lastElement().addPoint(new Point(event));
					for (DrawAreaListener listener: listeners) {
						listener.lineAdded();
					}
					// if newest FreeLine isn't very long, ignore it by deleting it
					if (freeLines.lastElement().size() < 3) {
						freeLines.remove(freeLines.size() - 1);
					}
					return null;
				}
				@Override
				public void done() {
					repaint();
				}
			};
			Application.sequentialExecutorService.submit(worker);
			mouseDown = false;
		}
		if (e.isPopupTrigger() && mode == ModeType.DRAWING) {
			// show popup menu
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (mouseDown) {
			final MouseEvent event = e;
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					// update new FreeLine
					freeLines.lastElement().addPoint(new Point(event));
					return null;
				}
				@Override
				public void done() {
					repaint();
				}
			};
			Application.sequentialExecutorService.submit(worker);
		}
	}
	
	/**
	 * @return A new round rectangle used as decoration.
	 */
	private Area canvasFactory() {
		// central rectangle
		Area area = new Area(new Rectangle(45, 45, getWidth() - 90, getHeight() - 90));
		
		// left rectangle
		area.add(new Area(new Rectangle(5, 45, 40, getHeight() - 90)));
		// right rectangle
		area.add(new Area(new Rectangle(getWidth() - 45, 45, 40, getHeight() - 90)));
		// top rectangle
		area.add(new Area(new Rectangle(45, 5, getWidth() - 90, 40)));
		// bottom rectangle
		area.add(new Area(new Rectangle(45, getHeight() - 45, getWidth() - 90, 40)));
		// top-left circle
		area.add(new Area(new Ellipse2D.Float(5, 5, 80, 80)));
		// top-right circle
		area.add(new Area(new Ellipse2D.Float(getWidth() - 85, 5, 80, 80)));
		// bottom-left circle
		area.add(new Area(new Ellipse2D.Float(5, getHeight() - 85, 80, 80)));
		// bottom-right circle
		area.add(new Area(new Ellipse2D.Float(getWidth() - 85, getHeight() - 85, 80, 80)));
		
		return area;
	}
	
	@Override
	public void paint(Graphics g1) {
		// Turn on anti-aliasing
		Graphics2D g = (Graphics2D) g1;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		Font font = new Font("Serif", Font.PLAIN, 12);
		g.setFont(font);
		
		// Create canvas if need be
		if (canvas == null) {
			canvas = canvasFactory();
		}
		
		// Reinitialise the drawing area
		g.clearRect(0, 0, getWidth(), getHeight());
		g.setClip(canvas);
		
		// Draw background		
		g.setColor(Color.WHITE);		
		g.fill(canvas);

		g.setColor(Color.BLACK);
		
		// draw label
		if (getMode() == ModeType.DRAWING && freeLines.size() == 0) {
			g.drawString("Welcome! Drag and drop to start drawing, or right-click to get drawing options.", 30, getHeight() / 2);
			g.drawString("Try drawing several polygons at once!", 50, getHeight() / 2 + 16);
		}
		
		// show mouse coordinates
		if (showMouse) {
			g.drawString("Mouse: " + mouseX + "," + mouseY, 25, 25);
		}
		
		// draw dots
		if (mode == ModeType.DRAWING) {
			for (int ii=0; ii < freeLines.size(); ii++) {
				g.setColor(Color.BLACK);
				String number = showLineNumbers ? Integer.toString(ii) : null;
				freeLines.get(ii).paint(g, number, showDots, showGradient, showCircle);
				// draw green dots if they are near to another dot
				if (showNearnessDots) {
					g.setColor(Color.GREEN);
					for (FreeLine otherFreeLine: freeLines) {
						if (otherFreeLine != freeLines.get(ii)) {
							for (Point otherPoint: new Point[] {otherFreeLine.getStartPoint(), otherFreeLine.getEndPoint()}) {
								for (Point point: new Point[] {freeLines.get(ii).getStartPoint(), freeLines.get(ii).getEndPoint()}) {
									if (point.isNear(otherPoint)) {
										g.fillOval(point.x - 2, point.y - 2, 4, 4);
									}
								}
							}
						}
					}
				}
			}
		} else if (mode == ModeType.STRAIGHT){
			// STRAIGHT mode, draw polygons
			for (PartialShape polygon: polygons) {
				polygon.paint(g);
			}
		}
		
		g.setColor(Color.GRAY);
		if (runningWorkers.get() > 0) {
			g.drawString("Please wait", 25, 45);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == menuLineNumbers) {
			showLineNumbers = menuLineNumbers.getState();
			repaint();
		} else if (e.getSource() == menuGradient) {
			showGradient = menuGradient.getState();
			repaint();
		} else if (e.getSource() == menuDots) {
			showDots = menuDots.getState();
			repaint();
		} else if (e.getSource() == menuNearnessDots) {
			showNearnessDots = menuNearnessDots.getState();
			repaint();
		} else if (e.getSource() == menuCircle) {
			showCircle = menuCircle.getState();
			repaint();
		} else if (e.getSource() == menuMouse) {
			showMouse = menuMouse.getState();
			repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		repaint();
		
	}
}
