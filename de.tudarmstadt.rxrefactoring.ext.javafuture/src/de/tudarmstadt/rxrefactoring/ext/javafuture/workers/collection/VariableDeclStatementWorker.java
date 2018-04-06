package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.SimpleNameVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class VariableDeclStatementWorker extends AbstractFutureWorker<VariableDeclarationStatement> {

	public VariableDeclStatementWorker() {
		super("VariableDeclarationStatement");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<VariableDeclarationStatement>> getNodesMap() {
		return collector.getVarDeclMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, VariableDeclarationStatement varDeclStatement) {
		Type type = varDeclStatement.getType();

		SimpleNameVisitor v = new SimpleNameVisitor(ClassInfos.Future.getBinaryName());
		type.accept(v);

		for (SimpleName simpleName : v.getSimpleNames()) {
			if (collector.isPure(unit, varDeclStatement)) {
				JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "Observable");
			} else {
				JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "FutureObservable");
			}
		}
	}
}
