package de.tudarmstadt.rxrefactoring.core.parser;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class BundledCompilationUnitFactory {

	
	private final ASTParser parser;
		
	
	public BundledCompilationUnitFactory() {
		parser  = ASTParser.newParser(AST.JLS8);		
	}
	
	public BundledCompilationUnit from(ICompilationUnit unit) {
		initializeParser();
		parser.setSource(unit);
		return new BundledCompilationUnit(unit, parser.createAST(null));
	}
	
	private void initializeParser() {
		//parser.setKind(ASTParser.K_STATEMENTS);
		parser.setResolveBindings(true);
	}

	
}
