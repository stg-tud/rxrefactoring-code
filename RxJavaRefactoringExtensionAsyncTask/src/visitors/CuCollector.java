package visitors;

import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import rxjavarefactoring.framework.refactoring.AbstractCollector;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class CuCollector extends AbstractCollector
{
	private IProject project;

	private final Map<ICompilationUnit, List<TypeDeclaration>> cuSubclassesMap;
	private final Map<ICompilationUnit, List<AnonymousClassDeclaration>> cuAnonymousClassesMap;
	private final Map<ICompilationUnit, List<ASTNode>> cuAnonymousCachedClassesMap;
	private final Map<ICompilationUnit, List<MethodInvocation>> cuRelevantUsagesMap;

	public CuCollector(IProject project, String collectorName )
	{
		super( collectorName );
		this.project = project;
		cuSubclassesMap = new HashMap<>();
		cuAnonymousClassesMap = new HashMap<>();
		cuAnonymousCachedClassesMap = new HashMap<>();
		cuRelevantUsagesMap = new HashMap<>();
	}

	public void addSubclasses( ICompilationUnit cu, List<TypeDeclaration> subclasses )
	{
		addToMap( cu, subclasses, cuSubclassesMap );
	}

	public void addAnonymClassDecl( ICompilationUnit cu, List<AnonymousClassDeclaration> anonymDeclarations )
	{
		addToMap( cu, anonymDeclarations, cuAnonymousClassesMap );
	}

	public void addAnonymCachedClassDecl( ICompilationUnit cu, List<ASTNode> anonymCachedDeclarations )
	{
		addToMap( cu, anonymCachedDeclarations, cuAnonymousCachedClassesMap );
	}

	public void addRelevantUsages( ICompilationUnit cu, List<MethodInvocation> usages )
	{
		addToMap( cu, usages, cuRelevantUsagesMap );
	}

	public Map<ICompilationUnit, List<TypeDeclaration>> getCuSubclassesMap()
	{
		return Collections.unmodifiableMap( cuSubclassesMap );
	}

	public Map<ICompilationUnit, List<AnonymousClassDeclaration>> getCuAnonymousClassesMap()
	{
		return Collections.unmodifiableMap( cuAnonymousClassesMap );
	}

	public Map<ICompilationUnit, List<ASTNode>> getCuAnonymousCachedClassesMap()
	{
		return Collections.unmodifiableMap( cuAnonymousCachedClassesMap );
	}

	public Map<ICompilationUnit, List<MethodInvocation>> getCuRelevantUsagesMap()
	{
		return Collections.unmodifiableMap( cuRelevantUsagesMap );
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

	@Override
	public String getInfo()
	{
		return "\n******************************************************************\n" +
				getDetails() +
				"\n******************************************************************";
	}

	@Override
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
				"Project = " + project.getName() + "\n" +
				"Subclasses = " + cuSubclassesMap.values().size() + "\n" +
				"Anonymous Classes = " + cuAnonymousClassesMap.values().size() + "\n" +
				"Anonymous Cached Classes = " + cuAnonymousCachedClassesMap.values().size() + "\n" +
				"Relevant Usages = " + cuRelevantUsagesMap.values().size() + "\n";
	}
}
