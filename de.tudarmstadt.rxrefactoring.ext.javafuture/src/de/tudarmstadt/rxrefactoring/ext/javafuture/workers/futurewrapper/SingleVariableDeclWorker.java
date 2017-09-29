package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.SimpleNameVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

/**
 * Refactores method arguments.
 *
 */
public class SingleVariableDeclWorker extends AbstractFutureWorker<SingleVariableDeclaration>
{
	public SingleVariableDeclWorker() {
		super("SingleVariableDeclaration");
	}
	
	@Override
	protected Map<IRewriteCompilationUnit, List<SingleVariableDeclaration>> getNodesMap() {
		return collector.getSingleVarDeclMap("future");
	}
	
	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addFutureObservableImport(unit);
		
		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, SingleVariableDeclaration singleVarDecl) {
		
		/*
		Optional<MethodDeclaration> methodDeclaration = ASTUtil.getMethodFromParameter(singleVarDecl);
		
		if(methodDeclaration.isPresent()) {
		
			// Check for super methods
			IMethodBinding methodBinding = methodDeclaration.get().resolveBinding();
			
			// We don't refactor the return value if the method overrides a method outside the project.
			if(!collector.containsMethodDeclaration("future", ASTUtil.getSuperMethod(methodBinding).orElse(null))
					&& ASTUtil.overridesSuperMethod(methodBinding)) {
				return;
			}
		} */
		
		SimpleNameVisitor visitor = new SimpleNameVisitor(ClassInfos.Future.getBinaryName());
		singleVarDecl.accept(visitor);

		for (SimpleName simpleName : visitor.getSimpleNames()) {
			if (simpleName.getIdentifier().equals("Future")) {
				
				if(collector.isPure(unit, singleVarDecl)) {
					JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "Observable");
				} else {
					JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "FutureObservable");
				}
				
			} else {
				JavaFutureASTUtils.appendSimpleName(unit, simpleName, "Observable");
			}
		}
	}
}
