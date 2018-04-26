package de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
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

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.PreconditionWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.CollectionInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.VisitorNodes;

/**
 * Search for occurrences of lists of Futures
 * 
 */
public class FutureCollectionVisitor2 extends ASTVisitor implements VisitorNodes {

	private final String classBinaryName;
	private final String[] collectionBinaryNames = CollectionInfo.getBinaryNames();

	Multimap<ASTNode, Use> instantiationUses;
	Multimap<ASTNode, ASTNode> collectionInstantiations;
	Multimap<ASTNode, MethodInvocation> collectionGetters;
	Set<ASTNode> toRefactorCollections;
	Set<IVariableBinding> collectionBindings;
	Set<IVariableBinding> iteratorBindings;
	Multimap<MethodDeclaration, ASTNode> collectionMethodDeclarations;
	Multimap<ASTNode, ASTNode> collectionIterators;
	
	private final List<TypeDeclaration> typeDeclarations;
	private final List<FieldDeclaration> fieldDeclarations;
	private final List<Assignment> assignments;
	private final List<VariableDeclarationStatement> varDeclStatements;
	private final List<SimpleName> simpleNames;
	private final List<ClassInstanceCreation> classInstanceCreations;
	private final List<SingleVariableDeclaration> singleVarDeclarations;
	private final List<MethodDeclaration> methodDeclarations;
	private final List<MethodInvocation> methodInvocations;
	private final List<ArrayCreation> arrayCreations;
	private final List<ReturnStatement> returnStatements;

	private MethodDeclaration currentMethod;
	private Map<ASTNode, MethodDeclaration> parentMethod;
	private Map<MethodDeclaration, Boolean> isMethodPure;

	private boolean insideAnonymousClass;

	private boolean methodRelevant;
	private boolean currentMethodPure;

	public FutureCollectionVisitor2(String classBinaryName, PreconditionWorker input) {
		this.classBinaryName = classBinaryName;

		typeDeclarations = new ArrayList<>();
		assignments = new ArrayList<>();
		fieldDeclarations = new ArrayList<>();
		methodInvocations = new ArrayList<>();
		varDeclStatements = new ArrayList<>();
		simpleNames = new ArrayList<>();
		classInstanceCreations = new ArrayList<>();
		singleVarDeclarations = new ArrayList<>();
		methodDeclarations = new ArrayList<>();
		arrayCreations = new ArrayList<>();
		returnStatements = new ArrayList<>();

		parentMethod = new HashMap<>();
		isMethodPure = new HashMap<>();
		
		instantiationUses = input.instantiationUses;
		collectionBindings = input.collectionBindings;
		collectionInstantiations = input.collectionInstantiations;
		collectionGetters = input.collectionGetters;
		collectionMethodDeclarations = input.collectionMethodDeclarations;
		iteratorBindings = input.iteratorBindings;
		collectionIterators = input.collectionIterators;
		toRefactorCollections = input.collectionCreationsToUses.keySet();
	}

	private void setPurity(boolean isPure) {
		currentMethodPure = isPure;
	}

	private void addParent(ASTNode node) {
		if (currentMethod == null)
			return;

		methodRelevant = true;

		parentMethod.put(node, currentMethod);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		Object fragment = node.fragments().get(0);
		if (fragment instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment variableDecl = (VariableDeclarationFragment)fragment;
			if (refactorVariable(variableDecl.getName()) || refactorIterator(variableDecl.getName())) {
				fieldDeclarations.add(node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {		
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
		Expression expr = fragment.getInitializer();
		
		if (refactorVariable(fragment.getName()) || refactorIterator(fragment.getName())){
			if (expr==null) {
				varDeclStatements.add(node);
				addParent(node);
			} else if (toRefactorCollections.contains(expr) || collectionIterators.containsValue(expr)) {
				varDeclStatements.add(node);
				addParent(node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (toRefactorCollections.contains(node)) {
			classInstanceCreations.add(node);
			addParent(node);
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		
		if (refactorVariable(simpleName)) {
			addSimpleName(simpleName);
		}
		return true;
	}

	private void addSimpleName(SimpleName simpleName) {
		if (!simpleNames.contains(simpleName)) {
			simpleNames.add(simpleName);

			addParent(simpleName);

			if (ASTUtils.isField(simpleName)) {
				setPurity(false);
			}
		}
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (collectionMethodDeclarations.containsKey(node)){
			methodDeclarations.add(node);
			methodRelevant = true;
		}
		return true;
	}

	/**
	 * This method finds methodInvocations that create Future instances within a collection.
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		if (node.arguments().size()==1) {
			if (collectionInstantiations.containsValue(node.arguments().get(0))){
				methodInvocations.add(node);
				addParent(node);
			}
					
		}
		for (Object arg : node.arguments()) {
			if (arg instanceof SimpleName) {
				SimpleName simpleName = (SimpleName) arg;
				ITypeBinding argType = simpleName.resolveTypeBinding();
				if (Types.isTypeOf(argType, collectionBinaryNames)
						&& ASTUtils.getTypeArgumentBinding(argType, classBinaryName) != null) {
					setPurity(false);
					if (!simpleNames.contains(simpleName)) {
						addParent(simpleName);
					}
				}
			}
		}
		return true;
	}
	/*
	@Override
	public boolean visit(ReturnStatement node) {

		// If the method was added that means it returns a list of futures.
		if (methodDeclarations.contains(currentMethod)) {
			addParent(node);
			returnStatements.add(node);
		}

		return true;
	}
	*/

	//TODO currently not found by UseDef
	/*
	@Override
	public boolean visit(ArrayCreation node) {

		ITypeBinding type = node.getType().getElementType().resolveBinding();

		if (Types.isExactTypeOf(type, classBinaryName)) {
			addParent(node);
			arrayCreations.add(node);
		}

		return true;
	}*/
	
	/**
	 * Determines whether a SimpleName should be refactored based on the whether it
	 * is binded to a Collection that should be refactored.
	 */
	private boolean refactorVariable(SimpleName simpleName){
		if (simpleName!=null) {
			IBinding binding = simpleName.resolveBinding();
			if (binding instanceof IVariableBinding) {
				if (collectionBindings.contains((IVariableBinding) binding))
					return true;	
			}
		}
		return false;
	}
	
	private boolean refactorIterator(SimpleName simpleName) {
		if (simpleName!=null) {
			IBinding binding = simpleName.resolveBinding();
			if (binding instanceof IVariableBinding) {
				if (iteratorBindings.contains((IVariableBinding) binding))
					return true;	
			}
		}
		return false;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		insideAnonymousClass = true;

		return true;
	}

	@Override
	public void endVisit(AnonymousClassDeclaration node) {
		insideAnonymousClass = false;
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
		return arrayCreations;
	}

	@Override
	public List<ReturnStatement> getReturnStatements() {
		return returnStatements;
	}

	@Override
	public Map<ASTNode, MethodDeclaration> getParentMethods() {
		return parentMethod;
	}

	@Override
	public Map<MethodDeclaration, Boolean> getIsMethodPures() {
		return isMethodPure;
	}
}
