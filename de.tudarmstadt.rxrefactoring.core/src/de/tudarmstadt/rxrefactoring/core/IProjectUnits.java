package de.tudarmstadt.rxrefactoring.core;

import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;


public interface IProjectUnits<T extends IRewriteCompilationUnit> extends Set<T>  {
	
	/**
	 * Accepts a visitor for each compilation unit in this
	 * set.
	 * 
	 * @param visitor the non-null visitor to accept.
	 */
	default public void accept(@NonNull UnitASTVisitor visitor) {
		for (IRewriteCompilationUnit unit : this) {
			
			Objects.requireNonNull(unit, "an element of ProjectUnits was null");				
			visitor.setUnit(unit);
			unit.accept(visitor);
		}
		
	}
}
