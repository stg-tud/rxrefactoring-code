package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.visitors.helper.SimpleNameVisitor;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractFutureWorker;

public class VariableDeclStatementWorker extends AbstractFutureWorker<VariableDeclarationStatement> {

	public VariableDeclStatementWorker() {
		super("VariableDeclarationStatement");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<VariableDeclarationStatement>> getNodesMap() {
		return collector.getVarDeclMap("collection");
	}

	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, VariableDeclarationStatement varDeclStatement) {
		Type type = varDeclStatement.getType();
		
		SimpleNameVisitor v = new SimpleNameVisitor(ClassInfos.AkkaFuture.getBinaryName());
		type.accept(v);
		
		for (SimpleName simpleName : v.getSimpleNames()) {
			if(collector.isPure(unit, varDeclStatement)) {
				JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "Observable");
			} else {
				JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "FutureObservable");
			}
		}
	}
}
