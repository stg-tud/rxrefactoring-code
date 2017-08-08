package de.tudarmstadt.rxrefactoring.ext.asynctask.collect;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.ClassDetails;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AsyncTaskCollector implements IWorker<Void,AsyncTaskCollector> {

	private final Multimap<BundledCompilationUnit, TypeDeclaration> subclassesMap;
	private final Multimap<BundledCompilationUnit, AnonymousClassDeclaration> anonymousClassesMap;
	private final Multimap<BundledCompilationUnit, AnonymousClassDeclaration> anonymousCachedClassesMap;
	private final Multimap<BundledCompilationUnit, MethodInvocation> relevantUsagesMap;
	
	public AsyncTaskCollector(String collectorName) {
		subclassesMap = HashMultimap.create();
		anonymousClassesMap = HashMultimap.create();
		anonymousCachedClassesMap = HashMultimap.create();
		relevantUsagesMap = HashMultimap.create();
	}
	
	@Override
	public AsyncTaskCollector refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {	
		units.forEach(unit -> processBundledCompilationUnit(unit));		
		return this;
	}	

	public void addSubclasses(BundledCompilationUnit cu, Iterable<TypeDeclaration> subclasses) {
		subclassesMap.putAll(cu, subclasses);
	}

	public void addAnonymClassDecl(BundledCompilationUnit cu, Iterable<AnonymousClassDeclaration> anonymDeclarations) {
		anonymousClassesMap.putAll(cu, anonymDeclarations);
	}

	public void addAnonymCachedClassDecl(BundledCompilationUnit cu, Iterable<AnonymousClassDeclaration> anonymCachedDeclarations) {
		anonymousCachedClassesMap.putAll(cu, anonymCachedDeclarations);
	}

	public void addRelevantUsages(BundledCompilationUnit cu, Iterable<MethodInvocation> usages) {
		relevantUsagesMap.putAll(cu, usages);
	}

	public Multimap<BundledCompilationUnit, TypeDeclaration> getSubclasses() {
		return subclassesMap;
	}

	public Multimap<BundledCompilationUnit, AnonymousClassDeclaration> getAnonymousClasses() {
		return anonymousClassesMap;
	}

	public Multimap<BundledCompilationUnit, AnonymousClassDeclaration> getAnonymousCachedClasses() {
		return anonymousCachedClassesMap;
	}

	public Multimap<BundledCompilationUnit, MethodInvocation> getRelevantUsages() {
		return relevantUsagesMap;
	}

	public int getNumberOfCompilationUnits() {
		Set<ICompilationUnit> allCompilationUnits = new HashSet<>();
		allCompilationUnits.addAll(subclassesMap.keySet());
		allCompilationUnits.addAll(anonymousClassesMap.keySet());
		allCompilationUnits.addAll(anonymousCachedClassesMap.keySet());
		allCompilationUnits.addAll(relevantUsagesMap.keySet());
		return allCompilationUnits.size();
	}

	private void processBundledCompilationUnit(BundledCompilationUnit unit) {
		ASTNode root = unit.getRoot();
		
		DeclarationVisitor declarationVisitor = new DeclarationVisitor(ClassDetails.ASYNC_TASK);
		UsagesVisitor usagesVisitor = new UsagesVisitor(ClassDetails.ASYNC_TASK);
		root.accept(declarationVisitor);
		root.accept(usagesVisitor);

		// Cache relevant information in an object that contains maps
		addSubclasses(unit, declarationVisitor.getSubclasses());
		addAnonymClassDecl(unit, declarationVisitor.getAnonymousClasses());
		addAnonymCachedClassDecl(unit, declarationVisitor.getAnonymousCachedClasses());
		addRelevantUsages(unit, usagesVisitor.getUsages());	
	}

	
}
