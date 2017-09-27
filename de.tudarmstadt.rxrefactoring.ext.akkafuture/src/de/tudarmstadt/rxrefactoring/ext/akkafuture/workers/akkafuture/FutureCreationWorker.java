package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.legacy.IdManager;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaFutureWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureCreationWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureTypeWrapper;

public class FutureCreationWorker extends AbstractAkkaFutureWorker<AkkaFutureCollector, FutureCreationWrapper> {
	public FutureCreationWorker() {
		super("Assignment");
	}

	@Override
	protected Multimap<RewriteCompilationUnit, FutureCreationWrapper> getNodesMap() {
		return collector.futureCreations;
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, FutureCreationWrapper wrapper) {		
		
		Expression expr = wrapper.getExpression();
		ASTNode parent = expr.getParent();
		
		if (expr.getParent() instanceof VariableDeclarationFragment) {
			addObservableImport(unit);
			addSubjectImport(unit);
			addCallableImport(unit);
			addSchedulersImport(unit);
			addAwaitImport(unit);
			addDurationImport(unit);
			
			refactorCreateInvocationWithAssignment(unit, (VariableDeclarationFragment) parent, wrapper);
		} else {
			addObservableImport(unit);
			addSubjectImport(unit);
			addCallableImport(unit);
			addSchedulersImport(unit);
			addAwaitImport(unit);
			addDurationImport(unit);
			
			refactorCreateInvocationWithoutAssignment(unit, wrapper);		
		}
		
			
	}
	
	@SuppressWarnings("unchecked")
	private void refactorCreateInvocationWithoutAssignment(RewriteCompilationUnit unit, FutureCreationWrapper wrapper) {
		
		Expression expr = wrapper.getExpression();
						
		AST ast = unit.getAST();	
		
		ITypeBinding binding = expr.resolveTypeBinding();
		Type futureType = Types.typeFromBinding(ast, FutureTypeWrapper.create(binding).getTypeParameter(ast)); //ASTUtils.typeFromBinding(ast, binding.getTypeArguments()[0]);	
		
		Statement referenceStatement = wrapper.getReferenceStatement();
				
		//If the declaration fragment is Future f = Futures.future... or Patterns.ask...
				
		/*
		 * Refactors
		 * 
		 * ... FUTURE ...
		 * 
		 * to
		 * 
		 * Subject<Integer, Integer> f1 = ReplaySubject.create();
		 *	Observable.fromCallable(() -> {
		 *		return Await.result(FUTURE, Duration.Inf());
		 *	})
		 *	.subscribeOn(Schedulers.io())
		 *	.subscribe(f1);
		 * 
		 * ... f1 ...
		 * 
		 */			
				
		//New observable variable name
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
		
		Statements.addStatementBefore(unit, ast.newExpressionStatement(varExpr), referenceStatement);
		
		/*
		 * Build
		 * {
		 *     return Await.result(rhs, Duration.Inf());
		 * }
		 */
		AkkaFutureASTUtils.replaceThisWithFullyQualifiedThisIn(expr, unit);
		
		MethodInvocation durationInf = ast.newMethodInvocation();
		durationInf.setExpression(ast.newSimpleName("Duration"));
		durationInf.setName(ast.newSimpleName("Inf"));
		
		MethodInvocation awaitResult = ast.newMethodInvocation();
		awaitResult.setExpression(ast.newSimpleName("Await"));
		awaitResult.setName(ast.newSimpleName("result"));
		awaitResult.arguments().add(unit.copyNode(expr));
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
		MethodInvocation fromCallable = AkkaFutureASTUtils.buildFromCallable(
				unit, 
				() -> (Type) ASTNode.copySubtree(ast, futureType), 
				() -> {
					AkkaFutureASTUtils.replaceThisWithFullyQualifiedThisIn(block, unit);
					return block;			
				});
		
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
		
		
		//Replace argument in add method	
		if (!(expr.getParent() instanceof Statement))
			unit.replace(expr, ast.newSimpleName(observableName));
		
		
		/*
		 * Build
		 * 
		 * final var1Final = var;
		 * ...
		 */
//		List<SimpleName> variables = ASTUtils.findVariablesIn(expr);
//		Set<String> alreadyDeclared = Sets.newHashSet();
//		
//		for(SimpleName var : variables) {
//			
//			IBinding varBinding = var.resolveBinding();			
//			//If the variable is already final, do nothing
//			if (varBinding != null && (varBinding.getModifiers() & Modifier.FINAL) != 0) {
//				continue;
//			}
//			
//			ITypeBinding varType = var.resolveTypeBinding();
//			if (varType == null) {
//				continue;
//			}
//			
//			String newVarName = var.getIdentifier() + "Final" + IdManager.getNextObserverId(unit);
//			
//			//If variable has not been declared already, then add a declaration.
//			if (!alreadyDeclared.contains(newVarName)) {					
//				
//				//Add the variable declaration.
//				VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
//				fragment.setName(ast.newSimpleName(newVarName));
//				fragment.setInitializer(ast.newSimpleName(var.getIdentifier()));
//				
//				VariableDeclarationStatement varStatement = ast.newVariableDeclarationStatement(fragment);
//				varStatement.setType(ASTUtils.typeFromBinding(ast, varType));
//				varStatement.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
//				
//				ASTUtils.addStatementBefore(unit, varStatement, referenceStatement);
//				
//				alreadyDeclared.add(newVarName);
//			}
//			
//			//Replace variable reference with new variable name
//			unit.replace(var, ast.newSimpleName(newVarName));			
//		}
		AkkaFutureASTUtils.transformVariablesInExpressionToFinal(unit, expr, stmt -> Statements.addStatementBefore(unit, stmt, referenceStatement));

		
		//Add Observable.fromCallable statement 
		Statements.addStatementBefore(unit, ast.newExpressionStatement(subscribe), referenceStatement);
				
		
		summary.addCorrect("futureCreation");
	}
	
	@SuppressWarnings("unchecked")
	private void refactorCreateInvocationWithAssignment(RewriteCompilationUnit unit, VariableDeclarationFragment variable, FutureCreationWrapper wrapper) {
		
		
		AST ast = unit.getAST();	
		
		Expression expr = wrapper.getExpression();
		
		/*
		 * Refactors
		 * 
		 * Future<Integer> f1 = FUTURE;
		 * 
		 * to
		 * 
		 * Subject<Integer, Integer> f1 = ReplaySubject.create();
		 *	Observable.fromCallable(() -> {
		 *		return Await.result(FUTURE, Duration.Inf());
		 *	})
		 *	.subscribeOn(Schedulers.io())
		 *	.subscribe(f1);
		 */			
		Type type = Types.declaredTypeOf(variable);
		
		Statement referenceStatement = ASTNodes.findParent(variable, Statement.class).orElse(null);
		
		//The name of the left-hand-side variable
		Type typeArgument = null;			
		if (type instanceof ParameterizedType) {
			//Replace Future<T> with Subject<T,T>				
			typeArgument = (Type) ((ParameterizedType) type).typeArguments().get(0);				
		} else if (type instanceof SimpleType) {
			//Replace Future with Subject
			typeArgument = ast.newSimpleType(ast.newSimpleName("Object"));		
		}
		
		//Refactor right hand side
		MethodInvocation subjectCreate = ast.newMethodInvocation();
		subjectCreate.setName(ast.newSimpleName("create"));
		subjectCreate.setExpression(ast.newSimpleName("ReplaySubject"));
		
		unit.replace(expr, subjectCreate);
			
		
		/*
		 * Build
		 * {
		 *     return Await.result(rhs, Duration.Inf());
		 * }
		 */
		AkkaFutureASTUtils.replaceThisWithFullyQualifiedThisIn(expr, unit);
		
		MethodInvocation durationInf = ast.newMethodInvocation();
		durationInf.setExpression(ast.newSimpleName("Duration"));
		durationInf.setName(ast.newSimpleName("Inf"));
		
		MethodInvocation awaitResult = ast.newMethodInvocation();
		awaitResult.setExpression(ast.newSimpleName("Await"));
		awaitResult.setName(ast.newSimpleName("result"));		
		awaitResult.arguments().add(unit.copyNode(expr));
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
		final Type typeArgumentFinal = typeArgument;
		MethodInvocation fromCallable = AkkaFutureASTUtils.buildFromCallable(
				unit, 
				() -> unit.copyNode(typeArgumentFinal), 
				() -> {
					AkkaFutureASTUtils.replaceThisWithFullyQualifiedThisIn(block, unit);
					return block;			
				});
		
		//Add subscribeOn(Schedulers.io())
		MethodInvocation subscribeOn = ast.newMethodInvocation();
		subscribeOn.setName(ast.newSimpleName("subscribeOn"));
		subscribeOn.setExpression(fromCallable);
		subscribeOn.arguments().add(AkkaFutureASTUtils.createSchedulersIo(ast));
		
		//Add subscribe(future)
		MethodInvocation subscribe = ast.newMethodInvocation();
		subscribe.setName(ast.newSimpleName("subscribe"));
		subscribe.setExpression(subscribeOn);
		subscribe.arguments().add(unit.copyNode(variable.getName()));
		
		
		Statements.addStatementAfter(unit, ast.newExpressionStatement(subscribe), referenceStatement);
		
		
		/*
		 * Build
		 * 
		 * final var1Final = var;
		 * ...
		 */
//		List<SimpleName> variables = ASTUtils.findVariablesIn(expr);
//		Set<String> alreadyDeclared = Sets.newHashSet();
//		
//		for(SimpleName var : variables) {
//			
//			IBinding binding = var.resolveBinding();			
//			//If the variable is already final, do nothing
//			if (binding != null && (binding.getModifiers() & Modifier.FINAL) != 0) {
//				continue;
//			}
//			
//			ITypeBinding varType = var.resolveTypeBinding();
//			if (varType == null) {
//				continue;
//			}
//			
//			String newVarName = var.getIdentifier() + "Final";
//			
//			//If variable has not been declared already, then add a declaration.
//			if (!alreadyDeclared.contains(newVarName)) {					
//				
//				//Add the variable declaration.
//				VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
//				fragment.setName(ast.newSimpleName(newVarName));
//				fragment.setInitializer(ast.newSimpleName(var.getIdentifier()));
//				
//				VariableDeclarationStatement varStatement = ast.newVariableDeclarationStatement(fragment);
//				varStatement.setType(ASTUtils.typeFromBinding(ast, varType));
//				varStatement.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
//				
//				ASTUtils.addStatementAfter(unit, varStatement, referenceStatement);
//				
//				alreadyDeclared.add(newVarName);
//			}
//			
//			//Replace variable reference with new variable name
//			unit.replace(var, ast.newSimpleName(newVarName));			
//		}
		
		AkkaFutureASTUtils.transformVariablesInExpressionToFinal(unit, expr, stmt -> Statements.addStatementAfter(unit, stmt, referenceStatement));
		
		summary.addCorrect("futureCreation");
	}
	
	
	@SuppressWarnings("unchecked")
	protected Type newSubjectType(AST ast, Type typeArgument1, Type typeArgument2) {
		ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Subject")));
		newType.typeArguments().add(typeArgument1);
		newType.typeArguments().add(typeArgument2);
		
		return newType;
	}
}

