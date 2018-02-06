package de.tudarmstadt.rxrefactoring.core.analysis.impl;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;

public class UseDefAnalysis extends DataFlowAnalysis<ASTNode, Set<String>> {

	public static UseDefAnalysis create() {
		return new UseDefAnalysis();
	}
	
	protected UseDefAnalysis() {
		super(null, traversalForwards());		
	}
	
	
}