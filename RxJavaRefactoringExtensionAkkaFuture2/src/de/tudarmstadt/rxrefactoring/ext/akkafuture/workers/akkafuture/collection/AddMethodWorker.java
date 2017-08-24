package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.collection;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractFutureWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureCollectionAccessWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureCreationWrapper;

public class AddMethodWorker extends AbstractAkkaWorker<AkkaFutureCollector, FutureCollectionAccessWrapper> {
	public AddMethodWorker() {
		super("Assignment");
	}

	@Override
	protected Multimap<RewriteCompilationUnit, FutureCollectionAccessWrapper> getNodesMap() {
		return collector.collectionAccess;
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
	protected void refactorNode(RewriteCompilationUnit unit, FutureCollectionAccessWrapper wrapper) {
		
		if (wrapper.isAdd()) {
			refactorAddInvocation(unit, wrapper);
			
		}		
	}
	
	private void refactorAddInvocation(RewriteCompilationUnit unit, FutureCollectionAccessWrapper wrapper) {
		
		MethodInvocation method = wrapper.getMethodInvocation();
		
		
		Expression argument = (Expression) method.arguments().get(0);
		Expression expr = method.getExpression();
		
				
		AST ast = unit.getAST();	
		Type futureType = ASTUtils.typeFromBinding(ast, argument.resolveTypeBinding().getTypeArguments()[0]);	
		
		Statement referenceStatement = ASTUtils.findParent(method, Statement.class);
				
		//If the declaration fragment is Future f = Futures.future... or Patterns.ask...
		if (FutureCreationWrapper.isFutureCreation(argument)) {				
			/*
			 * Refactors
			 * 
			 * add(FUTURE);
			 * 
			 * to
			 * 
			 * Subject<Integer, Integer> f1 = ReplaySubject.create();
			 *	Observable.fromCallable(() -> {
			 *		return Await.result(FUTURE, Duration.Inf());
			 *	})
			 *	.subscribeOn(Schedulers.io())
			 *	.subscribe(f1);
			 * add(f1);
			 * 
			 */		
			
			String observableName = "observable" + IdManager.getNextObservableId(unit);
			
			//Create replay subject
			MethodInvocation subjectCreate = ast.newMethodInvocation();
			subjectCreate.setName(ast.newSimpleName("create"));
			subjectCreate.setExpression(ast.newSimpleName("ReplaySubject"));
			
			//Initialize variable
			VariableDeclarationFragment varFragment = ast.newVariableDeclarationFragment();
			varFragment.setName(ast.newSimpleName(observableName));
			varFragment.setInitializer(subjectCreate);
			
			VariableDeclarationExpression varExpr = ast.newVariableDeclarationExpression(varFragment);
			varExpr.setType(newSubjectType(ast, (Type) ASTNode.copySubtree(ast, futureType), (Type) ASTNode.copySubtree(ast, futureType)));
			
			ASTUtils.addStatementBefore(unit, ast.newExpressionStatement(varExpr), referenceStatement);
			
			/*
			 * Build
			 * {
			 *     return Await.result(rhs, Duration.Inf());
			 * }
			 */
			MethodInvocation durationInf = ast.newMethodInvocation();
			durationInf.setExpression(ast.newSimpleName("Duration"));
			durationInf.setName(ast.newSimpleName("Inf"));
			
			MethodInvocation awaitResult = ast.newMethodInvocation();
			awaitResult.setExpression(ast.newSimpleName("Await"));
			awaitResult.setName(ast.newSimpleName("result"));
			awaitResult.arguments().add(unit.copyNode(argument));
			awaitResult.arguments().add(durationInf);
			
			ReturnStatement ret = ast.newReturnStatement();
			ret.setExpression(awaitResult);
			
			Block block = ast.newBlock();
			block.statements().add(ret);
			
			/*
			 * Build
			 * 
			 * Observable.fromCallable(...)
			 */
			MethodInvocation fromCallable = AkkaFutureASTUtils.buildFromCallable(unit, () -> (Type) ASTNode.copySubtree(ast, futureType), () -> block);
			
			//Add subscribeOn(Schedulers.io())
			MethodInvocation subscribeOn = ast.newMethodInvocation();
			subscribeOn.setName(ast.newSimpleName("subscribeOn"));
			subscribeOn.setExpression(fromCallable);
			subscribeOn.arguments().add(AkkaFutureASTUtils.createSchedulersIo(ast));
			
			//Add subscribe(future)
			MethodInvocation subscribe = ast.newMethodInvocation();
			subscribe.setName(ast.newSimpleName("subscribe"));
			subscribe.setExpression(subscribeOn);
			subscribe.arguments().add(ast.newSimpleName(observableName));
			
			
			ASTUtils.addStatementBefore(unit, ast.newExpressionStatement(subscribe), referenceStatement);
			
			//Replace argument in add method
			unit.replace(argument, ast.newSimpleName(observableName));
			
			summary.addCorrect("futureCreation");
		}
	}
	
	protected Type newSubjectType(AST ast, Type typeArgument1, Type typeArgument2) {
		ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Subject")));
		newType.typeArguments().add(typeArgument1);
		newType.typeArguments().add(typeArgument2);
		
		return newType;
	}
}

