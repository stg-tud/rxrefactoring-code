package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class FieldDeclarationWorker extends AbstractFutureWorker<FieldDeclaration> {

	public FieldDeclarationWorker() {
		super("FieldDeclaration");
	}

	@Override
	protected Multimap<IRewriteCompilationUnit, FieldDeclaration> getNodesMap() {
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
