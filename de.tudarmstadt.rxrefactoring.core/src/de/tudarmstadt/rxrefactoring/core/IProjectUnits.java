package de.tudarmstadt.rxrefactoring.core;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.search.IJavaSearchScope;

/**
 * An immutable set of compilation units.
 * 
 * @author mirko
 *
 */
public interface IProjectUnits extends Set<IRewriteCompilationUnit> {

	public @NonNull IJavaSearchScope getSearchScope();

	/**
	 * Accepts a visitor for each compilation unit in this set.
	 * 
	 * @param visitor
	 *            the non-null visitor to accept.
	 */
	default public void accept(@NonNull UnitASTVisitor visitor) {
		for (IRewriteCompilationUnit unit : this) {

			Objects.requireNonNull(unit, "an element of ProjectUnits was null");
			visitor.setUnit(unit);
			unit.accept(visitor);
		}
	}

	@Override
	public boolean add(IRewriteCompilationUnit e);
	

	public IRewriteCompilationUnit getAtPosition(int pos);
	

	@Override
	default public boolean remove(Object o) {
		throw new UnsupportedOperationException("this set is not mutable");
	}

	@Override
	public boolean addAll(Collection<? extends IRewriteCompilationUnit> c);

	@Override
	default public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("this set is not mutable");
	}

	@Override
	default public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("this set is not mutable");
	}

	@Override
	default public void clear() {
		throw new UnsupportedOperationException("this set is not mutable");
	}

	public Set<IRewriteCompilationUnit> getUnits();
}
