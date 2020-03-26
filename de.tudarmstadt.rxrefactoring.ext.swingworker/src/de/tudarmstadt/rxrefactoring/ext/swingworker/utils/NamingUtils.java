package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;

public class NamingUtils {
	
	public final static WorkerIdentifier ASSIGNMENTS_IDENTIFIER = new WorkerIdentifier("Assignments");
	public final static WorkerIdentifier CLASS_INSTANCE_CREATION_IDENTIFIER = new WorkerIdentifier("Class Instances");
	public final static WorkerIdentifier FIELD_DECLARATION_IDENTIFIER = new WorkerIdentifier("Field Declarations");
	public final static WorkerIdentifier METHOD_DECLARATION_IDENTIFIER = new WorkerIdentifier("Method Declarations");
	public final static WorkerIdentifier METHOD_INVOCATION_IDENTIFIER = new WorkerIdentifier("Method Invocations");
	public final static WorkerIdentifier RELEVANT_INVOCATION_IDENTIFIER = new WorkerIdentifier("Relevant Invocations");
	public final static WorkerIdentifier SIMPLE_NAME_IDENTIFIER = new WorkerIdentifier("Simple Names");
	public final static WorkerIdentifier SINGLE_VAR_DECL_IDENTIFIER = new WorkerIdentifier("Single Variable Declarations");
	public final static WorkerIdentifier TYPE_DECL_IDENTIFIER = new WorkerIdentifier("Type Declarations");
	public final static WorkerIdentifier VAR_DECL_STATEMENT_IDENTIFIER = new WorkerIdentifier("Variable Declarations");
	

	public static String getRightWorkerName(MethodDeclaration inMethod, SimpleName name) {
		return " in Method: " + inMethod.getName().getIdentifier() + ", name: " + name.getIdentifier();
	}
	
	
/*	public WorkerIdentifier getAssignmentIdentifier() {
		return new WorkerIdentifier(ASSIGNMENTS_NAME);
	}
	
	public WorkerIdentifier getClassInstanceCreationIdentifier() {
		return new WorkerIdentifier(CLASS_INSTANCE_CREATION_NAME);
	}
	
	public WorkerIdentifier getFieldDeclIdentifier() {
		return new WorkerIdentifier(FIELD_DECLARATION_NAME);
	}
	
	public WorkerIdentifier getMethodDeclIdentifier() {
		return new WorkerIdentifier(METHOD_DECLARATION_NAME);
	}
	
	public WorkerIdentifier getMethodInvocationIdentifier() {
		return new WorkerIdentifier(METHOD_INVOCATION_NAME);
	}
	
	public WorkerIdentifier getRelevantInvocationIdentifier() {
		return new WorkerIdentifier(RELEVANT_INVOCATION_NAME);
	}
	
	public WorkerIdentifier getSimpleNameIdentifier() {
		return new WorkerIdentifier(SIMPLE_NAME_NAME);
	}
	
	public WorkerIdentifier getSingleVarDeclIdentifier() {
		return new WorkerIdentifier(SINGLE_VAR_DECL_NAME);
	}
	
	public WorkerIdentifier getTypeDeclIdentifier() {
		return new WorkerIdentifier(SINGLE_VAR_DECL_NAME);
	}*/
	
	
	
	

}
