package visitors;

import java.util.*;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.refactoring.AbstractCollector;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class RxCollector extends AbstractCollector
{
	private final Map<ICompilationUnit, List<TypeDeclaration>> typeDeclMap;
	private final Map<ICompilationUnit, List<FieldDeclaration>> fieldDeclMap;
	private final Map<ICompilationUnit, List<Assignment>> assigmentsMap;
	private final Map<ICompilationUnit, List<VariableDeclarationStatement>> varDeclMap;
	private final Map<ICompilationUnit, List<SimpleName>> simpleNamesMap;
	private final Map<ICompilationUnit, List<ClassInstanceCreation>> classInstanceMap;
	private final Map<ICompilationUnit, List<SingleVariableDeclaration>> singleVarDeclMap;
	private final Map<ICompilationUnit, List<MethodInvocation>> methodInvocationsMap;

	public RxCollector( String collectorName )
	{
		super( collectorName );
		typeDeclMap = new HashMap<>();
		fieldDeclMap = new HashMap<>();
		assigmentsMap = new HashMap<>();
		varDeclMap = new HashMap<>();
		simpleNamesMap = new HashMap<>();
		classInstanceMap = new HashMap<>();
		singleVarDeclMap = new HashMap<>();
		methodInvocationsMap = new HashMap<>();
	}

	public void add( ICompilationUnit cu, List subclasses )
	{
		if ( subclasses.isEmpty() )
		{
			return;
		}

		if ( subclasses.get( 0 ) instanceof TypeDeclaration )
		{
			addToMap( cu, subclasses, typeDeclMap );
		}
		else if ( subclasses.get( 0 ) instanceof FieldDeclaration )
		{
			addToMap( cu, subclasses, fieldDeclMap );
		}
		else if ( subclasses.get( 0 ) instanceof Assignment )
		{
			addToMap( cu, subclasses, assigmentsMap );
		}
		else if ( subclasses.get( 0 ) instanceof VariableDeclarationStatement )
		{
			addToMap( cu, subclasses, varDeclMap );
		}
		else if ( subclasses.get( 0 ) instanceof SimpleName )
		{
			addToMap( cu, subclasses, simpleNamesMap );
		}
		else if ( subclasses.get( 0 ) instanceof ClassInstanceCreation )
		{
			addToMap( cu, subclasses, classInstanceMap );
		}
		else if ( subclasses.get( 0 ) instanceof SingleVariableDeclaration )
		{
			addToMap( cu, subclasses, singleVarDeclMap );
		}
		else if ( subclasses.get( 0 ) instanceof MethodInvocation )
		{
			addToMap( cu, subclasses, methodInvocationsMap );
		}

	}

	public Map<ICompilationUnit, List<TypeDeclaration>> getTypeDeclMap()
	{
		return typeDeclMap;
	}

	public Map<ICompilationUnit, List<FieldDeclaration>> getFieldDeclMap()
	{
		return fieldDeclMap;
	}

	public Map<ICompilationUnit, List<Assignment>> getAssigmentsMap()
	{
		return assigmentsMap;
	}

	public Map<ICompilationUnit, List<VariableDeclarationStatement>> getVarDeclMap()
	{
		return varDeclMap;
	}

	public Map<ICompilationUnit, List<SimpleName>> getSimpleNamesMap()
	{
		return simpleNamesMap;
	}

	public Map<ICompilationUnit, List<ClassInstanceCreation>> getClassInstanceMap()
	{
		return classInstanceMap;
	}

	public Map<ICompilationUnit, List<SingleVariableDeclaration>> getSingleVarDeclMap()
	{
		return singleVarDeclMap;
	}

	public Map<ICompilationUnit, List<MethodInvocation>> getMethodInvocationsMap()
	{
		return methodInvocationsMap;
	}

	public int getNumberOfCompilationUnits()
	{
		Set<ICompilationUnit> allCompilationUnits = new HashSet<>();
		allCompilationUnits.addAll( typeDeclMap.keySet() );
		allCompilationUnits.addAll( fieldDeclMap.keySet() );
		allCompilationUnits.addAll( assigmentsMap.keySet() );
		allCompilationUnits.addAll( varDeclMap.keySet() );
		allCompilationUnits.addAll( simpleNamesMap.keySet() );
		allCompilationUnits.addAll( classInstanceMap.keySet() );
		allCompilationUnits.addAll( singleVarDeclMap.keySet() );
		return allCompilationUnits.size();
	}
}
