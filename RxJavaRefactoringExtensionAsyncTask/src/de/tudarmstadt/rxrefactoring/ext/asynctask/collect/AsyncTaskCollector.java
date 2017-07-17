package de.tudarmstadt.rxrefactoring.ext.asynctask.collect;

import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.collect.AbstractCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.ClassDetails;


/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AsyncTaskCollector extends AbstractCollector {

	private final Multimap<ICompilationUnit, TypeDeclaration> subclassesMap;
	private final Multimap<ICompilationUnit, AnonymousClassDeclaration> anonymousClassesMap;
	private final Multimap<ICompilationUnit, ASTNode> anonymousCachedClassesMap;
	private final Multimap<ICompilationUnit, MethodInvocation> relevantUsagesMap;

	public AsyncTaskCollector(IJavaProject project, String collectorName )
	{
		super(project, collectorName);
		subclassesMap = HashMultimap.create();
		anonymousClassesMap = HashMultimap.create();
		anonymousCachedClassesMap = HashMultimap.create();
		relevantUsagesMap = HashMultimap.create();
	}

	public void addSubclasses(ICompilationUnit cu, Iterable<TypeDeclaration> subclasses) {
		subclassesMap.putAll(cu, subclasses);
	}

	public void addAnonymClassDecl( ICompilationUnit cu, Iterable<AnonymousClassDeclaration> anonymDeclarations ) {
		anonymousClassesMap.putAll(cu, anonymDeclarations);
	}

	public void addAnonymCachedClassDecl( ICompilationUnit cu, List<ASTNode> anonymCachedDeclarations )
	{
		anonymousCachedClassesMap.putAll(cu, anonymCachedDeclarations);
	}

	public void addRelevantUsages( ICompilationUnit cu, List<MethodInvocation> usages )
	{
		relevantUsagesMap.putAll(cu, usages);
	}

	public Multimap<ICompilationUnit, TypeDeclaration> getCuSubclassesMap() {
		return subclassesMap;
	}

	public Multimap<ICompilationUnit, AnonymousClassDeclaration> getCuAnonymousClassesMap()	{
		return anonymousClassesMap;
	}

	public Multimap<ICompilationUnit, ASTNode> getCuAnonymousCachedClassesMap()	{
		return anonymousCachedClassesMap;
	}

	public Multimap<ICompilationUnit, MethodInvocation> getCuRelevantUsagesMap()	{
		return relevantUsagesMap;
	}

	public int getNumberOfCompilationUnits()
	{
		Set<ICompilationUnit> allCompilationUnits = new HashSet<>();
		allCompilationUnits.addAll( subclassesMap.keySet() );
		allCompilationUnits.addAll( anonymousClassesMap.keySet() );
		allCompilationUnits.addAll( anonymousCachedClassesMap.keySet() );
		allCompilationUnits.addAll( relevantUsagesMap.keySet() );
		return allCompilationUnits.size();
	}

	public String getInfo()
	{
		return "\n******************************************************************\n" +
				getDetails() +
				"\n******************************************************************";
	}

	public String getError()
	{
		return "\n******************************************************************\n" +
				" [ ERROR during refactoring ]\n" +
				getDetails() +
				"\n******************************************************************";
	}

	public String getDetails()
	{
		return "Nr. files: " + getNumberOfCompilationUnits() + "\n" +
				"Project = " + getProject().toString() + "\n" +
				"Subclasses = " + subclassesMap.values().size() + "\n" +
				"Anonymous Classes = " + anonymousClassesMap.values().size() + "\n" +
				"Anonymous Cached Classes = " + anonymousCachedClassesMap.values().size() + "\n" +
				"Relevant Usages = " + relevantUsagesMap.values().size() + "\n";
	}

	@Override
	public void processCompilationUnit(ICompilationUnit unit) {
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );

		DeclarationVisitor declarationVisitor = new DeclarationVisitor( ClassDetails.ASYNC_TASK );
		UsagesVisitor usagesVisitor = new UsagesVisitor( ClassDetails.ASYNC_TASK );
		cu.accept( declarationVisitor );
		cu.accept( usagesVisitor );

		// Cache relevant information in an object that contains maps
		addSubclasses( unit, declarationVisitor.getSubclasses() );
		addAnonymClassDecl( unit, declarationVisitor.getAnonymousClasses() );
		addAnonymCachedClassDecl( unit, declarationVisitor.getAnonymousCachedClasses() );
		addRelevantUsages( unit, usagesVisitor.getUsages() );		
	}
}
