package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.utils.Expressions;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

public class ReachingDefinitionsAnalysis extends DataFlowAnalysis<ASTNode, ReachingDefinition> {

	public static ReachingDefinitionsAnalysis create() {
		return new ReachingDefinitionsAnalysis();
	}
	
	protected ReachingDefinitionsAnalysis() {
		super(new ReachingDefinitionsStrategy(), traversalForwards());		
	}
	
	private static class ReachingDefinitionsStrategy implements IDataFlowStrategy<ASTNode, ReachingDefinition>  {

		@Override
		public @NonNull ReachingDefinition entryResult() {			
			return ReachingDefinition.empty();
		}

		@Override
		public @NonNull ReachingDefinition initResult() {
			return ReachingDefinition.empty();
		}

		@Override
		public @NonNull ReachingDefinition mergeAll(@NonNull Iterable<ReachingDefinition> results) {			
			return ReachingDefinition.merge(results);
		}

		@Override
		public @NonNull ReachingDefinition transform(@NonNull ASTNode vertex,
				@NonNull ReachingDefinition input) {
						
			if (vertex instanceof Assignment) {
				Assignment assignment = (Assignment) vertex;
				
				Expression lhs = assignment.getLeftHandSide();
				Expression rhs = assignment.getRightHandSide();
							
				IVariableBinding variable = Expressions.resolveVariableBinding(lhs);
								
				if (variable == null) {
					Log.error(getClass(), "assignment to non-variable.");
				} else {
					return input.replace(variable, rhs);
				}				
				
			} else if (vertex instanceof VariableDeclarationStatement || vertex instanceof VariableDeclarationExpression) {
				
				List fragments = null;
				if (vertex instanceof VariableDeclarationStatement) {
					fragments = ((VariableDeclarationStatement) vertex).fragments();
				} else if (vertex instanceof VariableDeclarationExpression) {
					fragments = ((VariableDeclarationExpression) vertex).fragments();
				} else {
					fragments = Collections.emptyList();
				}
				
				for (Object o : fragments) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
					
					if (fragment.getInitializer() != null) {
						IVariableBinding variable = fragment.resolveBinding();
						
						input = input.replace(variable, fragment.getInitializer());
					}
				}
				
				return input;
			}
			
			return input;
		}
		
		
		

		
		
		
	}
	
}