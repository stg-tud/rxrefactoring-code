package workers;

import static domain.SchedulerType.ANDROID_MAIN_THREAD;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import codegenerators.RxObservableStringBuilder;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import visitors.ExtCollector;
import writer.SingleUnitExtensionWriter;

/**
 * Author: Ram<br>
 * ##############
 * Modified by: Grebiel Jose Ifill Brito
 * Ram's implementation was taken and refactored
 * No logic regarding the refactoring algorithm
 * was added neither removed
 * ##############
 * Created: 01/18/2017
 */
public class ForEachWorker extends AbstractRefactorWorker<ExtCollector>
{
	private int NUMBER_OF_FOR_LOOPS = 0;

	public ForEachWorker( ExtCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<EnhancedForStatement>> forBlocksMap = collector.getForBlocks();
		int numCunits = forBlocksMap.values().size();
		monitor.beginTask( getClass().getSimpleName(), numCunits );
		RxLogger.info( this, "METHOD=refactor - Number of compilation units: " + numCunits );
		for ( ICompilationUnit icu : forBlocksMap.keySet() )
		{
			List<EnhancedForStatement> declarations = forBlocksMap.get( icu );
			for ( EnhancedForStatement forBlock : declarations )
			{
				RxLogger.info( this, "METHOD=refactor - Extract Information from ForEach: " + icu.getElementName() );
				TypeDeclaration tyDec = ASTUtil.findParent( forBlock, TypeDeclaration.class );
				AST astFor = forBlock.getAST();
				SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astFor, getClass().getSimpleName() );
				SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
				RxLogger.info( this, "METHOD=refactor - create an observable for ForEach: " + icu.getElementName() );
				createLocalObservable( singleChangeWriter, tyDec, astFor, forBlock );
				updateImports( singleChangeWriter );
				NUMBER_OF_FOR_LOOPS++;

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}

			monitor.worked( 1 );
		}
		RxLogger.info( this, "Number of ForEach =  " + NUMBER_OF_FOR_LOOPS );
		return WorkerStatus.OK;
	}

	private void createLocalObservable( SingleUnitExtensionWriter rewriter, TypeDeclaration taskObject, AST ast,
			EnhancedForStatement forBlock )
	{
		String observableStatement = createObservable( forBlock );
		Statement replacementStatement = (Statement) ASTNodeFactory.createStatementsBlockFromText( ast, observableStatement )
				.statements().get( 0 );
		rewriter.replaceStatement( forBlock, replacementStatement );

	}

	private String createObservable( EnhancedForStatement forStatement )
	{
		Expression forExpression = forStatement.getExpression();
		ASTNode doOnNextBlock = forStatement.getBody();

		String type = forStatement.getParameter().getType().toString();
		String resultVariableName = forStatement.getParameter().getName().toString();
		if ( doOnNextBlock instanceof Statement )
		{
			return RxObservableStringBuilder.newObservable( type, forExpression, ANDROID_MAIN_THREAD )
					.addDoOnNext( "{" + doOnNextBlock.toString() + "}", resultVariableName, type, true ).addSubscribe().build();
		}
		return RxObservableStringBuilder.newObservable( type, forExpression, ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock.toString(), resultVariableName, type, true ).addSubscribe().build();
	}

	private void updateImports( SingleUnitExtensionWriter rewriter )
	{
		rewriter.addImport( "rx.Observable" );
		rewriter.addImport( "rx.functions.Action1" );

	}
}