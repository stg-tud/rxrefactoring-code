package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;

public class FieldDeclarationWorker extends AbstractFutureTaskWorker<FieldDeclaration> {

	public FieldDeclarationWorker() {
		super("FieldDeclaration");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<FieldDeclaration>> getNodesMap() {
		return collector.getFieldDeclMap("futuretask");
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, FieldDeclaration fieldDeclaration) {
		Type fieldType = fieldDeclaration.getType();

		if (fieldType instanceof ParameterizedType) {
			fieldType = ((ParameterizedType) fieldType).getType();
		}

		JavaFutureASTUtils.replaceType(unit, fieldType, "SimpleFutureTaskObservable");
	}
}
