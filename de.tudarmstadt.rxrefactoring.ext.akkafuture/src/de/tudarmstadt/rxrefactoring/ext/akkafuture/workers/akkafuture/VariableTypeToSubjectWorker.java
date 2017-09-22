package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaFutureWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;

public class VariableTypeToSubjectWorker extends AbstractAkkaFutureWorker<AkkaFutureCollector, VariableDeclarationFragment> {
	public VariableTypeToSubjectWorker() {
		super("VaribaleFragment");
	}

	@Override
	protected Multimap<RewriteCompilationUnit, VariableDeclarationFragment> getNodesMap() {
		return collector.variableDeclarationToSubject;
	}

	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {		
		addSubjectImport(unit);		
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, VariableDeclarationFragment variable) {
		Type type = Types.declaredTypeOf(variable);
		
		AST ast = unit.getAST();	
		
		Type typeArgument = null;			
		if (type instanceof ParameterizedType) {
			//Replace Future<T> with Subject<T,T>				
			typeArgument = (Type) ((ParameterizedType) type).typeArguments().get(0);			
			unit.replace(type, newSubjectType(ast, unit.copyNode(typeArgument), unit.copyNode(typeArgument)));
		} else if (type instanceof SimpleType) {
			//Replace Future with Subject
			typeArgument = ast.newSimpleType(ast.newSimpleName("Object"));
			unit.replace(type, unit.getAST().newSimpleType(unit.getAST().newSimpleName("Subject")));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Type newSubjectType(AST ast, Type typeArgument1, Type typeArgument2) {
		ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Subject")));
		newType.typeArguments().add(typeArgument1);
		newType.typeArguments().add(typeArgument2);
		
		return newType;
	}
	
		
}

