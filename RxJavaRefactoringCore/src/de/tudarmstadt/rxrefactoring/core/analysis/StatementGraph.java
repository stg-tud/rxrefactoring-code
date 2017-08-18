package de.tudarmstadt.rxrefactoring.core.analysis;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.Statement;

public class StatementGraph extends ControlFlowGraph<Statement, DefaultEdge<Statement>> {

	public static StatementGraph from(Block block) {
		block.accept(null);
		return null;
	}

	@SuppressWarnings("unused")
	private class CFGBuilder extends ASTVisitor {

		private Statement previous = null;

		public CFGBuilder(Statement previous) {
			this.previous = previous;
		}

		private void edgeFromPrevious(Statement current) {
			if (previous != null)
				edges.put(previous, current);
			previous = current;
		}

		@Override
		public boolean visit(EmptyStatement stmt) {
			// TODO: Provide implementation
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(AssertStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(Block stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(BreakStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(ConstructorInvocation stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(ContinueStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(DoStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(EnhancedForStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(ExpressionStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(ForStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(IfStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		@Override
		public boolean visit(LabeledStatement stmt) {
			edgeFromPrevious(previous);
			return false;
		}

		// {@link AssertStatement},
		/*
		 * {@link Block}, {@link BreakStatement}, {@link ConstructorInvocation}, {@link
		 * ContinueStatement}, {@link DoStatement}, {@link EmptyStatement}, {@link
		 * EnhancedForStatement} {@link ExpressionStatement}, {@link ForStatement},
		 * {@link IfStatement}, {@link LabeledStatement}, {@link ReturnStatement},
		 * {@link SuperConstructorInvocation}, {@link SwitchCase}, {@link
		 * SwitchStatement}, {@link SynchronizedStatement}, {@link ThrowStatement},
		 * {@link TryStatement}, {@link TypeDeclarationStatement}, {@link
		 * VariableDeclarationStatement}, {@link WhileStatement}
		 */
	}
}
