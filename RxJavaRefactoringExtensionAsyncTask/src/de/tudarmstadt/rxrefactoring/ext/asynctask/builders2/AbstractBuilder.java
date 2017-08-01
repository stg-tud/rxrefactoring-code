package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

abstract class AbstractBuilder {

	
	final AsyncTaskWrapper asyncTask;
	final UnitWriter writer;
	
	final AST ast;
	final ASTRewrite astRewrite;
	
	
	public AbstractBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer) {		
		this.asyncTask = asyncTask;
		this.writer = writer;		
		this.ast = writer.getAST();
		this.astRewrite = writer.getAstRewriter();
	}
		
	
	/*
	 * Private helper methods
	 */	
	@SuppressWarnings("unchecked")
	<V extends ASTNode> V copy(V node) {
		return (V) astRewrite.createCopyTarget(node);
		
		
	}
	
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
	 * Removes method invocations such as super.onCompleted() and
	 * removes unnecessary catch clauses.
	 * 
	 * @param body The statement block.
	 * @param methodName The super method to look for.
	 * 
	 * @return Returns the same block that has been given as argument.
	 */
	Block preprocessBlock(Block body, String methodName) {
		
		/*
		 * Remove super calls such as super.onCompleted()
		 */
		final List<SuperMethodInvocation> methods = Lists.newLinkedList();
		
		class SuperVisitor extends ASTVisitor {
			@Override public boolean visit(SuperMethodInvocation node) {
				if (ASTUtils.matchesTargetMethod(node, methodName, "android.os.AsyncTask")) {
					methods.add(node);
				}
				return true;
			}
		}
		
		SuperVisitor v = new SuperVisitor();
		body.accept(v);
		
		for (SuperMethodInvocation methodInvocation : methods) {
			Statement statement = ASTUtils.findParent(methodInvocation, Statement.class);
			astRewrite.remove(statement, null);
		}		
		
		/*
		 * Remove unnecessary catch clauses.
		 */
		ASTUtils.removeUnnecessaryCatchClauses(body);
		
		/*
		 * Replace field references with fully qualified name 
		 */
		AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(body, writer);

		
		return body;		
	}
	
}
