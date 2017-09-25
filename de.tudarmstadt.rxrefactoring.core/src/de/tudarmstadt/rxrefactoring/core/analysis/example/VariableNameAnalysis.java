package de.tudarmstadt.rxrefactoring.core.analysis.example;

import java.util.Set;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.strategy.DataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.strategy.SetDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.DataFlowTraversal;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.ForwardTraversal;

public class VariableNameAnalysis extends DataFlowAnalysis<Set<String>> {

	@Override
	public DataFlowStrategy<Set<String>> newDataFlowStrategy() {		
		return new SetDataFlowStrategy<String>() {

			private Set<String> findVariableNamesIn(Statement statement) {
				
				
				final Set<String> result = Sets.newHashSet();
				
				if (statement instanceof VariableDeclarationStatement) {
					VariableDeclarationStatement variableDeclaration = (VariableDeclarationStatement) statement;
					for (Object element : variableDeclaration.fragments()) {
						result.add(((VariableDeclarationFragment) element).getName().getIdentifier());
					}
				}
				
		
				
				return result;
			}
			
			
			@Override
			public Set<String> transform(Statement statement, Set<String> input) {
				Set<String> result = findVariableNamesIn(statement);
				result.addAll(input);
				return result;
			}
		};
	}

	@Override
	public DataFlowTraversal<Statement> newDataFlowTraversal(ControlFlowGraph cfg) {		
		return new ForwardTraversal(cfg);
	}

	
}