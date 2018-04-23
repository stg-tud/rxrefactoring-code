package de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.InstantiationUseWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.VisitorNodes;

public class FutureVisitor3 extends ASTVisitor implements VisitorNodes {
	private final ClassInfo classInfo;
	private final String classBinaryName;
	
	Multimap<ASTNode, Use> instantiationUses;
	Multiset<IVariableBinding> bindings;
	Multimap<ASTNode, ASTNode> collectionInstantiations;
	Multimap<ASTNode, MethodInvocation> collectionGetters;
	Multiset<ASTNode> instantiations;

	private final List<TypeDeclaration> typeDeclarations;
	private final List<FieldDeclaration> fieldDeclarations;
	private final List<Assignment> assignments;
	private final List<VariableDeclarationStatement> varDeclStatements;
	private final List<SimpleName> simpleNames;
	private final List<ClassInstanceCreation> classInstanceCreations;
	private final List<SingleVariableDeclaration> singleVarDeclarations;
	private final List<MethodDeclaration> methodDeclarations;
	private final List<MethodInvocation> methodInvocations;

	public FutureVisitor3(ClassInfo classInfo, InstantiationUseWorker input) {
		this.classInfo = classInfo;
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
		
		instantiationUses = input.instantiationUses;
		bindings = input.bindings;
		collectionInstantiations = input.collectionInstantiations;
		collectionGetters = input.collectionGetters;
		
		instantiations = HashMultiset.create();
		instantiations.addAll(instantiationUses.keySet());
		instantiations.addAll(collectionInstantiations.keySet());
		instantiations.addAll(collectionGetters.keySet());		
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		Object fragment = node.fragments().get(0);
		if (fragment instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment variableDecl = (VariableDeclarationFragment)fragment;
			if (refactorVariable(variableDecl.getName())) {
				fieldDeclarations.add(node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
	
		
		Expression expr = fragment.getInitializer();
		if (refactorVariable(fragment.getName())) {
			if (expr==null)
				varDeclStatements.add(node);
			else if (instantiationUses.containsKey(expr) && 
					!collectionGetters.containsValue(expr))
				varDeclStatements.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		Expression leftHandSide = node.getLeftHandSide();
		Expression rightHandSide = node.getRightHandSide();
		if (leftHandSide instanceof SimpleName && refactorVariable((SimpleName)leftHandSide)){
			if (instantiationUses.containsKey(rightHandSide) && 
					!collectionGetters.containsValue(rightHandSide))
				assignments.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (binding instanceof IVariableBinding) {
		}
		
		if (refactorVariable(simpleName)) 
				simpleNames.add(simpleName);
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (instantiations.contains(node))
			classInstanceCreations.add(node);
		return true;
	}

	//TODO what happens if an instance created through this method receives a 
	// method call of an unsupported method
	@Override
	public boolean visit(MethodDeclaration node) {
		IMethodBinding binding = node.resolveBinding();

		if (binding != null) {
			ITypeBinding returnType = binding.getReturnType();

			if (ASTUtils.isClassOf(returnType, classBinaryName)) {
				methodDeclarations.add(node);
			}
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		Expression expr = node.getExpression();
		if (instantiations.contains(expr) ||
			(expr instanceof SimpleName && refactorVariable((SimpleName) expr))){
			methodInvocations.add(node);
		}
		return true;
	}

	//TODO have not found any case where this takes place
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (ASTUtils.isClassOf(type, classBinaryName)) {
			
			singleVarDeclarations.add(node);
		}
		return true;
	}
	
	/**
	 * Determines whether a SimpleName should be refactored based on the whether it
	 * is binded to a node that should be refactored.
	 */
	private boolean refactorVariable(SimpleName simpleName){
		if (simpleName!=null) {
			IBinding binding = simpleName.resolveBinding();
			if (binding instanceof IVariableBinding) {
				if (bindings.contains((IVariableBinding) binding))
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
		return new ArrayList<>();
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
