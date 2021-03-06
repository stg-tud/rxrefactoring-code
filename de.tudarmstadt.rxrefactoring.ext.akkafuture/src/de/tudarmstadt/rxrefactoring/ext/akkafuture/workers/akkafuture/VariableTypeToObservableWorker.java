package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaFutureWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;

public class VariableTypeToObservableWorker extends AbstractAkkaFutureWorker<AkkaFutureCollector, VariableDeclarationFragment> {
	public VariableTypeToObservableWorker() {
		super("VaribaleFragment");
	}

	@Override
	protected Multimap<IRewriteCompilationUnit, VariableDeclarationFragment> getNodesMap() {
		return collector.variableDeclarationToObservable;
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {		
		addSubjectImport(unit);		
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, VariableDeclarationFragment variable) {
		Type type = Types.declaredTypeOf(variable);
		
		AST ast = unit.getAST();	
		
		Type typeArgument = null;			
		if (type instanceof ParameterizedType) {
			//Replace Future<T> with Subject<T,T>				
			typeArgument = (Type) ((ParameterizedType) type).typeArguments().get(0);			
			unit.replace(type, newObservableType(ast, unit.copyNode(typeArgument)));
		} else if (type instanceof SimpleType) {
			//Replace Future with Subject
			typeArgument = ast.newSimpleType(ast.newSimpleName("Object"));
			unit.replace(type, unit.getAST().newSimpleType(unit.getAST().newSimpleName("Observable")));
		}
	}
	
		
	@SuppressWarnings("unchecked")
	protected Type newObservableType(AST ast, Type typeArgument) {
		ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Observable")));
		newType.typeArguments().add(typeArgument);		
		
		return newType;
	}
	
	
	
}

