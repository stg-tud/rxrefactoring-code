package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.SimpleNameVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;


public class SingleVariableDeclWorker extends AbstractFutureTaskWorker<SingleVariableDeclaration>
{
	public SingleVariableDeclWorker() {
		super("SingleVariableDeclaration");
	}
	
	@Override
	protected Map<RewriteCompilationUnit, List<SingleVariableDeclaration>> getNodesMap() {
		return collector.getSingleVarDeclMap("futuretask");
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, SingleVariableDeclaration singleVarDecl) {
		SimpleNameVisitor visitor = new SimpleNameVisitor(ClassInfos.Future.getBinaryName());
		singleVarDecl.accept(visitor);

		if(!JavaFutureASTUtils.isMethodParameter(singleVarDecl)) {
			for (SimpleName simpleName : visitor.getSimpleNames()) {
				if (simpleName.getIdentifier().equals("Future")) {
					JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "SimpleFutureTaskObservable");
				} else {
					JavaFutureASTUtils.appendSimpleName(unit, simpleName, "Observable");
				}
			}
		}
	}
}
