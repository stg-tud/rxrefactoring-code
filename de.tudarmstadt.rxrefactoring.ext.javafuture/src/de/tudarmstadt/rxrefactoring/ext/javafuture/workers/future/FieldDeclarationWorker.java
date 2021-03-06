package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class FieldDeclarationWorker extends AbstractFutureWorker<FieldDeclaration> {

	public FieldDeclarationWorker() {
		super("FieldDeclaration");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<FieldDeclaration>> getNodesMap() {
		return collector.getFieldDeclMap("future");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, FieldDeclaration node) {
		Type type = node.getType();
		
		if (!Types.isExactTypeOf(type.resolveBinding(), "java.util.concurrent.Future")) {
			return;
		}
		
		if (type instanceof ParameterizedType) {
			type = ((ParameterizedType) type).getType();
		}

		JavaFutureASTUtils.replaceType(unit, type, "Observable");	
	}
}
