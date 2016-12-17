package rxjavarefactoring.analyzers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: Collects usages information for a target class<br>
 * This class contains code from the tool
 * <a href="http://refactoring.info/tools/asyncdroid/">AsyncDroid</a>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class UsagesVisitor extends ASTVisitor
{
	private final Map<String, String> publicMethodsMap;
	private final String classBinaryName;
	private final List<MethodInvocation> usages;

	public UsagesVisitor( Map<String, String> publicMethodsMap, String classBinaryName )
	{
		this.publicMethodsMap = publicMethodsMap;
		this.classBinaryName = classBinaryName;
		usages = new ArrayList<>();
	}

	@Override
	public boolean visit( MethodInvocation node )
	{
		IMethodBinding binding = node.resolveMethodBinding();
		if ( binding == null )
		{
			return true;
		}

		ITypeBinding declaringClass = binding.getDeclaringClass();
		if ( declaringClass == null )
		{
			return true;
		}

		String methodName = binding.getName();

		Set<String> publicMethods = publicMethodsMap.keySet();
		boolean targetClassFound = ASTUtil.isTypeOf( declaringClass, classBinaryName );
		boolean targetMethodFound = publicMethods.contains( methodName );
		if ( targetClassFound && targetMethodFound )
		{
			usages.add( node );
		}

		return true;
	}

	public List<MethodInvocation> getUsages()
	{
		return usages;
	}

	public boolean isUsagesFound()
	{
		return !usages.isEmpty();
	}
}
