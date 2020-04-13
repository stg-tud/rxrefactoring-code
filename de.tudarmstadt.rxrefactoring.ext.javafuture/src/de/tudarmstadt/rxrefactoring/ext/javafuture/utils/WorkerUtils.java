
package de.tudarmstadt.rxrefactoring.ext.javafuture.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;
import de.tudarmstadt.rxrefactoring.core.utils.RelevantInvocation;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.FutureCollectionVisitor2;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.FutureVisitor3;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.VisitorNodes;

public class WorkerUtils {

	private static final List<WorkerIdentifier> allWorkerIdentifier = new ArrayList<WorkerIdentifier>();
	

	public static void fillAllWorkerIdentifierForFuture() {
		allWorkerIdentifier.add(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.FIELD_DECLARATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.ASSIGNMENTS_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.METHOD_INVOCATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.METHOD_DECLARATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.ARRAY_CREATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.SIMPLE_NAME_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.TYPE_DECL_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.RETURN_STATEMENT_IDENTIFIER);
	}


	
	public static List<WorkerIdentifier> getAllIdentifier() {
		return allWorkerIdentifier;
	}
	
	
	public static void clearKeys() {
		allWorkerIdentifier.clear();
	}
	
	
	public static List getNeededList(WorkerIdentifier identifier, VisitorNodes visitor) {

		if (identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER))
			return visitor.getVarDeclStatements();
		else if (identifier.equals(NamingUtils.TYPE_DECL_IDENTIFIER))
			return visitor.getTypeDeclarations();
		else if (identifier.equals(NamingUtils.FIELD_DECLARATION_IDENTIFIER))
			return visitor.getFieldDeclarations();
		else if (identifier.equals(NamingUtils.ASSIGNMENTS_IDENTIFIER))
			return visitor.getAssignments();
		else if (identifier.equals(NamingUtils.SIMPLE_NAME_IDENTIFIER))
			return visitor.getSimpleNames();
		else if (identifier.equals(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER))
			return visitor.getClassInstanceCreations();
		else if (identifier.equals(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER))
			return visitor.getSingleVarDeclarations();
		else if (identifier.equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER))
			return visitor.getMethodInvocations();
		else if (identifier.equals(NamingUtils.METHOD_DECLARATION_IDENTIFIER))
			return visitor.getMethodDeclarations();
		else if(identifier.equals(NamingUtils.RETURN_STATEMENT_IDENTIFIER))
			return visitor.getReturnStatements();
		else if (identifier.equals(NamingUtils.ARRAY_CREATION_IDENTIFIER))
			return visitor.getArrayCreations();
		else {
			throw new IllegalStateException("Key not in different Maps!");
		}
	}
	
		
		public static void addElementToList(WorkerIdentifier identifier, 
				IRewriteCompilationUnit unit, Object astNode, Map<String, CollectorGroup> groups, String key) {
			
			CollectorGroup group = groups.get(key);

			if (identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER))
				group.getVarDeclMap().put(unit, (VariableDeclarationStatement) astNode);
			else if (identifier.equals(NamingUtils.TYPE_DECL_IDENTIFIER))
				group.getTypeDeclMap().put(unit, (TypeDeclaration) astNode);
			else if (identifier.equals(NamingUtils.FIELD_DECLARATION_IDENTIFIER))
				group.getFieldDeclMap().put(unit, (FieldDeclaration) astNode);
			else if (identifier.equals(NamingUtils.ASSIGNMENTS_IDENTIFIER))
				group.getAssigmentsMap().put(unit, (Assignment) astNode);
			else if (identifier.equals(NamingUtils.SIMPLE_NAME_IDENTIFIER))
				group.getSimpleNamesMap().put(unit, (SimpleName) astNode);
			else if (identifier.equals(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER))
				group.getClassInstanceMap().put(unit, (ClassInstanceCreation) astNode);
			else if (identifier.equals(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER))
				group.getSingleVarDeclMap().put(unit, (SingleVariableDeclaration) astNode);
			else if (identifier.equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER))
				group.getMethodInvocationsMap().put(unit, (MethodInvocation) astNode);
			else if (identifier.equals(NamingUtils.METHOD_DECLARATION_IDENTIFIER))
				group.getMethodDeclarationsMap().put(unit, (MethodDeclaration) astNode);
			else if(identifier.equals(NamingUtils.RETURN_STATEMENT_IDENTIFIER))
				group.getReturnStatementsMap().put(unit, (ReturnStatement) astNode);
			else if (identifier.equals(NamingUtils.ARRAY_CREATION_IDENTIFIER))
				group.getArrayCreationsMap().put(unit, (ArrayCreation) astNode);
			else {
				throw new IllegalStateException("Identifier not found!");
			}
	}
}
