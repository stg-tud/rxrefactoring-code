
package de.tudarmstadt.rxrefactoring.ext.javafuture.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.FutureVisitor3;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;

public class WorkerUtils {

	private static final List<WorkerIdentifier> allWorkerIdentifier = new ArrayList<WorkerIdentifier>();
	

	public static void fillAllWorkerIdentifierForFuture() {
		allWorkerIdentifier.add(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.FIELD_DECLARATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.ASSIGNMENTS_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.METHOD_INVOCATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.METHOD_DECLARATION_IDENTIFIER);
	}

	/*public static void fillAllMap() {
		allMap.put(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER, varDeclMap);
		//allMap.put(NamingUtils.TYPE_DECL_IDENTIFIER, typeDeclMap);
		allMap.put(NamingUtils.FIELD_DECLARATION_IDENTIFIER, fieldDeclMap);
		allMap.put(NamingUtils.ASSIGNMENTS_IDENTIFIER, assigmentsMap);
		//allMap.put(NamingUtils.SIMPLE_NAME_IDENTIFIER, simpleNamesMap);
		//allMap.put(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER, classInstanceMap);
		allMap.put(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER, singleVarDeclMap);
		allMap.put(NamingUtils.METHOD_INVOCATION_IDENTIFIER, methodInvocationsMap);
		allMap.put(NamingUtils.METHOD_DECLARATION_IDENTIFIER, methodDeclarationsMap);
		//allMap.put(NamingUtils.RELEVANT_INVOCATION_IDENTIFIER, relevantInvocationsMap);

	}*/

	
	public static List<WorkerIdentifier> getAllIdentifier() {
		return allWorkerIdentifier;
	}

	
	/*public static void clearAllMaps() {
		allWorkerIdentifier.clear();
		allMap.clear();
		//typeDeclMap.clear();
		fieldDeclMap.clear();
		assigmentsMap.clear();
		varDeclMap.clear();
		//simpleNamesMap.clear();
		//classInstanceMap.clear();
		singleVarDeclMap.clear();
		methodInvocationsMap.clear();
		methodDeclarationsMap.clear();
		//relevantInvocationsMap.clear();
	}*/
	
	
	public static void clearKeys() {
		allWorkerIdentifier.clear();
	}
	
	public static List getNeededList(WorkerIdentifier identifier, FutureVisitor3 visitor) {

		if (identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER))
			return visitor.getVarDeclStatements();
		//else if (identifier.equals(NamingUtils.TYPE_DECL_IDENTIFIER))
		//	return visitor.getTypeDeclarations();
		else if (identifier.equals(NamingUtils.FIELD_DECLARATION_IDENTIFIER))
			return visitor.getFieldDeclarations();
		else if (identifier.equals(NamingUtils.ASSIGNMENTS_IDENTIFIER))
			return visitor.getAssignments();
		//else if (identifier.equals(NamingUtils.SIMPLE_NAME_IDENTIFIER))
		//	return visitor.getSimpleNames();
		//else if (identifier.equals(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER))
		//	return visitor.getClassInstanceCreations();
		else if (identifier.equals(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER))
			return visitor.getSingleVarDeclarations();
		else if (identifier.equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER))
			return visitor.getMethodInvocations();
		else if (identifier.equals(NamingUtils.METHOD_DECLARATION_IDENTIFIER))
			return visitor.getMethodDeclarations();
		else {
			throw new IllegalStateException("Key not in different Maps!");
		}
	}
		
		public static void addElementToList(WorkerIdentifier identifier, FutureVisitor3 visitor, 
				IRewriteCompilationUnit unit, Object astNode, CollectorGroup group) {

			if (identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER))
				group.getVarDeclMap().put(unit, (VariableDeclarationStatement) astNode);
			//else if (identifier.equals(NamingUtils.TYPE_DECL_IDENTIFIER))
			//	typeDeclMap.put(unit, (TypeDeclaration) astNode);
			else if (identifier.equals(NamingUtils.FIELD_DECLARATION_IDENTIFIER))
				group.getFieldDeclMap().put(unit, (FieldDeclaration) astNode);
			else if (identifier.equals(NamingUtils.ASSIGNMENTS_IDENTIFIER))
				group.getAssigmentsMap().put(unit, (Assignment) astNode);
			//else if (identifier.equals(NamingUtils.SIMPLE_NAME_IDENTIFIER))
			//	simpleNamesMap.put(unit, (SimpleName) astNode);
			//else if (identifier.equals(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER))
			//	classInstanceMap.put(unit, (ClassInstanceCreation) astNode);
			else if (identifier.equals(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER))
				group.getSingleVarDeclMap().put(unit, (SingleVariableDeclaration) astNode);
			else if (identifier.equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER))
				group.getMethodInvocationsMap().put(unit, (MethodInvocation) astNode);
			else if (identifier.equals(NamingUtils.METHOD_DECLARATION_IDENTIFIER))
				group.getMethodDeclarationsMap().put(unit, (MethodDeclaration) astNode);
			//else if (identifier.equals(NamingUtils.RELEVANT_INVOCATION_IDENTIFIER))
			//	relevantInvocationsMap.put(unit, (RelevantInvocation) astNode);
			else {
				throw new IllegalStateException("Identifier not found!");
			}
	}
}
