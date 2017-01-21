package writer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;

/**
 * Description: Single Unit Writer to be used for this extension<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class SingleUnitExtensionWriter  extends RxSingleUnitWriter
{
    public SingleUnitExtensionWriter(ICompilationUnit icu, AST ast, String refactoringDescription )
    {
        super( icu, ast, refactoringDescription );
    }
}