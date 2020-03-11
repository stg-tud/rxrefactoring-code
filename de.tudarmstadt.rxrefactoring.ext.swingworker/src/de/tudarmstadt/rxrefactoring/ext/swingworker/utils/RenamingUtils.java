package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class RenamingUtils {

	public static String getRightWorkerName(MethodDeclaration inMethod, SimpleName name) {
		return " in Method: " + inMethod.getName().getIdentifier() + ", name: " + name.getIdentifier();
	}
	

}
