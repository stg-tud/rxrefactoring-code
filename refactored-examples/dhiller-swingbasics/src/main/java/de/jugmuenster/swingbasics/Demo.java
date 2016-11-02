/*
 * Copyright (c) 2011, Java User Group Münster, NRW, Germany, 
 * http://www.jug-muenster.de
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  - 	Redistributions of source code must retain the above copyright notice, this 
 * 	list of conditions and the following disclaimer.
 *  - 	Redistributions in binary form must reproduce the above copyright notice, 
 * 	this list of conditions and the following disclaimer in the documentation 
 * 	and/or other materials provided with the distribution.
 *  - 	Neither the name of the Java User Group Münster nor the names of its contributors may 
 * 	be used to endorse or promote products derived from this software without 
 * 	specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.jugmuenster.swingbasics;

import java.awt.event.ActionEvent;

import javax.swing.*;

import de.jugmuenster.swingbasics.action.ActionDemo;
import de.jugmuenster.swingbasics.components.ComponentsDemo;
import de.jugmuenster.swingbasics.edt.EDTDemo;
import de.jugmuenster.swingbasics.layout.BorderLayoutDemo;
import de.jugmuenster.swingbasics.layout.GridBagLayoutDemo;
import de.jugmuenster.swingbasics.listener.item.ComboBoxListenerDemo;
import de.jugmuenster.swingbasics.listener.mouse.ButtonListenerDemo;
import de.jugmuenster.swingbasics.swingworker.calculation.SwingWorkerDemo;

/**
 * The list of available demos.
 */
public enum Demo {

    EDT(EDTDemo.class),

    BORDER_LAYOUT(BorderLayoutDemo.class),

    GRID_BAG_LAYOUT(GridBagLayoutDemo.class),

    COMPONENTS(ComponentsDemo.class),

    COMBOBOX_LISTENER(ComboBoxListenerDemo.class),

    BUTTON_LISTENER(ButtonListenerDemo.class),

    SWING_WORKER(SwingWorkerDemo.class),

    ACTION(ActionDemo.class),

    ;
    private final Class<? extends JFrame> c;

    private Demo(Class<? extends JFrame> c) {
	this.c = c;
    }

    /**
     * @return the action to start the demo. Will usually be used with a
     *         {@link JButton} or {@link JMenuItem}
     */
    public Action create() {
	return new AbstractAction(c.getSimpleName(), new ImageIcon(getClass().getResource("/play.png"))) {

	    public void actionPerformed(ActionEvent e) {
		Utils.display(c, JFrame.DISPOSE_ON_CLOSE, true);
	    }
	};
    }

}
