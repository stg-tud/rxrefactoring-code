package de.tudarmstadt.rxrefactoring.core;

import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Base class for tests providing some helper methods.
 */
public abstract class RxRefactoringTest {

	/**
	 * Parses a string containing one or more statements.
	 *
	 * @param statements a string with valid Java code.
	 * @return the parsed statements in a Block
	 */
	public final Block parse(String statements) {
		statements += "assert Boolean.TRUE;\n";

		final ASTParser parser = ASTParser.newParser(AST.JLS10);

		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(statements.toCharArray());
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);

		return (Block) parser.createAST(null);
	}

	/**
	 * Gets the expression for an {@link ExpressionStatement} in a Block.
	 *
	 * @param block a Block
	 * @param index the index of the ExpressionStatement
	 * @return the expression of the ExpressionStatement
	 */
	public final Expression getExpression(Block block, int index) {
		ExpressionStatement statement = assertHasType(block.statements().get(index), ExpressionStatement.class);
		return statement.getExpression();
	}

	/**
	 * Gets the variable initialization expression for the given variable declaration statement.
	 *
	 * @param varDecl a VariableDeclarationStatement declaring exactly one variable
	 * @return the initial value for the variable
	 */
	public final Expression getVarInit(ASTNode varDecl) {
		VariableDeclarationStatement statement = assertHasType(varDecl, VariableDeclarationStatement.class);
		VariableDeclarationFragment fragment = assertHasType(statement.fragments().get(0), VariableDeclarationFragment.class);
		return fragment.getInitializer();
	}

	public <T> T assertHasType(final Object obj, final Class<T> type) {
		assertTrue(type.isInstance(obj));
		return type.cast(obj);
	}
}
