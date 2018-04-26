package de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Expressions;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.PreconditionWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.VisitorNodes;

public class FutureVisitor3 extends ASTVisitor implements VisitorNodes {
	private final String classBinaryName;
	
	Multimap<ASTNode, Use> instantiationUses;
	Set<IVariableBinding> bindings;
	Multimap<ASTNode, ASTNode> collectionInstantiations;
	Multimap<ASTNode, MethodInvocation> collectionGetters;
	Multimap<MethodDeclaration, ASTNode> methodDecl;
	Set<SingleVariableDeclaration> collectionForStatements;

	private final List<TypeDeclaration> typeDeclarations;
	private final List<FieldDeclaration> fieldDeclarations;
	private final List<Assignment> assignments;
	private final List<VariableDeclarationStatement> varDeclStatements;
	private final List<SimpleName> simpleNames;
	private final List<ClassInstanceCreation> classInstanceCreations;
	private final List<SingleVariableDeclaration> singleVarDeclarations;
	private final List<MethodDeclaration> methodDeclarations;
	private final List<MethodInvocation> methodInvocations;
	private final List<ReturnStatement> returnStatements;

	public FutureVisitor3(ClassInfo classInfo, PreconditionWorker input) {
		this.classBinaryName = classInfo.getBinaryName();

		typeDeclarations = new ArrayList<>();
		assignments = new ArrayList<>();
		fieldDeclarations = new ArrayList<>();
		methodInvocations = new ArrayList<>();
		varDeclStatements = new ArrayList<>();
		simpleNames = new ArrayList<>();
		classInstanceCreations = new ArrayList<>();
		singleVarDeclarations = new ArrayList<>();
		methodDeclarations = new ArrayList<>();
		returnStatements = new ArrayList<>();
		
		instantiationUses = input.instantiationUses;
		bindings = input.bindings;
		collectionInstantiations = input.collectionInstantiations;
		collectionGetters = input.collectionGetters;
		methodDecl = input.methodDeclarations;
		collectionForStatements = new HashSet<SingleVariableDeclaration>();
		input.collectionForStatements.values().forEach(x -> collectionForStatements.add(x.getParameter()));
		
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		Object fragment = node.fragments().get(0);
		if (fragment instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment variableDecl = (VariableDeclarationFragment)fragment;
			if (refactorVariable(Expressions.resolveVariableBinding(variableDecl.getName()))) {
				fieldDeclarations.add(node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
	
		
		
		Expression expr = fragment.getInitializer();
		if (refactorVariable(Expressions.resolveVariableBinding(fragment.getName()))) {
			if (expr==null)
				varDeclStatements.add(node);
			else if (instantiationUses.containsKey(expr))
				varDeclStatements.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		Expression leftHandSide = node.getLeftHandSide();
		Expression rightHandSide = node.getRightHandSide();
		if (refactorVariable(Expressions.resolveVariableBinding(leftHandSide))){
			if (instantiationUses.containsKey(rightHandSide) && 
					!collectionGetters.containsValue(rightHandSide))
				assignments.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (refactorVariable(Expressions.resolveVariableBinding(simpleName))) 
				simpleNames.add(simpleName);
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (instantiationUses.containsKey(node))
			classInstanceCreations.add(node);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (methodDecl.containsKey(node))
			methodDeclarations.add(node);
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		Expression expr = node.getExpression();
		
		if (expr == null) {
			return true;
		}
 				
		if (instantiationUses.containsKey(expr) 
				|| refactorVariable(Expressions.resolveVariableBinding(expr))) {
			methodInvocations.add(node);
		}
		return true;
	}
	
	@Override
	public boolean visit(ReturnStatement node) {
		Expression expr = node.getExpression();
		if (instantiationUses.containsKey(expr)){
			returnStatements.add(node);
		}
		return true;
	}
	
	
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		if (collectionForStatements.contains(node)) {
			singleVarDeclarations.add(node);
		}
		return true;
	}
	
	/**
	 * Determines whether a SimpleName should be refactored based on the whether it
	 * is binded to a node that should be refactored.
	 */
	private boolean refactorVariable(IVariableBinding variable){
		if (variable != null) {
			if (bindings.contains(variable)) {
				return true;
			}
		}
				
		return false;
	}
	
	@Override
	public List<TypeDeclaration> getTypeDeclarations() {
		return typeDeclarations;
	}

	@Override
	public List<FieldDeclaration> getFieldDeclarations() {
		return fieldDeclarations;
	}

	@Override
	public List<Assignment> getAssignments() {
		return assignments;
	}

	@Override
	public List<VariableDeclarationStatement> getVarDeclStatements() {
		return varDeclStatements;
	}

	@Override
	public List<SimpleName> getSimpleNames() {
		return simpleNames;
	}

	@Override
	public List<ClassInstanceCreation> getClassInstanceCreations() {
		return classInstanceCreations;
	}

	@Override
	public List<SingleVariableDeclaration> getSingleVarDeclarations() {
		return singleVarDeclarations;
	}

	@Override
	public List<MethodInvocation> getMethodInvocations() {
		return methodInvocations;
	}

	@Override
	public List<MethodDeclaration> getMethodDeclarations() {
		return methodDeclarations;
	}

	@Override
	public List<ArrayCreation> getArrayCreations() {
		return new ArrayList<>();
	}

	@Override
	public List<ReturnStatement> getReturnStatements() {
		return returnStatements;
	}

	@Override
	public Map<ASTNode, MethodDeclaration> getParentMethods() {
		return null;
	}

	@Override
	public Map<MethodDeclaration, Boolean> getIsMethodPures() {
		return null;
	}
}
