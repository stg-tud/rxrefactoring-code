package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractFutureWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureCreationWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureMethodWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FuturesMapWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FuturesSequenceWrapper;

public class VariableTypeToSubjectWorker extends AbstractAkkaWorker<AkkaFutureCollector, VariableDeclarationFragment> {
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
		Type type = ASTUtils.typeOfVariableFragment(variable);
		
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
	
	protected Type newSubjectType(AST ast, Type typeArgument1, Type typeArgument2) {
		ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Subject")));
		newType.typeArguments().add(typeArgument1);
		newType.typeArguments().add(typeArgument2);
		
		return newType;
	}
	
		
}

