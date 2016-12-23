package workers;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import codegenerators.ComplexRxObservableBuilder;
import codegenerators.RxObservableStringBuilder;
import codegenerators.RxSubscriberHolder;
import domain.SchedulerType;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.codegenerators.DynamicIdsMapHolder;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import visitors.Collector;
import visitors.RefactoringVisitor;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/17/2016
 */
public class AnonymClassWorker extends AbstractRefactorWorker<Collector>
{
	private static final String EMPTY = "";

	public AnonymClassWorker( Collector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<AnonymousClassDeclaration>> cuAnonymousClassesMap = collector.getCuAnonymousClassDeclMap();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numCunits );
		RxLogger.info( this, "METHOD=refactor - Number of compilation units: " + numCunits );
		for ( ICompilationUnit icu : cuAnonymousClassesMap.keySet() )
		{
			List<AnonymousClassDeclaration> declarations = cuAnonymousClassesMap.get( icu );
			for ( AnonymousClassDeclaration swingWorkerDeclaration : declarations )
			{
				// Collect details about the SwingWorker
				RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				AST ast = swingWorkerDeclaration.getAST();
				RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
				swingWorkerDeclaration.accept(refactoringVisitor);

				RxSingleUnitWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				// Create rx.Observable using the Subscriber if necessary
				RxLogger.info( this, "METHOD=refactor - Creating rx.Observable object: " + icu.getElementName() );
				Statement referenceStatement = ASTUtil.findParent( swingWorkerDeclaration, Statement.class );
				addRxObservable( icu, singleChangeWriter, referenceStatement, refactoringVisitor, swingWorkerDeclaration );

				// remove existing SwingWorker
				singleChangeWriter.removeStatement( swingWorkerDeclaration );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );

			}
			monitor.worked( 1 );
		}
		return WorkerStatus.OK;
	}

	// ### Private Methods ###

	private void addRxObservable(
			ICompilationUnit icu,
			RxSingleUnitWriter rewriter,
			Statement referenceStatement,
			RefactoringVisitor refactoringVisitor,
			AnonymousClassDeclaration swingWorkerDeclaration )
	{
		boolean complexRxObservableClassNeeded = refactoringVisitor.hasAdditionalFieldsOrMethods();
		boolean processBlockExists = refactoringVisitor.getProcessBlock() != null;
		removeSuperInvocations(refactoringVisitor);

		RxSubscriberHolder subscriberHolder = null;
		if ( processBlockExists )
		{
			subscriberHolder = new RxSubscriberHolder(
					icu.getElementName(),
					refactoringVisitor.getProgressUpdateTypeName(),
					refactoringVisitor.getProcessBlock(),
					refactoringVisitor.getProgressUpdateVariableName() );
		}

		AST ast = referenceStatement.getAST();
		if ( !complexRxObservableClassNeeded )
		{
			String subscribedObservable = createObservable(refactoringVisitor, subscriberHolder )
					.addSubscribe()
					.build();
			if ( processBlockExists )
			{
				String newMethodString = subscriberHolder.getGetMethodDeclaration();
				MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText( ast, newMethodString );
				rewriter.addMethodAfter( newMethod, swingWorkerDeclaration );

				String subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				Statement getSubscriberStatement = ASTNodeFactory.createSingleStatementFromText( ast, subscriberDecl );
				rewriter.addStatementBefore( getSubscriberStatement, referenceStatement );
			}
			Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast, subscribedObservable );
			rewriter.addStatementBefore( newStatement, referenceStatement );
		}
		else
		{
			List<FieldDeclaration> fieldDeclarations = refactoringVisitor.getFieldDeclarations();
			String subscriberDecl = EMPTY;
			String subscriberGetRxUpdateMethod = EMPTY;
			if ( processBlockExists )
			{
				subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				subscriberGetRxUpdateMethod = subscriberHolder.getGetMethodDeclaration();
			}

			String observableStatement = createObservable(refactoringVisitor, subscriberHolder )
					.buildReturnStatement();
			String observableType = refactoringVisitor.getResultType().toString();

			String complexRxObservableClass = ComplexRxObservableBuilder.newComplexRxObservable( icu.getElementName() )
					.withFields( fieldDeclarations )
					.withGetAsyncObservable( observableType, subscriberDecl, observableStatement )
					.withMethod( subscriberGetRxUpdateMethod )
					.withMethods( refactoringVisitor.getAdditionalMethodDeclarations() ).build();

			TypeDeclaration complexRxObservableDecl = ASTNodeFactory.createTypeDeclarationFromText( ast, complexRxObservableClass );
			rewriter.addInnerClassAfter( complexRxObservableDecl, referenceStatement );

			String lastComplexObsId = DynamicIdsMapHolder.getLastObservableId( icu.getElementName() );
			String newStatementString = new StringBuilder().append( "new ComplexRxObservable()" )
					.append( lastComplexObsId )
					.append( ".getAsyncObservable()" )
					.append( lastComplexObsId )
					.append( ".subscribe();" ).toString();

			Statement newSatement = ASTNodeFactory.createSingleStatementFromText( ast, newStatementString );
			rewriter.addStatementBefore( newSatement, referenceStatement );

		}

		updateImports( rewriter, refactoringVisitor);
	}

	private void updateImports( RxSingleUnitWriter rewriter, RefactoringVisitor refactoringVisitor)
	{
		rewriter.addImport( "rx.Observable" );
		rewriter.addImport( "rx.schedulers.Schedulers" );
		rewriter.addImport( "java.util.concurrent.Callable" );
		rewriter.removeImport( "javax.swing.SwingWorker" );
		if ( refactoringVisitor.getDoneBlock() != null )
		{
			rewriter.addImport( "rx.functions.Action1" );
			if ( !refactoringVisitor.getTimeoutArguments().isEmpty() )
			{
				rewriter.addImport( "rx.functions.Func1" );
				rewriter.addImport( "java.util.concurrent.TimeUnit" );
			}
			rewriter.removeImport( "java.util.concurrent.ExecutionException" );
			rewriter.removeImport( "java.util.concurrent.TimeoutException" );
		}
		if ( refactoringVisitor.getProcessBlock() != null )
		{
			rewriter.addImport( "rx.Subscriber" );
			rewriter.addImport( "java.util.Arrays" );
		}
	}

	private RxObservableStringBuilder createObservable(RefactoringVisitor refactoringVisitor, RxSubscriberHolder subscriberHolder )
	{
		Block doInBackgroundBlock = refactoringVisitor.getDoInBackgroundBlock();
		Block doOnCompletedBlock = refactoringVisitor.getDoneBlock();
		String type = refactoringVisitor.getResultType().toString();
		String resultVariableName = refactoringVisitor.getResultVariableName();
		List<String> timeOutArguments = refactoringVisitor.getTimeoutArguments();
		Block timeoutCatchBlock = refactoringVisitor.getTimeoutCatchBlock();

		// replaces publish(x, y, ...) by rxUpdateSubscriber.onNext(Arrays.asList(x, y, ...))
		replacePublishInvocations(refactoringVisitor, subscriberHolder );

		// changes all get() / get(long, TimeUnit) invocation by a variable name
		removeGetInvocations(refactoringVisitor);

		// get() and get(long, TimeUnit) throw exceptions.
		// Since they were just replaced by a variable name, the catch clauses
		// must be removed
		ASTUtil.removeUnnecessaryCatchClauses( doOnCompletedBlock );

		return RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD )
				.addDoOnNext( doOnCompletedBlock, resultVariableName )
				.addTimeout( timeOutArguments, timeoutCatchBlock );
	}

	private void replacePublishInvocations(RefactoringVisitor refactoringVisitor, RxSubscriberHolder subscriberHolder )
	{
		if ( !refactoringVisitor.getMethodInvocationsPublish().isEmpty() || !refactoringVisitor.getSuperMethodInvocationsPublish().isEmpty() )
		{
			for ( MethodInvocation publishInvocation : refactoringVisitor.getMethodInvocationsPublish() )
			{
				List argumentList = publishInvocation.arguments();
				AST ast = publishInvocation.getAST();
				replacePublishInvocationsAux( subscriberHolder, publishInvocation, argumentList, ast );
			}
			for ( SuperMethodInvocation publishInvocation : refactoringVisitor.getSuperMethodInvocationsPublish() )
			{
				List argumentList = publishInvocation.arguments();
				AST ast = publishInvocation.getAST();
				replacePublishInvocationsAux( subscriberHolder, publishInvocation, argumentList, ast );
			}
		}
	}

	private <T extends ASTNode> void replacePublishInvocationsAux( RxSubscriberHolder subscriberHolder, T publishInvocation, List argumentList, AST ast )
	{
		String newInvocation = subscriberHolder.getOnNextInvocation( argumentList );
		Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast, newInvocation );
		ASTUtil.replaceInStatement( publishInvocation, newStatement );
	}

	private void removeGetInvocations( RefactoringVisitor refactoringVisitor)
	{
		if ( refactoringVisitor.getDoneBlock() != null )
		{
			for ( MethodInvocation methodInvocation : refactoringVisitor.getMethodInvocationsGet() )
			{
				replaceGetInvocation(refactoringVisitor, methodInvocation );
			}
			for ( SuperMethodInvocation methodInvocation : refactoringVisitor.getSuperMethodInvocationsGet() )
			{
				replaceGetInvocation(refactoringVisitor, methodInvocation );
			}
		}
	}

	private <T extends ASTNode> void replaceGetInvocation(RefactoringVisitor refactoringVisitor, T methodInvocation )
	{
		String resultVariableName = refactoringVisitor.getResultVariableName();
		SimpleName variableName = methodInvocation.getAST().newSimpleName( resultVariableName );
		ASTUtil.replaceInStatement( methodInvocation, variableName );
	}

	private void removeSuperInvocations( RefactoringVisitor refactoringVisitor)
	{
		for ( SuperMethodInvocation methodInvocation : refactoringVisitor.getSuperMethodInvocationsToRemove() )
		{
			Statement statement = ASTUtil.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}
}
