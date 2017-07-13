package visitors;

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
import domain.ClassDetails;


/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class CuCollector extends AbstractCollector {

	private final Multimap<ICompilationUnit, TypeDeclaration> cuSubclassesMap;
	private final Multimap<ICompilationUnit, AnonymousClassDeclaration> cuAnonymousClassesMap;
	private final Multimap<ICompilationUnit, ASTNode> cuAnonymousCachedClassesMap;
	private final Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap;

	public CuCollector(IJavaProject project, String collectorName )
	{
		super(project, collectorName);
		cuSubclassesMap = HashMultimap.create();
		cuAnonymousClassesMap = HashMultimap.create();
		cuAnonymousCachedClassesMap = HashMultimap.create();
		cuRelevantUsagesMap = HashMultimap.create();
	}

	public void addSubclasses(ICompilationUnit cu, Iterable<TypeDeclaration> subclasses) {
		cuSubclassesMap.putAll(cu, subclasses);
	}

	public void addAnonymClassDecl( ICompilationUnit cu, Iterable<AnonymousClassDeclaration> anonymDeclarations ) {
		cuAnonymousClassesMap.putAll(cu, anonymDeclarations);
	}

	public void addAnonymCachedClassDecl( ICompilationUnit cu, List<ASTNode> anonymCachedDeclarations )
	{
		cuAnonymousCachedClassesMap.putAll(cu, anonymCachedDeclarations);
	}

	public void addRelevantUsages( ICompilationUnit cu, List<MethodInvocation> usages )
	{
		cuRelevantUsagesMap.putAll(cu, usages);
	}

	public Multimap<ICompilationUnit, TypeDeclaration> getCuSubclassesMap() {
		return cuSubclassesMap;
	}

	public Multimap<ICompilationUnit, AnonymousClassDeclaration> getCuAnonymousClassesMap()	{
		return cuAnonymousClassesMap;
	}

	public Multimap<ICompilationUnit, ASTNode> getCuAnonymousCachedClassesMap()	{
		return cuAnonymousCachedClassesMap;
	}

	public Multimap<ICompilationUnit, MethodInvocation> getCuRelevantUsagesMap()	{
		return cuRelevantUsagesMap;
	}

	public int getNumberOfCompilationUnits()
	{
		Set<ICompilationUnit> allCompilationUnits = new HashSet<>();
		allCompilationUnits.addAll( cuSubclassesMap.keySet() );
		allCompilationUnits.addAll( cuAnonymousClassesMap.keySet() );
		allCompilationUnits.addAll( cuAnonymousCachedClassesMap.keySet() );
		allCompilationUnits.addAll( cuRelevantUsagesMap.keySet() );
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
				"Subclasses = " + cuSubclassesMap.values().size() + "\n" +
				"Anonymous Classes = " + cuAnonymousClassesMap.values().size() + "\n" +
				"Anonymous Cached Classes = " + cuAnonymousCachedClassesMap.values().size() + "\n" +
				"Relevant Usages = " + cuRelevantUsagesMap.values().size() + "\n";
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
