package de.tudarmstadt.rxrefactoring.core;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTVisitor;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;

public class UnitASTVisitor extends ASTVisitor {

	private IRewriteCompilationUnit unit;

	public UnitASTVisitor() {
		super();		
	}
	
	public void setUnit(@NonNull IRewriteCompilationUnit unit) {
		this.unit = unit;
	}
	
	@SuppressWarnings("null")
	protected @NonNull IRewriteCompilationUnit getUnit() {
		Objects.requireNonNull(unit, "The compilation unit has not been initialized.");
		return unit;
	}
	
	
}
