package de.tudarmstadt.rxrefactoring.core;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class RewriteCompilationUnitFactory {

	private final ASTParser parser;

	public RewriteCompilationUnitFactory() {
		parser = ASTParser.newParser(AST.JLS8);
	}

	public RewriteCompilationUnit from(ICompilationUnit unit) {
		initializeParser();
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		return new RewriteCompilationUnit(unit, parser.createAST(null));
	}

	private void initializeParser() {
		parser.setResolveBindings(true);
	}

}
