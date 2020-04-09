package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.SimpleNameVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class VariableDeclStatementWorker extends AbstractFutureWorker<VariableDeclarationStatement> {

	public VariableDeclStatementWorker() {
		super("VariableDeclarationStatement");
	}

	@Override
	protected Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> getNodesMap() {
		return collector.getVarDeclMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, VariableDeclarationStatement varDeclStatement) {
		Type type = varDeclStatement.getType();	
		
		SimpleNameVisitor v = new SimpleNameVisitor(ClassInfos.Future.getBinaryName());
		type.accept(v);

		
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
		
//		for (SimpleName simpleName : v.getSimpleNames()) {
//			JavaFutureASTUtils.replaceSimpleName(unit, simpleName, "Observable");
//		}
	}
		
}
