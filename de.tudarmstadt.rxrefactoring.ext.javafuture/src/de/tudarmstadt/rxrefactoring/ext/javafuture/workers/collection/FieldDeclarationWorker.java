package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class FieldDeclarationWorker extends AbstractFutureWorker<FieldDeclaration> {

	public FieldDeclarationWorker() {
		super("FieldDeclaration");
	}

	@Override
	protected Multimap<IRewriteCompilationUnit, FieldDeclaration> getNodesMap() {
		return collector.getFieldDeclMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, FieldDeclaration fieldDeclaration) {
		Type fieldType = fieldDeclaration.getType();

		if (fieldType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) fieldType;
			Type typeArg = (Type) pType.typeArguments().get(0);
			typeArg = ((ParameterizedType) typeArg).getType();

			JavaFutureASTUtils.replaceType(unit, typeArg, "Observable");
		}
	}
}
