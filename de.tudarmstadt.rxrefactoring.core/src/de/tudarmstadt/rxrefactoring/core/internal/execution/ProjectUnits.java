package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.text.edits.MalformedTreeException;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;

/**
 * A set of {@link IRewriteCompilationUnit} enhanced with utility methods.
 * 
 * @author mirko
 *
 */
public class ProjectUnits implements IProjectUnits<RewriteCompilationUnit> {

	private final @NonNull Set<RewriteCompilationUnit> units;

	public ProjectUnits(@NonNull Set<RewriteCompilationUnit> units) {
		Objects.requireNonNull(units, "The initial units can not be null");

		this.units = units;
	}

	@SuppressWarnings("null")
	public ProjectUnits() {
		this(Sets.newHashSet());
	}

	
	protected void addChangesTo(CompositeChange changes)
			throws IllegalArgumentException, MalformedTreeException, BadLocationException, CoreException {		
		
		for (RewriteCompilationUnit unit : units) {			
				unit.getChangedDocument().ifPresent(doc -> {
					changes.add(doc);
				});			
			
		}
	}

	public void accept(@NonNull UnitASTVisitor visitor) {
		for (IRewriteCompilationUnit unit : units) {
			Objects.requireNonNull(unit, "an element of ProjectUnits was null");			
			visitor.setUnit(unit);
			unit.accept(visitor);
		}
	}

	/*
	 * Methods defined by Set
	 */
	@Override
	public int size() {
		return units.size();
	}

	@Override
	public boolean isEmpty() {
		return units.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return units.contains(o);
	}

	@Override
	public Iterator<RewriteCompilationUnit> iterator() {
		return units.iterator();
	}

	@Override
	public Object[] toArray() {
		return units.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return units.toArray(a);
	}

	@Override
	public boolean add(RewriteCompilationUnit e) {
		throw new UnsupportedOperationException("This class is not mutable.");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("This class is not mutable.");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return units.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends RewriteCompilationUnit> c) {
		return units.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return units.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return units.removeAll(c);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("This class is not mutable.");
	}

}
