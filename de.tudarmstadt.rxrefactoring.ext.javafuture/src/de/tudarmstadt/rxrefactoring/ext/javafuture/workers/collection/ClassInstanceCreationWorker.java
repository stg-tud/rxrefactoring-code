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
				
			ParameterizedType collectionT = (ParameterizedType) type;

			if (collectionT.typeArguments().size() == 1) {

				Type futureT = (Type) collectionT.typeArguments().get(0);
				
				
				
				if (futureT instanceof ParameterizedType) {					
					JavaFutureASTUtils.replaceType(unit, ((ParameterizedType) futureT).getType(), "Observable");
				} else {
					JavaFutureASTUtils.replaceType(unit, futureT, "Observable");
				}	
				
				
				
				
			}
		}
	}
}
