package de.jugmuenster.swingbasics.listener.mouse;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import de.jugmuenster.swingbasics.listener.AbstractListenerDemo;

/**
 * The listener that displays all events.
 * 
 * @see AbstractListenerDemo#appendEvent(java.awt.AWTEvent)
 */
public class ShowMouseEventListener implements MouseListener {

    private final ButtonListenerDemo buttonListenerDemo;

    /**
     * @param buttonListenerDemo
     */
    ShowMouseEventListener(ButtonListenerDemo buttonListenerDemo) {
        this.buttonListenerDemo = buttonListenerDemo;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.buttonListenerDemo.appendEvent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.buttonListenerDemo.appendEvent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.buttonListenerDemo.appendEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.buttonListenerDemo.appendEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.buttonListenerDemo.appendEvent(e);
    }

}