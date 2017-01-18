package workers;

import java.util.List;
import java.util.Map;

import builders.ComplexRxObservableBuilder;
import builders.RxObservableStringBuilder;
import builders.RxSubscriberHolder;
import domain.SchedulerType;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import utils.AsyncTaskAstUtils;
import visitors.AsyncTaskVisitor;
import visitors.CuCollector;
import writer.SingleUnitExtensionWriter;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AnonymAsyncTaskWorker extends AbstractRefactorWorker<CuCollector>
{
	private static final String EMPTY = "";
	private int NUMBER_OF_ANONYMOUS_TASKS = 0;

	private final String EXECUTE = "execute";
	private final String CANCEL = "cancel";
	private final String SUBSCRIBE = "subscribe";
	private final String UN_SUBSCRIBE = "unsubscribe";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	private String complexObservableclassName = "complexObservableclassName";
	private String subscription = "subscription";
	private String asyncMethodName = "asyncMethodName";

	public AnonymAsyncTaskWorker( CuCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{

		Map<ICompilationUnit, List<AnonymousClassDeclaration>> cuAnonymousClassesMap = collector.getCuAnonymousClassesMap();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numCunits );
		RxLogger.info( this, "METHOD=refactor - Number of compilation units: " + numCunits );
		for ( ICompilationUnit icu : cuAnonymousClassesMap.keySet() )
		{
			List<AnonymousClassDeclaration> declarations = cuAnonymousClassesMap.get( icu );
			
			for ( AnonymousClassDeclaration asyncTaskDeclaration : declarations )
			{
				RxLogger.info( this, "METHOD=refactor - Extract Information from AsyncTask: " + icu.getElementName() );
				AsyncTaskVisitor asyncTaskVisitor = new AsyncTaskVisitor();
				asyncTaskDeclaration.accept( asyncTaskVisitor );
				if ( asyncTaskVisitor.getDoInBackgroundBlock() == null )
					continue;
				AST ast = asyncTaskDeclaration.getAST();
				SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, ast, getClass().getSimpleName() );
				SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
				RxLogger.info( this, "METHOD=refactor - Updating imports: " + icu.getElementName() );
				// updateImports(singleChangeWriter);
				Statement referenceStatement = ASTUtil.findParent( asyncTaskDeclaration, Statement.class );
				addRxObservable( icu, singleChangeWriter, referenceStatement, asyncTaskVisitor, asyncTaskDeclaration );
				// singleChangeWriter.removeStatement(asyncTaskDeclaration);
				if ( asyncTaskVisitor.hasField() )
				{
					if ( !checkForimplecitExecute( collector.getCuRelevantUsagesMap(), ast, singleChangeWriter,
							asyncTaskDeclaration, icu ) )
						singleChangeWriter.removeStatement( asyncTaskDeclaration );
					updateUsage( collector.getCuRelevantUsagesMap(), ast, singleChangeWriter, asyncTaskDeclaration, icu );
				}
				updateImports( singleChangeWriter, asyncTaskVisitor );
				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				// rxMultipleChangeWriter.addChange(icu, singleChangeWriter);
				NUMBER_OF_ANONYMOUS_TASKS++;

				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}

			monitor.worked( 1 );
		}
		RxLogger.info( this, "Number of Anonymous AsynckTasks =  " + NUMBER_OF_ANONYMOUS_TASKS );
		return WorkerStatus.OK;
	}

	/**
	 * This method checks if the instance creation statement has execute method
	 * implicit Ex: AsyncTask<Void,Void,Void> task= new AsyncTask().execute();
	 *
	 * In such cases this method reference is added to relevance usage list
	 *
	 * @param cuRelevantUsagesMap
	 * @param ast
	 * @param singleChangeWriter
	 * @param asyncTaskDeclaration
	 * @param icu
	 */
	private boolean checkForimplecitExecute( Map<ICompilationUnit, List<MethodInvocation>> cuRelevantUsagesMap, AST ast,
			SingleUnitExtensionWriter singleChangeWriter, AnonymousClassDeclaration asyncTaskDeclaration,
			ICompilationUnit icu )
	{
		String variableName = "getObservable";
		MethodInvocation implicitExecute = null;
		ASTNode execute = AsyncTaskAstUtils.findOuterParent( asyncTaskDeclaration, MethodInvocation.class );
		if ( execute != null )
			implicitExecute = (MethodInvocation) execute;
		if ( implicitExecute != null )
			if ( implicitExecute.getName().toString().equals( EXECUTE )
					|| implicitExecute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
			{
				VariableDeclarationFragment var = (VariableDeclarationFragment) ASTUtil.findParent( implicitExecute,
						VariableDeclarationFragment.class );
				// if (var != null) {
				// variableName = var.getName().toString();
				// } else {
				// variableName = ((Assignment)
				// ASTUtil.findParent(implicitExecute, Assignment.class))
				// .getLeftHandSide().toString();
				// }
				MethodInvocation me = ast.newMethodInvocation();
				me.setName( ast.newSimpleName( implicitExecute.getName().toString() ) );
				me.setExpression( ast.newSimpleName( variableName ) );
				Statement s = (Statement) ASTUtil.findParent( asyncTaskDeclaration, Statement.class );
				replaceExecuteImplicit( me, icu, ast, false, s );
				return true;
			}
		return false;
	}

	private void updateUsage( Map<ICompilationUnit, List<MethodInvocation>> cuRelevantUsagesMap, AST ast,
			SingleUnitExtensionWriter SingleUnitExtensionWriter, AnonymousClassDeclaration asyncTaskDeclaration,
			ICompilationUnit icuOuter )
	{
		for ( ICompilationUnit icu : cuRelevantUsagesMap.keySet() )
		{
			TypeDeclaration tyDec = ast.newTypeDeclaration();
			SingleUnitExtensionWriter singleChangeWriterNew = null;
			AST astInvoke = ast;
			RxLogger.info( this, "METHOD=updateUsage - updating usage for class: " + icuOuter.getElementName() + " in "
					+ icu.getElementName() );
			for ( MethodInvocation methodInvoke : cuRelevantUsagesMap.get( icu ) )
			{
				if ( methodInvoke.getName().toString().equals( EXECUTE )
						|| methodInvoke.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
				{

					if ( icuOuter.getElementName().equals( icu.getElementName() ) )
						replaceCancel( cuRelevantUsagesMap, methodInvoke, icuOuter, ast );
					else
					{
						// String newName = ((TypeDeclaration)
						// ASTUtil.findParent(methodInvoke,
						// TypeDeclaration.class))
						// .getName().toString();
						// tyDec = (TypeDeclaration)
						// ASTUtil.findParent(methodInvoke,
						// TypeDeclaration.class);
						// astInvoke = tyDec.getAST();
						// if
						// (!SingleUnitExtensionWriterMap.keySet().contains(newName))
						// {
						// singleChangeWriterNew = new SingleUnitExtensionWriter(icu,
						// astInvoke,
						// getClass().getSimpleName());
						// SingleUnitExtensionWriterMap.put(newName, new
						// ComplexMap(singleChangeWriterNew, icu));
						// }
						// replaceCancel(cuRelevantUsagesMap, methodInvoke, icu,
						// astInvoke);
					}

				}
			}

		}

	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 *
	 * @param cuRelevantUsagesMap
	 * @param methodInvoke
	 * @param icu
	 * @param astInvoke
	 */
	void replaceCancel( Map<ICompilationUnit, List<MethodInvocation>> cuRelevantUsagesMap, MethodInvocation methodInvoke,
			ICompilationUnit icu, AST astInvoke )
	{
		boolean isCancelPresent = false;
		for ( MethodInvocation methodReference : cuRelevantUsagesMap.get( icu ) )
		{
			try
			{
				if ( methodInvoke.getExpression().toString().equals( methodReference.getExpression().toString() ) )
				{
					if ( methodReference.getName().toString().equals( CANCEL ) )
					{
						isCancelPresent = true;
						RxLogger.info( this,
								"METHOD=replaceCancel - updating cancel invocation for class: " + icu.getElementName());
						replaceExecute( methodInvoke, icu, astInvoke, true );
						updateCancelInvocation( icu, methodReference, astInvoke );

					}
				}
			}
			catch ( Exception e )
			{

			}
		}
		if ( !isCancelPresent )
		{

			replaceExecute( methodInvoke, icu, astInvoke, false );
		}
	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
	private void updateCancelInvocation( ICompilationUnit icu, MethodInvocation methodInvoke, AST astInvoke )
	{

		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
		RxLogger.info( this,
				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + icu.getElementName());
		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression( astInvoke.newSimpleName( getVariableName( methodInvoke.getExpression() ) + subscription ) );
		unSubscribe.setName( astInvoke.newSimpleName( UN_SUBSCRIBE ) );
		singleChangeWriter.replace( methodInvoke, unSubscribe );

	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute( MethodInvocation methodInvoke, ICompilationUnit icu, AST astInvoke, boolean withSubscription )
	{
		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
		astInvoke = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( astInvoke, methodInvoke );
		if ( execute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
		{
			execute.arguments().clear();
		}
		execute.setExpression( AsyncTaskAstUtils.getinstannceCreationStatement( astInvoke, complexObservableclassName ) );
		execute.setName( astInvoke.newSimpleName( asyncMethodName ) );

		MethodInvocation invocation = astInvoke.newMethodInvocation();

		invocation.setExpression( execute );
		invocation.setName( astInvoke.newSimpleName( SUBSCRIBE ) );
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		if ( !withSubscription )
		{
			singleChangeWriter.replace( methodInvoke, invocation );
		}
		else
		{
			createSubscriptionDeclaration( icu, methodInvoke );
			Assignment initSubscription = astInvoke.newAssignment();

			initSubscription.setLeftHandSide(
					astInvoke.newSimpleName( getVariableName( methodInvoke.getExpression() ) + subscription ) );
			initSubscription.setRightHandSide( invocation );
			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
			singleChangeWriter.replace( methodInvoke, initSubscription );
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteImplicit( MethodInvocation methodInvoke, ICompilationUnit icu, AST astInvoke,
			boolean withSubscription, Statement referenceStatement )
	{
		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
		astInvoke = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( astInvoke, methodInvoke );
		if ( execute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
		{
			execute.arguments().clear();
		}
		execute.setExpression( AsyncTaskAstUtils.getinstannceCreationStatement( astInvoke, complexObservableclassName ) );
		execute.setName( astInvoke.newSimpleName( asyncMethodName ) );

		MethodInvocation invocation = astInvoke.newMethodInvocation();

		invocation.setExpression( execute );
		invocation.setName( astInvoke.newSimpleName( SUBSCRIBE ) );
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		Statement ss = ASTNodeFactory.createSingleStatementFromText( astInvoke, invocation.toString() );
		if ( !withSubscription )
		{
			singleChangeWriter.replace( referenceStatement, ss );
		}
		else
		{
			createSubscriptionDeclaration( icu, methodInvoke );
			Assignment initSubscription = astInvoke.newAssignment();

			initSubscription.setLeftHandSide(
					astInvoke.newSimpleName( getVariableName( methodInvoke.getExpression() ) + subscription ) );
			initSubscription.setRightHandSide( invocation );
			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
			singleChangeWriter.replace( methodInvoke, initSubscription );
		}
	}

	private String getVariableName( Expression e )
	{
		if ( e instanceof FieldAccess )
			return ( (FieldAccess) e ).getName().toString();
		else
			return e.toString();
	}

	void createSubscriptionDeclaration( ICompilationUnit icu, MethodInvocation methodInvoke )
	{
		TypeDeclaration tyDec = ASTUtil.findParent( methodInvoke, TypeDeclaration.class );
		AST astInvoke = tyDec.getAST();

		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );

		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		variable.setName( astInvoke.newSimpleName( methodInvoke.getExpression().toString() + subscription ) );
		FieldDeclaration subscription = astInvoke.newFieldDeclaration( variable );
		subscription.setType( astInvoke.newSimpleType( astInvoke.newSimpleName( "Subscription" ) ) );
		singleChangeWriter.addStatementToClass( subscription, tyDec );
		singleChangeWriter.addImport( "rx.Subscription" );
	}

	private void updateImports( SingleUnitExtensionWriter rewriter, AsyncTaskVisitor asyncTaskVisitor )
	{
		rewriter.addImport( "rx.Observable" );
		rewriter.addImport( "rx.schedulers.Schedulers" );
		rewriter.addImport( "java.util.concurrent.Callable" );
		rewriter.removeImport( "android.os.AsyncTask" );
		if ( asyncTaskVisitor.getOnPostExecuteBlock() != null )
		{
			rewriter.addImport( "rx.functions.Action1" );
			rewriter.removeImport( "java.util.concurrent.ExecutionException" );
			rewriter.removeImport( "java.util.concurrent.TimeoutException" );

		}
		if ( asyncTaskVisitor.getDoInBackgroundBlock() != null )
		{
			rewriter.addImport( "rx.functions.Action1" );
		}
		if ( asyncTaskVisitor.getOnProgressUpdateBlock() != null )
		{
			rewriter.addImport( "rx.Subscriber" );
			rewriter.addImport( "java.util.Arrays" );
		}
		if ( asyncTaskVisitor.getOnCancelled() != null )
		{
			rewriter.addImport( "rx.functions.Action0" );
		}
	}

	/**
	 * Remove super method invocation from AsyncTask methods
	 *
	 * @param asyncTaskVisitor
	 */
	private void removeSuperInvocations( AsyncTaskVisitor asyncTaskVisitor )
	{
		for ( SuperMethodInvocation methodInvocation : asyncTaskVisitor.getSuperClassMethodInvocation() )
		{
			Statement statement = (Statement) ASTUtil.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}

	private void addRxObservable( ICompilationUnit icu, SingleUnitExtensionWriter rewriter, Statement referenceStatement,
			AsyncTaskVisitor asyncTaskVisitor, AnonymousClassDeclaration anonymousCahcedClassDecleration )
	{
		boolean complexRxObservableClassNeeded = asyncTaskVisitor.hasField();
		boolean onProgressUpdateBlock = asyncTaskVisitor.getOnProgressUpdateBlock() != null;
		removeSuperInvocations( asyncTaskVisitor );

		RxSubscriberHolder subscriberHolder = null;
		if ( onProgressUpdateBlock )
		{
			subscriberHolder = new RxSubscriberHolder( icu.getElementName(),
					asyncTaskVisitor.getProgressType().toString() + "[]", asyncTaskVisitor.getOnProgressUpdateBlock(),
					asyncTaskVisitor.getProgressParameters() );
		}

		AST ast = referenceStatement.getAST();
		if ( !complexRxObservableClassNeeded && asyncTaskVisitor.getIsVoid() )
		{
			String subscribedObservable = createObservable( asyncTaskVisitor, subscriberHolder ).addSubscribe().build();

			if ( onProgressUpdateBlock )
			{
				String newMethodString = subscriberHolder.getGetMethodDeclaration();
				MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText( ast, newMethodString );
				rewriter.addMethodAfter( newMethod, anonymousCahcedClassDecleration );

				String subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				Statement getSubscriberStatement = ASTNodeFactory.createSingleStatementFromText( ast, subscriberDecl );
				rewriter.addStatementBefore( getSubscriberStatement, referenceStatement );
			}

			Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast, subscribedObservable );

			rewriter.replaceStatement( referenceStatement, newStatement );

		}
		else
		{
			List<FieldDeclaration> fieldDeclarations = asyncTaskVisitor.getFieldDeclarations();
			String subscriberDecl = EMPTY;
			String subscriberGetRxUpdateMethod = EMPTY;
			if ( onProgressUpdateBlock )
			{
				subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				subscriberGetRxUpdateMethod = subscriberHolder.getGetMethodDeclaration();
			}

			String observableStatement = createObservable( asyncTaskVisitor, subscriberHolder ).buildReturnStatement();
			String observableType = asyncTaskVisitor.getReturnedType().toString();

			ComplexRxObservableBuilder complexObservable = ComplexRxObservableBuilder
					.newComplexRxObservable( icu.getElementName() ).withFields( fieldDeclarations )
					.withGetAsyncObservable( observableType, subscriberDecl, observableStatement )
					.withMethod( subscriberGetRxUpdateMethod )
					.withMethods( asyncTaskVisitor.getAdditionalMethodDeclarations() );

			String complexRxObservableClass = complexObservable.build();
			// initialize class name which will be used for replacing execute
			// method usage
			complexObservableclassName = complexObservable.getComplexObservableName();

			// initialize asyncmethodname to be used at execute
			asyncMethodName = complexObservable.getAsyncmethodName();

			// initialize asyncmethodname to be used at execute
			subscription = complexObservable.getAsynSubscription();

			TypeDeclaration complexRxObservableDecl = ASTNodeFactory.createTypeDeclarationFromText( ast,
					complexRxObservableClass );
			rewriter.addInnerClassAfter( complexRxObservableDecl, referenceStatement );
		}

	}

	private RxObservableStringBuilder createObservable( AsyncTaskVisitor asyncTaskVisitor,
			RxSubscriberHolder subscriberHolder )
	{
		Block doInBackgroundBlock = asyncTaskVisitor.getDoInBackgroundBlock();
		Block doOnCompletedBlock = asyncTaskVisitor.getOnPostExecuteBlock();

		String type = ( asyncTaskVisitor.getReturnedType() == null ? "Void"
				: asyncTaskVisitor.getReturnedType().toString() );
		String postExecuteParameters = asyncTaskVisitor.getPostExecuteParameters();

		if ( type == null )
		{
			System.out.println( "NULL type for Do In Background" );
		}
		replacePublishInvocations( asyncTaskVisitor, subscriberHolder );
		ASTUtil.removeUnnecessaryCatchClauses( doOnCompletedBlock );

		RxObservableStringBuilder complexObservable = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD )
				.addDoOnNext( ( doOnCompletedBlock == null ? "{}" : doOnCompletedBlock.toString() ),
						postExecuteParameters == null ? "arg" : postExecuteParameters, type, false );

		Block preExec = asyncTaskVisitor.getOnPreExecuteBlock();
		if ( preExec != null )
		{
			complexObservable.addDoOnPreExecute( preExec );
		}

		Block onCancelled = asyncTaskVisitor.getOnCancelled();
		if ( onCancelled != null )
		{
			complexObservable.addDoOnCancelled( onCancelled );
		}

		return complexObservable;

	}

	private void replacePublishInvocations( AsyncTaskVisitor asyncTaskVisitor, RxSubscriberHolder subscriberHolder )
	{
		if ( !asyncTaskVisitor.getPublishInvocations().isEmpty() )
		{
			for ( MethodInvocation publishInvocation : asyncTaskVisitor.getPublishInvocations() )
			{
				List<?> argumentList = publishInvocation.arguments();
				AST ast = publishInvocation.getAST();
				replacePublishInvocationsAux( subscriberHolder, publishInvocation, argumentList, ast );
			}
		}
	}

	private <T extends ASTNode> void replacePublishInvocationsAux( RxSubscriberHolder subscriberHolder,
			T publishInvocation, List<?> argumentList, AST ast )
	{
		String newInvocation = subscriberHolder.getOnNextInvocation( argumentList, subscriberHolder.getType() );
		Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast, newInvocation );
		ASTUtil.replaceInStatement( publishInvocation, newStatement );
	}

	public int getNUMBER_OF_ANONYMOUS_TASKS()
	{
		return NUMBER_OF_ANONYMOUS_TASKS;
	}

}
