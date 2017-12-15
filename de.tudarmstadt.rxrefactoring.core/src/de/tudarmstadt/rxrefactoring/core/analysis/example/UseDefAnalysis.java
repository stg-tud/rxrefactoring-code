package de.tudarmstadt.rxrefactoring.core.analysis.example;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.strategy.DataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.DataFlowTraversal;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.ForwardTraversal;

public class UseDefAnalysis extends DataFlowAnalysis<ASTNode, Multimap<Expression, ASTNode>>{

	@Override
	public DataFlowStrategy<ASTNode, Multimap<Expression, ASTNode>> newDataFlowStrategy() {
		return new DataFlowStrategy<ASTNode, Multimap<Expression, ASTNode>>() {

			@Override
			public Multimap<Expression, ASTNode> entryResult() {
				return Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
			}

			@Override
			public Multimap<Expression, ASTNode> initResult() {				
				return Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
			}

			@Override
			public Multimap<Expression, ASTNode> mergeAll(List<Multimap<Expression, ASTNode>> results) {
				
				Multimap<Expression, ASTNode> result = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
				// TODO Auto-generated method stub
				
				
				return result;
			}

			@Override
			public Multimap<Expression, ASTNode> transform(ASTNode vertex, Multimap<Expression, ASTNode> input) {
				Multimap<Expression, ASTNode> result = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
				
				// TODO Auto-generated method stub
				return result;
			}
			
		};
	}

	@Override
	public DataFlowTraversal<ASTNode> newDataFlowTraversal(IControlFlowGraph<ASTNode> cfg) {
		return new ForwardTraversal<>(cfg);
	}

}
