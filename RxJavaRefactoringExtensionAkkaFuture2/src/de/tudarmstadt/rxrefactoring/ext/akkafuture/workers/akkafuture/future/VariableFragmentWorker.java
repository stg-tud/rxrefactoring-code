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

public class VariableFragmentWorker extends AbstractAkkaWorker<AkkaFutureCollector, VariableDeclarationFragment> {
	public VariableFragmentWorker() {
		super("VaribaleFragment");
	}

	@Override
	protected Multimap<RewriteCompilationUnit, VariableDeclarationFragment> getNodesMap() {
		return collector.variableDeclarations;
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
	protected void refactorNode(RewriteCompilationUnit unit, VariableDeclarationFragment variable) {
		Expression expr = variable.getInitializer();
		Type type = ASTUtils.typeOfVariableFragment(variable);
		
		AST ast = unit.getAST();	
		
		//If the declaration fragment is Future f = Futures.future... or Patterns.ask...
		if (FutureCreationWrapper.isFutureCreation(expr)) {				
			refactorFutureCreation(unit, variable, type, expr);
		} else if (FutureMethodWrapper.isFutureMethod(expr)) {
			refactorFutureMethod(unit, variable, type,expr);
		}
	}
	
	
	private void refactorFutureMethod(RewriteCompilationUnit unit, VariableDeclarationFragment variable, Type type, Expression expr) {
		AST ast = unit.getAST();
		
		//The name of the left-hand-side variable
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
		
//		if (FuturesSequenceWrapper.isFuturesSequence(expr)) {
//			FuturesSequenceWrapper wrapper = FuturesSequenceWrapper.create(expr);			
//			MethodInvocation m = wrapper.createZipExpression(unit);			
//			unit.replace(expr, m);
//			
//			addArraysImport(unit);
//			addCollectorsImport(unit);
//		} else if (FuturesMapWrapper.isFutureMap(expr)) {
//			FuturesMapWrapper wrapper = FuturesMapWrapper.create(expr);			
//			MethodInvocation m = wrapper.createMapExpression(unit);			
//			unit.replace(expr, m);
//			
//			addFunc1Import(unit);
//		}
	}
	
	
	/*
	 * type = Future<...>
	 * 
	 * expr = FUTURE_CREATION
	 */
	private void refactorFutureCreation(RewriteCompilationUnit unit, VariableDeclarationFragment variable, Type type, Expression expr) {
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
		
		AST ast = unit.getAST();	
		
		Statement referenceStatement = ASTUtils.findParent(variable, Statement.class);
		
		//The name of the left-hand-side variable
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
		MethodInvocation fromCallable = AkkaFutureASTUtils.buildFromCallable(unit, () -> unit.copyNode(typeArgumentFinal), block);
		
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
		
		
		ASTUtils.addStatementAfter(unit, ast.newExpressionStatement(subscribe), referenceStatement);
		
		
		/*
		 * Build
		 * 
		 * final var1Final = var;
		 * ...
		 */
		List<SimpleName> variables = ASTUtils.findVariablesIn(expr);
		Set<String> alreadyDeclared = Sets.newHashSet();
		
		for(SimpleName var : variables) {
			
			IBinding binding = var.resolveBinding();			
			//If the variable is already final, do nothing
			if (binding != null && (binding.getModifiers() & Modifier.FINAL) != 0) {
				continue;
			}
			
			ITypeBinding varType = var.resolveTypeBinding();
			if (varType == null) {
				continue;
			}
			
			String newVarName = var.getIdentifier() + "Final";
			
			//If variable has not been declared already, then add a declaration.
			if (!alreadyDeclared.contains(newVarName)) {					
				
				//Add the variable declaration.
				VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
				fragment.setName(ast.newSimpleName(newVarName));
				fragment.setInitializer(ast.newSimpleName(var.getIdentifier()));
				
				VariableDeclarationStatement varStatement = ast.newVariableDeclarationStatement(fragment);
				varStatement.setType(ASTUtils.typeFromBinding(ast, varType));
				varStatement.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
				
				ASTUtils.addStatementAfter(unit, varStatement, referenceStatement);
				
				alreadyDeclared.add(newVarName);
			}
			
			//Replace variable reference with new variable name
			unit.replace(var, ast.newSimpleName(newVarName));			
		}
		
		summary.addCorrect("futureCreation");
	}
	
	protected Type newSubjectType(AST ast, Type typeArgument1, Type typeArgument2) {
		ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Subject")));
		newType.typeArguments().add(typeArgument1);
		newType.typeArguments().add(typeArgument2);
		
		return newType;
	}
	
	protected Type newObservableType(AST ast, Type typeArgument) {
		ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Observable")));
		newType.typeArguments().add(typeArgument);		
		
		return newType;
	}
	
	
	
}

