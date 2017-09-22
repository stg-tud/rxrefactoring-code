package de.tudarmstadt.rxrefactoring.core;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class UnitASTVisitor extends ASTVisitor {

	private RewriteCompilationUnit unit;

	public UnitASTVisitor() {
		super();		
	}
	
	void setUnit(@NonNull RewriteCompilationUnit unit) {
		this.unit = unit;
	}
	
	@SuppressWarnings("null")
	protected @NonNull RewriteCompilationUnit getUnit() {
		Objects.requireNonNull(unit, "The compilation unit has not been initialized.");
		return unit;
	}
	
	
}
