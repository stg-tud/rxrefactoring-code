package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future;

import java.util.List;
import java.util.Set;

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
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addSubjectImport(unit);
		addCallableImport(unit);
		addSchedulersImport(unit);
		addAwaitImport(unit);
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, Expression expr) {
		
		AST ast = unit.getAST();
		
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		if (typeBinding == null)
			return;
		
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
		
			
		MethodInvocation futuresFuture = ast.newMethodInvocation();
		futuresFuture.setName(ast.newSimpleName("future"));
		futuresFuture.setExpression(ast.newSimpleName("Future"));
				
		//new Callable...
		ClassInstanceCreation newCallable = AkkaFutureASTUtils.buildCallableFromExpr(unit, () -> ASTUtils.typeFromBinding(ast, typeBinding), () -> unit.copyNode(expr)); 
			
		//first argument		
		futuresFuture.arguments().add(newCallable);
		
		MethodInvocation executionContextGlobal = ast.newMethodInvocation();
		executionContextGlobal.setName(ast.newSimpleName("global"));
		executionContextGlobal.setExpression(ast.newSimpleName("ExecutionContexts"));
		
		//2nd argument
		futuresFuture.arguments().add(executionContextGlobal);
		
		
		//Replace original expr
		unit.replace(expr, futuresFuture);		
	}
}

