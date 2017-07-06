package de.tudarmstadt.refactoringrx.core.collect;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

public class ASTCollector extends AbstractCollector {

		
	private final ASTParser parser;
	private ASTNode ast = null;
	
	public ASTCollector(IJavaProject project, String name) {	
		super(project, name);
		parser  = ASTParser.newParser(AST.JLS8);
	}

	@Override
	public void processCompilationUnit(ICompilationUnit unit) {
		parser.setSource(unit);
		ast = parser.createAST(null);
	}
}
