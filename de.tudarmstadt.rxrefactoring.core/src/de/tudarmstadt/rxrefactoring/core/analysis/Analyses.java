package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.SetDataFlowStrategy;

public class Analyses {
	
	public static final DataFlowAnalysis<ASTNode, Multimap<Expression, ASTNode>> USE_DEF_ANALYSIS =
			DataFlowAnalysis.create(null, null);
	
	
	public static class VariableNameAnalysis extends DataFlowAnalysis<ASTNode, Set<String>> {

		public static VariableNameAnalysis create() {
			return new VariableNameAnalysis();
		}
		
		protected VariableNameAnalysis() {
			super(new SetDataFlowStrategy<ASTNode, String>() {

				private Set<String> findVariableNamesIn(ASTNode node) {					
					final Set<String> result = Sets.newHashSet();
					
					if (node instanceof VariableDeclarationStatement) {
						VariableDeclarationStatement variableDeclaration = (VariableDeclarationStatement) node;
						for (Object element : variableDeclaration.fragments()) {
							result.add(((VariableDeclarationFragment) element).getName().getIdentifier());
						}
					}		
					
					return result;
				}				
				
				@Override
				public @NonNull Set<String> transform(@NonNull ASTNode node, @NonNull Set<String> input) {
					Set<String> result = findVariableNamesIn(node);
					result.addAll(input);
					return result;
				}
			}, traversalForwards());
			
		}
		
	}
	
	
}
