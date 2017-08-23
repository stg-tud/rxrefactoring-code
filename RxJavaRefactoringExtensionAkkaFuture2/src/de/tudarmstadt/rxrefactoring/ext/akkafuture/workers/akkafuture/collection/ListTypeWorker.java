package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.collection;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaListCollector;

public class ListTypeWorker extends AbstractAkkaWorker<AkkaListCollector, ParameterizedType> {
	public ListTypeWorker() {
		super("Await.result");
	}

	@Override
	protected Multimap<RewriteCompilationUnit, ParameterizedType> getNodesMap() {
		return collector.collectionTypes;
	}

	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addSubjectImport(unit);
		addCallableImport(unit);
		addSchedulersImport(unit);
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, ParameterizedType type) {
		
		AST ast = unit.getAST();
		
		ASTNode parent = type.getParent();
		
		if (parent instanceof MethodDeclaration)
			return;
				
		if (type.typeArguments().size() != 1)
			return;
		
		Type argument = (Type) type.typeArguments().get(0);
		
		if (argument instanceof ParameterizedType) {
			unit.replace(((ParameterizedType) argument).getType(), ast.newSimpleType(ast.newSimpleName("Observable")));
		} else if (argument instanceof SimpleType) {
			unit.replace(argument, ast.newSimpleType(ast.newSimpleName("Observable")));
		}
		
	}
	

}

