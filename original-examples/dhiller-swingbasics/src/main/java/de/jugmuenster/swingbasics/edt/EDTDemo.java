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

package de.jugmuenster.swingbasics.edt;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Random;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Try to demonstrate <a href=
 * "http://download.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html"
 * >Event Dispatch Thread</a> issues. Displays a tree and two buttons, each of
 * which will add a node with ten children to the tree. One of them will do the
 * add within the EDT and the other outside the EDT.
 */
public class EDTDemo extends JFrame {

    private static final int NUMBER_OF_CHILDREN = 10;

    private static final Random random = new Random();

    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
	    "Root");
    private final DefaultTreeModel treeModel = new DefaultTreeModel(root);
    private final JTree tree = new JTree(treeModel);

    public EDTDemo() {
	this.getContentPane().setLayout(new BorderLayout());
	this.getContentPane().add(new JScrollPane(tree));
	final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
	buttons.add(new JButton(new NonThreadSafeManipulator(this)));
	buttons.add(new JButton(new ThreadSafeManipulator(this)));
	this.getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    /**
     * Adds a node with ten children to the tree instance. Node is inserted at
     * random position directly below the root.
     * 
     * @see #tree
     * @see #random
     */
    void addNode() {
	printThreadSafetyOnStdOut();
	final DefaultMutableTreeNode parent = newNode();
	insertIntoTreeModelBelow(parent, root, randomInsertPositionBelowRoot());
	createAndInsertChildren(parent, NUMBER_OF_CHILDREN);
	tree.expandPath(new TreePath(new Object[] { root, parent }));
    }

    private void printThreadSafetyOnStdOut() {
	System.out
		.println(String
			.format("addNode called %s", (SwingUtilities.isEventDispatchThread() ? "thread-safe." : "NON THREAD SAFE!"))); //$NON-NLS-1$ // TODO: Remove
    }

    private int randomInsertPositionBelowRoot() {
	return (root.getChildCount() > 1 ? Math.abs(random.nextInt()
		% (root.getChildCount() - 1)) : 0);
    }

    private void createAndInsertChildren(final DefaultMutableTreeNode parent,
	    final int number) {
	for (int i = 0; i < number; i++) {
	    insertIntoTreeModelBelow(newNode(), parent, parent.getChildCount());
	}
    }

    private void insertIntoTreeModelBelow(final MutableTreeNode newChild,
	    final MutableTreeNode parent, int index) {
	treeModel.insertNodeInto(newChild, parent, index);
    }

    private DefaultMutableTreeNode newNode() {
	return new DefaultMutableTreeNode(random.nextLong());
    }

}
