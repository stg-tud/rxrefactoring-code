package writer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;

import rxjavarefactoring.framework.writers.RxSingleUnitWriter;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/30/2016
 */
public class RxSwingWorkerWriter extends RxSingleUnitWriter
{
	public RxSwingWorkerWriter( ICompilationUnit icu, AST ast, String refactoringDescription )
	{
		super( icu, ast, refactoringDescription );
	}
}
