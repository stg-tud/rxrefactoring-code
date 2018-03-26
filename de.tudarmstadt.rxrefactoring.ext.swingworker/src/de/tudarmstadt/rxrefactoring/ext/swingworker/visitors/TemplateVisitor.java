package de.tudarmstadt.rxrefactoring.ext.swingworker.visitors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;

/**
 * Description: This visitor collects method and type declarations from source
 * code produced using FreeMarker templates. Author: Camila Gonzalez<br>
 * Created: 15/02/2018
 */
public class TemplateVisitor extends ASTVisitor {

	private MethodDeclaration methodDeclaration;
	private TypeDeclaration typeDeclaration;
	private int counter;

	private TemplateVisitor() {
		counter = 0;
		// This class should not be instantiated
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		methodDeclaration = node;
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (counter == 0) {
			typeDeclaration = node;
		}
		// Return true only the first time
		return counter++ == 0;
	}

	/**
	 * Parses the source code and sets the fields of a {@link TemplateVisitor}
	 * 
	 * @param sc
	 *            source code with valid syntax
	 * @return a {@link TemplateVisitor}
	 */
	private static synchronized TemplateVisitor initVisitor(String sc) {
		ASTParser javaParser = ASTParser.newParser(AST.JLS10);
		javaParser.setSource(sc.toCharArray());
		TemplateVisitor visitor = new TemplateVisitor();
		javaParser.createAST(null).accept(visitor);
		return visitor;
	}

	/**
	 * Creates a {@link TypeDeclaration} given its source code
	 * 
	 * @param targetAST
	 *            target {@link AST}
	 * @param typeDeclaration
	 *            class (Type) source code. It must have a valid syntax
	 * @return a {@link TypeDeclaration} based on the source code
	 */
	public static @NonNull TypeDeclaration createTypeDeclarationFromText(AST targetAST, String typeDeclaration) {
		synchronized (targetAST) {
			TypeDeclaration typeDec = initVisitor(typeDeclaration).typeDeclaration;
			return (TypeDeclaration) SwingWorkerASTUtils.copySubtree(targetAST, typeDec);
		}
	}

	/**
	 * Creates a {@link MethodDeclaration} given its source code
	 * 
	 * @param targetAST
	 *            target {@link AST}
	 * @param method
	 *            method source code. It must have a valid syntax
	 * @return a {@link MethodDeclaration} based on the source code
	 */
	public static @NonNull MethodDeclaration createMethodFromText(AST targetAST, String method) {
		String auxClassStart = "public class AuxClass { ";
		String auxClassEnd = "}";
		String auxClass = auxClassStart + method + auxClassEnd;
		synchronized (targetAST) {
			MethodDeclaration methodDec = initVisitor(auxClass).methodDeclaration;
			return (MethodDeclaration) SwingWorkerASTUtils.copySubtree(targetAST, methodDec);
		}
	}

	/**
	 * Creates a {@link Statement} given its source code (without ";")
	 * 
	 * @param targetAST
	 *            target {@link AST}
	 * @param statement
	 *            statement source code. It must have a valid syntax. It cannot
	 *            contain a ";".
	 * @return a {@link Statement} based on the source code
	 */
	public static @NonNull Statement createSingleStatementFromText(AST targetAST, String statement) {
		String auxMethodStart = "public void auxMethod() {";
		String auxMethodEnd = "}";
		String auxMethod = auxMethodStart + statement + ";" + auxMethodEnd;
		synchronized (targetAST) {
			MethodDeclaration methodFromText1 = createMethodFromText(targetAST, auxMethod);
			ASTNode node = (ASTNode) methodFromText1.getBody().statements().get(0);
			return (Statement) SwingWorkerASTUtils.copySubtree(targetAST, node);
		}
	}
}
