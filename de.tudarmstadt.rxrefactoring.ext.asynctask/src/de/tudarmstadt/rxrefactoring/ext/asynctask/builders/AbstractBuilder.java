package de.tudarmstadt.rxrefactoring.ext.asynctask.builders;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

abstract class AbstractBuilder {

	
	final AsyncTaskWrapper asyncTask;
	final IRewriteCompilationUnit unit;
	
	final AST ast;
	
	
	public AbstractBuilder(AsyncTaskWrapper asyncTask) {		
		this.asyncTask = asyncTask;
		this.unit = asyncTask.getUnit();
		this.ast = unit.getAST();		
	}
		
	
	/*
	 * Private helper methods
	 */	
	
	
	/**
	 * Produces an Override annotation AST Node.
	 * 
	 * @return An unparented Override annotation.
	 */
	Annotation createOverrideAnnotation() {
		MarkerAnnotation annotation = ast.newMarkerAnnotation();
		annotation.setTypeName(ast.newSimpleName("Override"));
		
		return annotation;
	}
	
	/**
	 * Removes method invocations such as super.onCompleted(),
	 * removes unnecessary catch clauses, and replaces
	 * this expressions with the new superclass name.
	 * 
	 * @param body The statement block.
	 * @param methodName The super method to look for.
	 * @param superClassName The name of the super class which
	 * should be used to replace this expressions or null
	 * if this expressions should not be replaced.
	 * @param removeCatchClauses True, if unnecessary catch clauses
	 * should be removed.
	 * 
	 * @return Returns the same block that has been given as argument.
	 */
	Block preprocessBlock(Block body, String methodName, String superClassName, boolean removeCatchClauses) {
		
		/*
		 * Replace this expression with superClassName.this
		 */
		if (superClassName != null) {
			AsyncTaskASTUtils.replaceThisWithFullyQualifiedThisIn(body, unit, superClassName);
		}
		
		/*
		 * Remove super calls such as super.onCompleted()
		 */
		final List<SuperMethodInvocation> methods = Lists.newLinkedList();
		
		class SuperVisitor extends ASTVisitor {
			@Override public boolean visit(SuperMethodInvocation node) {
				if (de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils.matchesTargetMethod(node, methodName, "android.os.AsyncTask")) {
					methods.add(node);
				}
				return true;
			}
		}
		
		SuperVisitor v = new SuperVisitor();
		body.accept(v);
		
		for (SuperMethodInvocation methodInvocation : methods) {
			Optional<Statement> statement = ASTNodes.findParent(methodInvocation, Statement.class);
			unit.remove(statement.orElse(null));
		}		
		
		/*
		 * Remove unnecessary catch clauses.
		 */
//		if (removeCatchClauses)
//			ASTUtils.removeUnnecessaryCatchClauses(unit, body);
		
		/*
		 * Replace field references with fully qualified name 
		 */
		AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(body, unit);

		
		return body;		
	}
	
}
