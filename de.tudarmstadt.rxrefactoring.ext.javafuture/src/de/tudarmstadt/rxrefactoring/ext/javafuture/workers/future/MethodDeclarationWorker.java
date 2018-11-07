package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class MethodDeclarationWorker extends AbstractFutureWorker<MethodDeclaration> {

	public MethodDeclarationWorker() {
		super("MethodDeclaration");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<MethodDeclaration>> getNodesMap() {
		return collector.getMethodDeclarationsMap("future");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		
		if (collector.refactorReturnStatement(methodDeclaration)) {
			replaceReturnType(unit, methodDeclaration);
			replaceReturnStatements(unit, methodDeclaration);
		}
		
		//Collection<ASTNode> parameters = collector.refactorParameter(methodDeclaration);
		//if (parameters != null) {
		//	replaceParameters(unit, methodDeclaration, parameters);
		//}
		
	}

	private void replaceParameters(IRewriteCompilationUnit unit, MethodDeclaration methodDeclaration, Collection<ASTNode> params) {
		for(Object o :methodDeclaration.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration v = (SingleVariableDeclaration) o;
				Type type = v.getType();
				IBinding b = v.getName().resolveBinding();
				if (params.stream().anyMatch(n -> (n instanceof SimpleName) && b.equals(((SimpleName)n).resolveBinding()))){
						JavaFutureASTUtils.replaceType(unit, type, "Observable");
				}
				
			}
		}
		
	}

	private void replaceReturnType(IRewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		Type returnType = methodDeclaration.getReturnType2();
		if (returnType instanceof ParameterizedType) {
			Type type = ((ParameterizedType) returnType).getType();

			JavaFutureASTUtils.replaceType(unit, type, "Observable");
		}		
	}

	private void replaceReturnStatements(IRewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		
		// Replace return statements
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(ReturnStatement returnStatement) {

				Expression returnExpression = returnStatement.getExpression();

				if (returnExpression instanceof MethodInvocation || returnExpression instanceof NullLiteral) {

					JavaFutureASTUtils.moveInsideMethodInvocation(unit, "Observable", "fromFuture", returnExpression);
					summary.addCorrect("futureCreation");
				}

				return true;
			}

			@Override
			public boolean visit(LambdaExpression lambdaExpression) {
				return false;
			}
		};

		methodDeclaration.accept(visitor);
	}

}
