package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.CollectionInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;


public class MethodDeclarationWorker extends AbstractFutureWorker<MethodDeclaration> {

	public MethodDeclarationWorker() {
		super("MethodDeclaration");
	}
	
	@Override
	protected Map<RewriteCompilationUnit, List<MethodDeclaration>> getNodesMap() {
		return collector.getMethodDeclarationsMap("collection");
	}
	
	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addFutureObservableImport(unit);
		
		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		replaceReturnType(unit, methodDeclaration);
	}

	private void replaceReturnType(RewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		Type returnType = methodDeclaration.getReturnType2();
		
		// Check for super methods
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		
		// We don't refactor the return value if the method overrides a method outside the project.
		if(!collector.containsMethodDeclaration("collection", ASTUtils.getSuperMethod(methodBinding).orElse(null))
				&& ASTUtils.overridesSuperMethod(methodBinding)) {
			return;
		}
		
		if(ASTUtils.isTypeOf(returnType, CollectionInfo.getBinaryNames())) {
			if(returnType instanceof ParameterizedType) {

				ParameterizedType pType = (ParameterizedType)returnType;
				Type typeArg = (Type)pType.typeArguments().get(0);
				typeArg = ((ParameterizedType)typeArg).getType();

				JavaFutureASTUtils.replaceType(unit, typeArg, "FutureObservable");
			}
		}
	}
}
