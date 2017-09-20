package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.legacy.IdManager;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureTypeWrapper;

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
		
		Supplier<Type> typeSupplier = () -> Types.typeFromBinding(ast, FutureTypeWrapper.create(typeBinding).getTypeParameter(ast));
		
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
		
		/*
		 * Build
		 * 
		 * final var1Final = var;
		 * ...
		 */
		List<SimpleName> variables = ASTUtils.findVariablesIn(expr);
		Set<String> alreadyDeclared = Sets.newHashSet();
		
		String id = IdManager.getNextObserverId(unit);
		
		Expression newExpr = null;
		
		for(SimpleName var : variables) {
			
			//If the variable is already final or a field, do nothing
			IBinding binding = var.resolveBinding();			
			if (binding != null) {
				int modifiers = binding.getModifiers(); 	
				
				if (binding.getKind() != IBinding.VARIABLE || Modifier.isFinal(modifiers) || (modifiers & Modifier.FIELD_ACCESS) != 0) //|| isParameter(var))
					continue;
			}		
					
			
			ITypeBinding varType = var.resolveTypeBinding();
			if (varType == null) {
				continue;
			}
			
			String newVarName = var.getIdentifier() + "Final" + id;
			
			//If variable has not been declared already, then add a declaration.
			if (!alreadyDeclared.contains(newVarName)) {					
				
				//Add the variable declaration.
				VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
				fragment.setName(ast.newSimpleName(newVarName));
				fragment.setInitializer(ast.newSimpleName(var.getIdentifier()));
				
				VariableDeclarationStatement varStatement = ast.newVariableDeclarationStatement(fragment);				
				if (FutureTypeWrapper.isAkkaFuture(varType)) {
					varStatement.setType(FutureTypeWrapper.create(varType).toObservableType(ast));
				} else {
					varStatement.setType(Types.typeFromBinding(ast, varType));
				}				
				varStatement.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
				
				Statements.addStatementBefore(unit, varStatement, ASTNodes.findParent(expr, Statement.class).get());
				
				alreadyDeclared.add(newVarName);
			}
			
//			//Replace variable reference with new variable name
			if (var == expr) {
				newExpr = ast.newSimpleName(newVarName);
			} else {
				unit.replace(var, ast.newSimpleName(newVarName));	
			}
			
						
		}
		
		
		
			
		MethodInvocation futuresFuture = ast.newMethodInvocation();
		futuresFuture.setName(ast.newSimpleName("future"));
		futuresFuture.setExpression(ast.newSimpleName("Futures"));
				
		
		//FUTURE.toBlocking().single()
		MethodInvocation toBlocking = ast.newMethodInvocation();
		toBlocking.setName(ast.newSimpleName("toBlocking"));
		toBlocking.setExpression(newExpr != null ? newExpr : unit.copyNode(expr));
		
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

