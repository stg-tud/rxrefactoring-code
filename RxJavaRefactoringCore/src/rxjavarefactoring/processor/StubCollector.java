package rxjavarefactoring.processor;

import java.util.*;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.refactoring.AbstractCollector;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class StubCollector extends AbstractCollector
{
	private final Map<ICompilationUnit, List<TypeDeclaration>> typeDeclMap;
	private final Map<ICompilationUnit, List<AnonymousClassDeclaration>> cuAnonymousClassDeclMap;
	private final Map<ICompilationUnit, List<VariableDeclaration>> variableDeclMap;
	private final Map<ICompilationUnit, List<Assignment>> assignmentsMap;
	private final Map<ICompilationUnit, List<FieldDeclaration>> fieldDeclarationsMap;
	private final Map<ICompilationUnit, List<MethodInvocation>> methodInvocationMap;

	public StubCollector(String collectorName )
	{
		super( collectorName );
		typeDeclMap = new HashMap<>();
		cuAnonymousClassDeclMap = new HashMap<>();
		variableDeclMap = new HashMap<>();
		methodInvocationMap = new HashMap<>();
		assignmentsMap = new HashMap<>();
		fieldDeclarationsMap = new HashMap<>();
	}

	public void addSubclasses( ICompilationUnit cu, List<TypeDeclaration> subclasses )
	{
		addToMap( cu, subclasses, typeDeclMap );
	}

	public void addAnonymClassDecl( ICompilationUnit cu, List<AnonymousClassDeclaration> anonymDeclarations )
	{
		addToMap( cu, anonymDeclarations, cuAnonymousClassDeclMap );
	}

	public void addVariableDeclarations( ICompilationUnit cu, List<VariableDeclaration> anonymCachedDeclarations )
	{
		addToMap( cu, anonymCachedDeclarations, variableDeclMap );
	}

	public void addAssignments( ICompilationUnit cu, List<Assignment> assignments )
	{
		addToMap( cu, assignments, assignmentsMap );
	}

	public void addMethodInvocatons( ICompilationUnit cu, List<MethodInvocation> methodInvocations )
	{
		addToMap( cu, methodInvocations, methodInvocationMap );
	}

	public void addFieldDeclarations( ICompilationUnit cu, List<FieldDeclaration> fieldDeclarations )
	{
		addToMap( cu, fieldDeclarations, fieldDeclarationsMap );
	}

	public Map<ICompilationUnit, List<TypeDeclaration>> getTypeDeclMap()
	{
		return Collections.unmodifiableMap( typeDeclMap );
	}

	public Map<ICompilationUnit, List<AnonymousClassDeclaration>> getCuAnonymousClassDeclMap()
	{
		return Collections.unmodifiableMap( cuAnonymousClassDeclMap );
	}

	public Map<ICompilationUnit, List<VariableDeclaration>> getVariableDeclMap()
	{
		return Collections.unmodifiableMap( variableDeclMap );
	}

	public Map<ICompilationUnit, List<Assignment>> getAssigmentsMap()
	{
		return Collections.unmodifiableMap( assignmentsMap );
	}

	public Map<ICompilationUnit, List<MethodInvocation>> getMethodInvocationMap()
	{
		return Collections.unmodifiableMap( methodInvocationMap );
	}

	public Map<ICompilationUnit, List<FieldDeclaration>> getFieldDeclarationsMap()
	{
		return Collections.unmodifiableMap( fieldDeclarationsMap );
	}

	public int getNumberOfCompilationUnits()
	{
		Set<ICompilationUnit> allCompilationUnits = new HashSet<>();
		allCompilationUnits.addAll( typeDeclMap.keySet() );
		allCompilationUnits.addAll( cuAnonymousClassDeclMap.keySet() );
		allCompilationUnits.addAll( variableDeclMap.keySet() );
		allCompilationUnits.addAll( assignmentsMap.keySet() );
		allCompilationUnits.addAll( methodInvocationMap.keySet() );
		allCompilationUnits.addAll( fieldDeclarationsMap.keySet() );
		return allCompilationUnits.size();
	}
}
