package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.MalformedTreeException;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerUtils;

/**
 * A set of {@link RewriteCompilationUnit} enhanced with utility methods.
 * 
 * @author mirko
 *
 */
public class ProjectUnits implements IProjectUnits {

	private final @NonNull IJavaProject project;

	private final @NonNull Set<RewriteCompilationUnit> units;

	protected ProjectUnits(@NonNull IJavaProject project, @NonNull Set<RewriteCompilationUnit> units) {
		Objects.requireNonNull(units, "The initial units can not be null");

		this.project = project;
		this.units = units;
	}

//	@SuppressWarnings("null")
//	protected ProjectUnits() {
//		this(Sets.newHashSet());
//	}

	public Set<IRewriteCompilationUnit> getUnits() {
		@SuppressWarnings("null")
		@NonNull
		Set<IRewriteCompilationUnit> result = Sets.newConcurrentHashSet();
		result.addAll(this.units);
		return result;
	}

	public IJavaProject getJavaProject() {
		return this.project;
	}

	public IRewriteCompilationUnit getAtPosition(int pos) {
		List<RewriteCompilationUnit> list = new ArrayList<>(units);
		return list.get(pos);
	}

	/**
	 * Adds the changes that are stored in this sets compilation units to a
	 * {@link CompositeChange} object.
	 * 
	 * @param changes the object that should be modified
	 */
	protected void addChangesTo(@NonNull CompositeChange changes)
			throws IllegalArgumentException, MalformedTreeException, BadLocationException, CoreException {

			units.iterator().next().getChangedDocumentForListOfUnits(units).ifPresent(doc -> {
				changes.add(doc);
			});
	}

	public void accept(@NonNull UnitASTVisitor visitor) {
		for (IRewriteCompilationUnit unit : units) {
			Objects.requireNonNull(unit, "an element of ProjectUnits was null");
			visitor.setUnit(unit);
			unit.accept(visitor);
		}
	}


	@SuppressWarnings("null")
	@Override
	public @NonNull IJavaSearchScope getSearchScope() {
		return SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
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
	public boolean containsAll(Collection<?> c) {
		return units.containsAll(c);
	}

	@Override
	public Iterator<IRewriteCompilationUnit> iterator() {
		// Wrap Iterator<IRewriteCompilationUnit> around
		// Iterator<RewriteCompilationUnit>
		return new Iterator<IRewriteCompilationUnit>() {

			private final Iterator<RewriteCompilationUnit> it = units.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public IRewriteCompilationUnit next() {
				return it.next();
			}
		};
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
	public boolean add(IRewriteCompilationUnit e) {
		if (e == null || units.contains(e))
			return false;
		else {
			units.add((RewriteCompilationUnit) e);
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends IRewriteCompilationUnit> c) {
		if (c == null || units.containsAll(c) || c.isEmpty())
			return false;
		for (IRewriteCompilationUnit unit : c) {
			units.add((RewriteCompilationUnit) unit);
		}
		return true;
	}

	@Override
	public boolean remove(Object o) {
		if (o == null || !units.contains(o))
			return false;
		else {
			units.remove(o);
		}
		return true;
	}

}
