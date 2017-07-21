package de.tudarmstadt.rxrefactoring.ext.asynctask.collect;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;

public class TypeVisitor extends ASTVisitor {

	private final Iterable<TypeDeclaration> types;

	public TypeVisitor(Iterable<TypeDeclaration> types) {
		this.types = types;
		// varDecls = Sets.newHashSet();
	}

	/*
	 * final Set<Expression> varDecls;
	 * 
	 * public boolean visit(Assignment node) { Expression rhs =
	 * node.getRightHandSide();
	 * 
	 * if (isClassInstanceCreationOfTypes(rhs))
	 * 
	 * 
	 * Expression lhs = node.getLeftHandSide();
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * node.getLeftHandSide().visit(new ASTVisitor() { public boolean
	 * visit(SimpleName varDec) {
	 * 
	 * varDec.res
	 * 
	 * varDec.getType()
	 * 
	 * return true; } });
	 * 
	 * 
	 * 
	 * return true; }
	 * 
	 * 
	 * private Optional<TypeDeclaration> isClassInstanceCreationOfTypes(ASTNode
	 * node) {
	 * 
	 * class ClassCreationVisitor extends ASTVisitor { TypeDeclaration
	 * isRelevantClass = null;
	 * 
	 * @Override public boolean visit(ClassInstanceCreation node) { for
	 * (TypeDeclaration t : types) { if (isRelevantClass == null &&
	 * ASTUtils.isTypeOf(node, t.getName().getFullyQualifiedName())) {
	 * isRelevantClass = t; return false; } } return false; } }
	 * 
	 * ClassCreationVisitor v = new ClassCreationVisitor(); node.accept(v); return
	 * Optional.ofNullable(v.isRelevantClass); }
	 * 
	 * private class LocalConstantPropagation
	 */
}
