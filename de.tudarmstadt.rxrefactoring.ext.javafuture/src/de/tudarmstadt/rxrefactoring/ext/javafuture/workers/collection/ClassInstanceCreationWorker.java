package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.CollectionInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class ClassInstanceCreationWorker extends AbstractFutureWorker<ClassInstanceCreation> {

	public ClassInstanceCreationWorker() {
		super("ClassInstanceCreation");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<ClassInstanceCreation>> getNodesMap() {
		return collector.getClassInstanceMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, ClassInstanceCreation classInstanceCreation) {
		Type type = classInstanceCreation.getType();
			
		if (type instanceof ParameterizedType) {
				
			ParameterizedType pType = (ParameterizedType) type;

			if (pType.typeArguments().size() > 0) {

				Type typeArg = (Type) pType.typeArguments().get(0);
				typeArg = ((ParameterizedType) typeArg).getType();
				
				if (collector.isPure(unit, classInstanceCreation)) {
					JavaFutureASTUtils.replaceType(unit, typeArg, "Observable");
				} else {
					JavaFutureASTUtils.replaceType(unit, typeArg, "FutureObservable");
				}
			}
		}
	}
}
