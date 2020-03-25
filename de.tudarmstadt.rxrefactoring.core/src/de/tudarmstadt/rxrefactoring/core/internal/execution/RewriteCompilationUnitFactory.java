package de.tudarmstadt.rxrefactoring.core.internal.execution;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class RewriteCompilationUnitFactory { // THA umbauen, dass nicht public sein muss

	private final ASTParser parser;

	public RewriteCompilationUnitFactory() {
		parser = ASTParser.newParser(AST.JLS10);
	}

	@SuppressWarnings("null")
	public RewriteCompilationUnit from(@NonNull ICompilationUnit unit) {
		initializeParser();
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		return new RewriteCompilationUnit(unit, parser.createAST(null));
	}

	private void initializeParser() {
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
	}
}
