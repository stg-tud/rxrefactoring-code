package de.tudarmstadt.rxrefactoring.core.parser;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class ProjectUnits implements Set<BundledCompilationUnit> {

	private Set<BundledCompilationUnit> units;
	
	
	public ProjectUnits(Set<BundledCompilationUnit> units) {
		this.units = units;
	}
	
	public void applyChanges() throws IllegalArgumentException, MalformedTreeException, BadLocationException, CoreException {
		for (BundledCompilationUnit unit : units) {
			unit.applyChanges();
		}		
	}

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
	public Iterator<BundledCompilationUnit> iterator() {
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
	public boolean add(BundledCompilationUnit e) {
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
	public boolean addAll(Collection<? extends BundledCompilationUnit> c) {
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
