package workers;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;

import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import visitors.ExtCollector;
import writer.SingleUnitExtensionWriter;

// TODO 7.1 - adjust the class name to something more appropriate
/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class FirstWorker extends AbstractRefactorWorker<ExtCollector>
{
	public FirstWorker( ExtCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		// TODO 7.2 - change ASTNode by the actual node type
		Map<ICompilationUnit, List<ASTNode>> exampleMap = collector.getExampleMap();

		for ( Map.Entry<ICompilationUnit, List<ASTNode>> exampleEntry : exampleMap.entrySet() )
		{
			// Get the compilation unit
			ICompilationUnit compilationUnit = exampleEntry.getKey();

			for ( ASTNode node : exampleEntry.getValue() )
			{
				// Get writer instance
				AST ast = node.getAST();
				SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( compilationUnit, ast, getClass().getSimpleName() );
				SingleUnitExtensionWriter writer = RxSingleUnitWriterMapHolder.getSingleUnitWriter( compilationUnit, writerTemplate );

				// TODO 7.3 - Implement the logic to modify the node using the writer here!!
                // Example: writer.replaceNode(node, anotherNode); If you need more methods to apply
                // changes to the ASTNode then add them in SingleUnitExtensionWriter

				// TODO 7.4 - do not forget adding the compilation unit to the rxMultipleUnitsWriter
				rxMultipleUnitsWriter.addCompilationUnit( compilationUnit );
			}
		}
		return null;
	}
}
