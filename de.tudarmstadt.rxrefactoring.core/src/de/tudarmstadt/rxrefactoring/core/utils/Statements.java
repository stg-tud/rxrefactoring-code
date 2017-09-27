package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;

public final class Statements {

	
	private Statements() {}
	
	/**
	 * Checks whether the given statement is a loop statement.
	 * 
	 * @param statement The statement to check.
	 * 
	 * @return True, iff statement is an instance of {@link WhileStatement}, 
	 * {@link DoStatement}, {@link ForStatement}, or {@link EnhancedForStatement}.
	 */
	public static boolean isLoop(Statement statement) {
		return (statement instanceof WhileStatement) || (statement instanceof DoStatement) || (statement instanceof ForStatement) || (statement instanceof EnhancedForStatement);
	}
	
	/**
	 * Finds the label of a statement.
	 * 
	 * @param statement The statement that is labeled.
	 * 
	 * @return The label of the given statement if it is a {@link LabeledStatement}, or labeled by
	 * a {@link LabeledStatement}. Returns null, if there is no label or the input was null.
	 */
	public static SimpleName getLabelOf(Statement statement) {
		if (statement == null) {
			return null;
		}
		
		if (statement instanceof LabeledStatement) {
			//TODO: Keep this case?
			return ((LabeledStatement) statement).getLabel();
		}
		
		ASTNode parent = statement.getParent();
		if (parent instanceof LabeledStatement) {
			return ((LabeledStatement) parent).getLabel();
		}
		
		return null;
	}
	
	
	public static void addStatementBefore(@NonNull RewriteCompilationUnit unit, @NonNull Statement newStatement, @NonNull Statement referenceStatement) {				
		Optional<Block> parentBlock = ASTNodes.findParent(referenceStatement, Block.class);
		
		if (!parentBlock.isPresent()) {
			throw new IllegalArgumentException("referenceStatement not in a block");
		}
				
		ListRewrite statementsBlock = unit.getListRewrite(parentBlock.get(), Block.STATEMENTS_PROPERTY);
		statementsBlock.insertBefore(newStatement, referenceStatement, null);					
	}
	
	
	public static void addStatementAfter(@NonNull RewriteCompilationUnit unit, @NonNull Statement newStatement, @NonNull Statement referenceStatement) {		
		Optional<Block> parentBlock = ASTNodes.findParent(referenceStatement, Block.class);
		
		if (!parentBlock.isPresent()) {
			throw new IllegalArgumentException("referenceStatement not in a block");
		}
				
		ListRewrite statementsBlock = unit.getListRewrite(parentBlock.get(), Block.STATEMENTS_PROPERTY);
		statementsBlock.insertAfter(newStatement, referenceStatement, null);
	}
	
	
}
