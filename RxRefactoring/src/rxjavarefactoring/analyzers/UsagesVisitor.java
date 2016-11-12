package rxjavarefactoring.analyzers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import rxjavarefactoring.domain.ClassDetails;
import rxjavarefactoring.utils.ASTUtil;

/**
 * Description: Collects usages information for a target class<br>
 * This class contains code from the tool
 * <a href="http://refactoring.info/tools/asyncdroid/">AsyncDroid</a>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class UsagesVisitor extends ASTVisitor
{
	private ClassDetails targetClass;
	private List<MethodInvocation> usages;

	public UsagesVisitor( ClassDetails targetClass )
	{
		this.targetClass = targetClass;
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

		Set<String> publicMethods = targetClass.getPublicMethodsMap().keySet();
		boolean targetClassFound = ASTUtil.isTypeOf( declaringClass, this.targetClass.getBinaryName() );
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
