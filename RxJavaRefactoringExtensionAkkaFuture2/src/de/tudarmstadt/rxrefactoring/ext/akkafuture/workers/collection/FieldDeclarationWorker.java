package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.domain.CollectionInfo;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractFutureWorker;


public class FieldDeclarationWorker extends AbstractFutureWorker<FieldDeclaration> {
	
	public FieldDeclarationWorker() {
		super("FieldDeclaration");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<FieldDeclaration>> getNodesMap() {
		return collector.getFieldDeclMap("collection");
	}
	
	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);
		
		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, FieldDeclaration fieldDeclaration) {
		Type fieldType = fieldDeclaration.getType();
		
		if(ASTUtils.isTypeOf(fieldType, CollectionInfo.getBinaryNames())) {
			if(fieldType instanceof ParameterizedType) {

				ParameterizedType pType = (ParameterizedType)fieldType;
				Type typeArg = (Type)pType.typeArguments().get(0);
				typeArg = ((ParameterizedType)typeArg).getType();

				JavaFutureASTUtils.replaceType(unit, typeArg, "FutureObservable");
			}
		}
	}
}