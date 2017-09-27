package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class FieldDeclarationWorker extends AbstractFutureWorker<FieldDeclaration> {
	
	public FieldDeclarationWorker() {
		super("FieldDeclaration");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<FieldDeclaration>> getNodesMap() {
		return collector.getFieldDeclMap("future");
	}
	
	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		
		super.endRefactorNode(unit);
	}



	@Override
	protected void refactorNode(RewriteCompilationUnit unit, FieldDeclaration node) {
		Type fieldType = node.getType();
		
		if (fieldType instanceof ParameterizedType) {
			fieldType = ((ParameterizedType)fieldType).getType();
		}
		
		JavaFutureASTUtils.replaceType(unit, fieldType, "Observable");		
	}
}
