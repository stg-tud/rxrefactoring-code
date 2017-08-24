package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
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
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureTypeWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FuturesMapWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FuturesSequenceWrapper;

public class UnrefactorableReferencesWorker extends AbstractAkkaWorker<AkkaFutureCollector, Expression> {
	public UnrefactorableReferencesWorker() {
		super("UnrefactorableReferences");
	}

	@Override
	protected Multimap<RewriteCompilationUnit, Expression> getNodesMap() {
		return collector.unrefactorableFutureReferences;
	}


	@Override
	protected void refactorNode(RewriteCompilationUnit unit, Expression expr) {
		
		AST ast = unit.getAST();
		
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		if (!FutureTypeWrapper.isAkkaFuture(typeBinding))
			return;
		
		Supplier<Type> typeSupplier = () -> ASTUtils.typeFromBinding(ast, FutureTypeWrapper.create(typeBinding).getTypeParameter(ast));
		
		/*
		 * builds
		 * 
		 * Futures.future(new Callable<FUTURE_TYPE>() {
				@Override
				public FUTURE_TYPE call() throws Exception {					
					return FUTURE.toBlocking().single();
				}
			
			}, ExecutionContexts.global())
		 * 
		 * from
		 * 
		 * FUTURE 
		 */
		
		AkkaFutureASTUtils.doWithVariablesFromExpression(unit, expr, stmt -> ASTUtils.addStatementBefore(unit, stmt, ASTUtils.findParent(expr, Statement.class)));
			
		MethodInvocation futuresFuture = ast.newMethodInvocation();
		futuresFuture.setName(ast.newSimpleName("future"));
		futuresFuture.setExpression(ast.newSimpleName("Futures"));
				
		//FUTURE.toBlocking().single()
		MethodInvocation toBlocking = ast.newMethodInvocation();
		toBlocking.setName(ast.newSimpleName("toBlocking"));
		toBlocking.setExpression(unit.copyNode(expr));
		
		MethodInvocation single = ast.newMethodInvocation();
		single.setName(ast.newSimpleName("single"));
		single.setExpression(toBlocking);
		
		//new Callable...
		ClassInstanceCreation newCallable = AkkaFutureASTUtils.buildCallableFromExpr(unit, typeSupplier, () -> single); 
			
		//first argument		
		futuresFuture.arguments().add(newCallable);
		
		MethodInvocation executionContextGlobal = ast.newMethodInvocation();
		executionContextGlobal.setName(ast.newSimpleName("global"));
		executionContextGlobal.setExpression(ast.newSimpleName("ExecutionContexts"));
		
		//2nd argument
		futuresFuture.arguments().add(executionContextGlobal);
		
		
		//Add imports
		addFuturesImport(unit);
		addCallableImport(unit);
		addExecutionContextsImport(unit);		
		
		
		//Replace original expr
		unit.replace(expr, futuresFuture);		
	}
}

