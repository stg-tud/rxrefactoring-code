package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
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
	
	
}
