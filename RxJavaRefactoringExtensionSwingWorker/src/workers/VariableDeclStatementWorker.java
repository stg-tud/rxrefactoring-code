package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import domain.RxObservableDto;
import domain.RxSubscriberDto;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;
import utils.TemplateUtils;
import visitors.RefactoringVisitor;
import visitors.RxCollector;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class VariableDeclStatementWorker extends AbstractRefactorWorker<RxCollector>
{
	public VariableDeclStatementWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<VariableDeclarationStatement>> varDeclMap = collector.getVarDeclMap();
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<VariableDeclarationStatement>> varDelcEntry : varDeclMap.entrySet() )
		{
			ICompilationUnit icu = varDelcEntry.getKey();

			for ( VariableDeclarationStatement varDeclStatement : varDelcEntry.getValue() )
			{
				// Collect details about the SwingWorker
				RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				AST ast = varDeclStatement.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDeclStatement.fragments().get( 0 );
				Expression initializer = fragment.getInitializer();
				if ( initializer instanceof ClassInstanceCreation )
				{
					RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
					varDeclStatement.accept( refactoringVisitor );

					RxLogger.info( this, "METHOD=refactor - Copying changes to the single unit writer: " + icu.getElementName() );
					refactorVarDecl( icu, singleUnitWriter, refactoringVisitor, varDeclStatement, fragment );
				}
				else
				{
					String originalStatement = varDeclStatement.toString();
					String typeChanged = originalStatement.replace( "SwingWorker", "SWSubscriber" );
					String cleanedStatement = RefactoringUtils.cleanSwingWorkerName( typeChanged );
					Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast, cleanedStatement );

					RxLogger.info( this, "METHOD=refactor - Copying changes to the single unit writer: " + icu.getElementName() );
					singleUnitWriter.addStatementBefore( newStatement, varDeclStatement );
					singleUnitWriter.removeStatement( varDeclStatement );
				}
			}
		}

		return WorkerStatus.OK;
	}

	private void refactorVarDecl(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			VariableDeclarationStatement varDeclStatement,
			VariableDeclarationFragment fragment )
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( singleUnitWriter );

		String icuName = icu.getElementName();
		RxObservableDto observableDto = createObservableDto( icuName, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "dto", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = varDeclStatement.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		singleUnitWriter.addStatementBefore( observableStatement, varDeclStatement );

		SimpleName swingWorkerName = fragment.getName();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.toString() );
		RxSubscriberDto subscriberDto = createObserverDto( rxObserverName, refactoringVisitor, observableDto );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "dto", subscriberDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		singleUnitWriter.addStatementBefore( observerStatement, varDeclStatement );

		singleUnitWriter.removeStatement( varDeclStatement );
	}

	private void updateImports( RxSingleUnitWriter singleUnitWriter )
	{
		singleUnitWriter.addImport( "rx.Observable" );
		singleUnitWriter.addImport( "rx.Emitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWEmitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWDto" );
	}

	private RxObservableDto createObservableDto( String icuName, RefactoringVisitor refactoringVisitor )
	{
		RxObservableDto observableDto = new RxObservableDto( icuName );
		observableDto.setResultType( refactoringVisitor.getResultType().toString() );
		observableDto.setProcessType( refactoringVisitor.getProcessType().toString() );
		observableDto.setDoInBackgroundBlock( refactoringVisitor.getDoInBackgroundBlock().toString() );
		return observableDto;
	}

	private RxSubscriberDto createObserverDto( String observerName, RefactoringVisitor refactoringVisitor, RxObservableDto observableDto )
	{
		RxSubscriberDto subscriberDto = new RxSubscriberDto();
		subscriberDto.setObserverName( observerName );
		subscriberDto.setVariableDecl( true );
		Type resultType = refactoringVisitor.getResultType();
		if ( resultType != null )
		{
			subscriberDto.setResultType( resultType.toString() );
		}
		else
		{
			subscriberDto.setResultType( "Object" );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			subscriberDto.setProcessType( processType.toString() );
		}
		else
		{
			subscriberDto.setProcessType( "Object" );
		}
		subscriberDto.setObservableName( observableDto.getVarName() );
		subscriberDto.setChunksName( refactoringVisitor.getProcessVariableName() );
		Block processBlock = refactoringVisitor.getProcessBlock();
		if ( processBlock != null )
		{
			subscriberDto.setProcessBlock( processBlock.toString() );
		}
		Block doneBlock = refactoringVisitor.getDoneBlock();
		if ( doneBlock != null )
		{
			subscriberDto.setDoneBlock( doneBlock.toString() );
		}
		return subscriberDto;
	}

	private void removeSuperInvocations( RefactoringVisitor refactoringVisitor )
	{
		for ( SuperMethodInvocation methodInvocation : refactoringVisitor.getSuperMethodInvocationsToRemove() )
		{
			Statement statement = ASTUtil.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}
}
