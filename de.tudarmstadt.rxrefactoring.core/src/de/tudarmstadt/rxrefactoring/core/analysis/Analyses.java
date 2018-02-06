package de.tudarmstadt.rxrefactoring.core.analysis;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;

public class Analyses {
	
	public static final DataFlowAnalysis<ASTNode, Multimap<Expression, ASTNode>> USE_DEF_ANALYSIS =
			DataFlowAnalysis.create(null, null);
	
	
}
