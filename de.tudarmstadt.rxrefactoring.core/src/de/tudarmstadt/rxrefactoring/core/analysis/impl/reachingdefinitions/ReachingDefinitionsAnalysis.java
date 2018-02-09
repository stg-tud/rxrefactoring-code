package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
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
				
				//TODO Add fields
				if (lhs instanceof Name) {
					return input.replace((Name) lhs, rhs);
				} else {
					Log.error(getClass(), "Assignment to non-name");
				}
			} else if (vertex instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement varDecl = (VariableDeclarationStatement) vertex;
				
				for (Object o : varDecl.fragments()) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
					
					if (fragment.getInitializer() != null) {
						return input.replace(fragment.getName(), fragment.getInitializer());
					}
				}
			}
			
			return input;
		}
		
		
		

		
		
		
	}
	
}