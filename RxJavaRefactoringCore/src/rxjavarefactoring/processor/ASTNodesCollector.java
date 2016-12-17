package rxjavarefactoring.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import rxjavarefactoring.framework.refactoring.AbstractCollector;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class ASTNodesCollector extends AbstractCollector
{
	private final Map<ICompilationUnit, List<TypeDeclaration>> cuSubclassesMap;
	private final Map<ICompilationUnit, List<AnonymousClassDeclaration>> cuAnonymousClassesMap;
	private final Map<ICompilationUnit, List<VariableDeclaration>> cuAnonymousCachedClassesMap;
	private final Map<ICompilationUnit, List<MethodInvocation>> cuRelevantUsagesMap;

	public ASTNodesCollector(String collectorName)
	{
		super(collectorName);
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

	public void addAnonymCachedClassDecl( ICompilationUnit cu, List<VariableDeclaration> anonymCachedDeclarations )
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

	public Map<ICompilationUnit, List<VariableDeclaration>> getCuAnonymousCachedClassesMap()
	{
		return Collections.unmodifiableMap( cuAnonymousCachedClassesMap );
	}

	public Map<ICompilationUnit, List<MethodInvocation>> getCuRelevantUsagesMap()
	{
		return Collections.unmodifiableMap( cuRelevantUsagesMap );
	}

	public int getNumberOfCompilationUnits()
	{
		return cuSubclassesMap.size() + cuAnonymousClassesMap.size()
				+ cuAnonymousCachedClassesMap.size() + cuRelevantUsagesMap.size();
	}
}
