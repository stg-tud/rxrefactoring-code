package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.SimpleNameVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class SingleVariableDeclWorker extends AbstractFutureWorker<SingleVariableDeclaration>
{
	public SingleVariableDeclWorker() {
		super("SingleVariableDeclaration");
	}
	
	@Override
	protected Map<RewriteCompilationUnit, List<SingleVariableDeclaration>> getNodesMap() {
		return collector.getSingleVarDeclMap("future");
	}
	
	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		
		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, SingleVariableDeclaration singleVarDecl) {
		SimpleNameVisitor visitor = new SimpleNameVisitor(ClassInfos.Future.getBinaryName());
		singleVarDecl.accept(visitor);

		for (SimpleName simpleName : visitor.getSimpleNames()) {
			if (simpleName.getIdentifier().equals("Future")) {
				JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "Observable");
			} else {
				JavaFutureASTUtils.appendSimpleName(unit, simpleName, "Observable");
			}
		}
	}
}
