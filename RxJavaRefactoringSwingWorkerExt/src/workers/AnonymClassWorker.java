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
import rxjavarefactoring.processor.ASTNodesCollector;
import rxjavarefactoring.processor.WorkerStatus;
import visitors.SwingWorkerVisitor;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/17/2016
 */
public class AnonymClassWorker extends AbstractRefactorWorker<ASTNodesCollector>
{
	private static final String EMPTY = "";

	public AnonymClassWorker( ASTNodesCollector collector )
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
				SwingWorkerVisitor swingWorkerVisitor = new SwingWorkerVisitor();
				swingWorkerDeclaration.accept( swingWorkerVisitor );

				// TODO: do not create the compilation unit here. Create a Map instead!
				RxSingleUnitWriter singleChangeWriter = new RxSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				// Create rx.Observable using the Subscriber if necessary
				RxLogger.info( this, "METHOD=refactor - Creating rx.Observable object: " + icu.getElementName() );
				Statement referenceStatement = ASTUtil.findParent( swingWorkerDeclaration, Statement.class );
				addRxObservable( icu, singleChangeWriter, referenceStatement, swingWorkerVisitor, swingWorkerDeclaration );

				// remove existing SwingWorker
				singleChangeWriter.removeStatement( swingWorkerDeclaration );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				rxMultipleUnitsWriter.addChange( icu, singleChangeWriter );

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
			SwingWorkerVisitor swingWorkerVisitor,
			AnonymousClassDeclaration swingWorkerDeclaration )
	{
		boolean complexRxObservableClassNeeded = swingWorkerVisitor.hasAdditionalFieldsOrMethods();
		boolean processBlockExists = swingWorkerVisitor.getProcessBlock() != null;
		removeSuperInvocations( swingWorkerVisitor );

		RxSubscriberHolder subscriberHolder = null;
		if ( processBlockExists )
		{
			subscriberHolder = new RxSubscriberHolder(
					icu.getElementName(),
					swingWorkerVisitor.getProgressUpdateTypeName(),
					swingWorkerVisitor.getProcessBlock(),
					swingWorkerVisitor.getProgressUpdateVariableName() );
		}

		AST ast = referenceStatement.getAST();
		if ( !complexRxObservableClassNeeded )
		{
			String subscribedObservable = createObservable( swingWorkerVisitor, subscriberHolder )
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
			List<FieldDeclaration> fieldDeclarations = swingWorkerVisitor.getFieldDeclarations();
			String subscriberDecl = EMPTY;
			String subscriberGetRxUpdateMethod = EMPTY;
			if ( processBlockExists )
			{
				subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				subscriberGetRxUpdateMethod = subscriberHolder.getGetMethodDeclaration();
			}

			String observableStatement = createObservable( swingWorkerVisitor, subscriberHolder )
					.buildReturnStatement();
			String observableType = swingWorkerVisitor.getResultType().toString();

			String complexRxObservableClass = ComplexRxObservableBuilder.newComplexRxObservable( icu.getElementName() )
					.withFields( fieldDeclarations )
					.withGetAsyncObservable( observableType, subscriberDecl, observableStatement )
					.withMethod( subscriberGetRxUpdateMethod )
					.withMethods( swingWorkerVisitor.getAdditionalMethodDeclarations() ).build();

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

		updateImports( rewriter, swingWorkerVisitor );
	}

	private void updateImports( RxSingleUnitWriter rewriter, SwingWorkerVisitor swingWorkerVisitor )
	{
		rewriter.addImport( "rx.Observable" );
		rewriter.addImport( "rx.schedulers.Schedulers" );
		rewriter.addImport( "java.util.concurrent.Callable" );
		rewriter.removeImport( "javax.swing.SwingWorker" );
		if ( swingWorkerVisitor.getDoneBlock() != null )
		{
			rewriter.addImport( "rx.functions.Action1" );
			if ( !swingWorkerVisitor.getTimeoutArguments().isEmpty() )
			{
				rewriter.addImport( "rx.functions.Func1" );
				rewriter.addImport( "java.util.concurrent.TimeUnit" );
			}
			rewriter.removeImport( "java.util.concurrent.ExecutionException" );
			rewriter.removeImport( "java.util.concurrent.TimeoutException" );
		}
		if ( swingWorkerVisitor.getProcessBlock() != null )
		{
			rewriter.addImport( "rx.Subscriber" );
			rewriter.addImport( "java.util.Arrays" );
		}
	}

	private RxObservableStringBuilder createObservable( SwingWorkerVisitor swingWorkerVisitor, RxSubscriberHolder subscriberHolder )
	{
		Block doInBackgroundBlock = swingWorkerVisitor.getDoInBackgroundBlock();
		Block doOnCompletedBlock = swingWorkerVisitor.getDoneBlock();
		String type = swingWorkerVisitor.getResultType().toString();
		String resultVariableName = swingWorkerVisitor.getResultVariableName();
		List<String> timeOutArguments = swingWorkerVisitor.getTimeoutArguments();
		Block timeoutCatchBlock = swingWorkerVisitor.getTimeoutCatchBlock();

		// replaces publish(x, y, ...) by rxUpdateSubscriber.onNext(Arrays.asList(x, y, ...))
		replacePublishInvocations( swingWorkerVisitor, subscriberHolder );

		// changes all get() / get(long, TimeUnit) invocation by a variable name
		removeGetInvocations( swingWorkerVisitor );

		// get() and get(long, TimeUnit) throw exceptions.
		// Since they were just replaced by a variable name, the catch clauses
		// must be removed
		ASTUtil.removeUnnecessaryCatchClauses( doOnCompletedBlock );

		return RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD )
				.addDoOnNext( doOnCompletedBlock, resultVariableName )
				.addTimeout( timeOutArguments, timeoutCatchBlock );
	}

	private void replacePublishInvocations( SwingWorkerVisitor swingWorkerVisitor, RxSubscriberHolder subscriberHolder )
	{
		if ( !swingWorkerVisitor.getMethodInvocationsPublish().isEmpty() || !swingWorkerVisitor.getSuperMethodInvocationsPublish().isEmpty() )
		{
			for ( MethodInvocation publishInvocation : swingWorkerVisitor.getMethodInvocationsPublish() )
			{
				List argumentList = publishInvocation.arguments();
				AST ast = publishInvocation.getAST();
				replacePublishInvocationsAux( subscriberHolder, publishInvocation, argumentList, ast );
			}
			for ( SuperMethodInvocation publishInvocation : swingWorkerVisitor.getSuperMethodInvocationsPublish() )
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

	private void removeGetInvocations( SwingWorkerVisitor swingWorkerVisitor )
	{
		if ( swingWorkerVisitor.getDoneBlock() != null )
		{
			for ( MethodInvocation methodInvocation : swingWorkerVisitor.getMethodInvocationsGet() )
			{
				replaceGetInvocation( swingWorkerVisitor, methodInvocation );
			}
			for ( SuperMethodInvocation methodInvocation : swingWorkerVisitor.getSuperMethodInvocationsGet() )
			{
				replaceGetInvocation( swingWorkerVisitor, methodInvocation );
			}
		}
	}

	private <T extends ASTNode> void replaceGetInvocation( SwingWorkerVisitor swingWorkerVisitor, T methodInvocation )
	{
		String resultVariableName = swingWorkerVisitor.getResultVariableName();
		SimpleName variableName = methodInvocation.getAST().newSimpleName( resultVariableName );
		ASTUtil.replaceInStatement( methodInvocation, variableName );
	}

	private void removeSuperInvocations( SwingWorkerVisitor swingWorkerVisitor )
	{
		for ( SuperMethodInvocation methodInvocation : swingWorkerVisitor.getSuperMethodInvocationsToRemove() )
		{
			Statement statement = ASTUtil.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}
}
