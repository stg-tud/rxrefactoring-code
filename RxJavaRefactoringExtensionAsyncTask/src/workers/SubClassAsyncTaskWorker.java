/**
 * 
 */
package workers;

import java.util.List;
import java.util.Map;

import builders.RxSubscriberStringBuilder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import builders.RxObservableStringBuilder;
import domain.SchedulerType;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import visitors.AsyncTaskVisitor;
import visitors.CuCollector;
import writer.SingleUnitExtensionWriter;

/**
 * Description: This worker is in charge of refactoring classes that extends
 * AsyncTasks that are assigned to a variable.<br>
 * Example: class Task extends AsyncTask<>{...}<br>
 * Author: Ram<br>
 * Created: 11/12/2016
 */
public class SubClassAsyncTaskWorker extends AbstractRefactorWorker<CuCollector>
{
	final String SUBSCRIPTION = "Subscription";
	final String EXECUTE = "execute";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	final String CANCEL = "cancel";
	final String ASYNC_METHOD_NAME = "getAsyncObservable";
	final String SUBSCRIBE = "subscribe";
	final String UN_SUBSCRIBE = "unsubscribe";
	private int NUMBER_OF_ASYNC_TASKS = 0;
	private int NUMBER_OF_ABSTRACT_TASKS = 0;

	/**
	 * @return the nUMBER_OF_ASYNC_TASKS
	 */
	public int getNUMBER_OF_ASYNC_TASKS()
	{
		return NUMBER_OF_ASYNC_TASKS;
	}

	/**
	 * @return the nUMBER_OF_ABSTRACT_TASKS
	 */
	public int getNUMBER_OF_ABSTRACT_TASKS()
	{
		return NUMBER_OF_ABSTRACT_TASKS;
	}

	public SubClassAsyncTaskWorker( CuCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		NUMBER_OF_ASYNC_TASKS = 0;
		Map<ICompilationUnit, List<TypeDeclaration>> cuAnonymousClassesMap = collector.getCuSubclassesMap();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numCunits );
		RxLogger.info( this, "METHOD=refactor - Number of compilation units: " + numCunits );
		for ( ICompilationUnit icu : cuAnonymousClassesMap.keySet() )
		{
			List<TypeDeclaration> declarations = cuAnonymousClassesMap.get( icu );
			for ( TypeDeclaration asyncTaskDeclaration : declarations )
			{

				RxLogger.info( this, "METHOD=refactor - Extract Information from AsyncTask: " + icu.getElementName() );
				AsyncTaskVisitor asyncTaskVisitor = new AsyncTaskVisitor();
				asyncTaskDeclaration.accept( asyncTaskVisitor );
				if ( asyncTaskVisitor.getDoInBackgroundBlock() == null )
				{
					NUMBER_OF_ABSTRACT_TASKS++;
					continue;
				}
				AST ast = asyncTaskDeclaration.getAST();
				SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, ast, getClass().getSimpleName() );
				SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
				updateUsage( collector.getCuRelevantUsagesMap(), ast, singleChangeWriter, asyncTaskDeclaration, icu );
				singleChangeWriter.removeSuperClass( asyncTaskDeclaration );
				RxLogger.info( this, "METHOD=refactor - Updating imports: " + icu.getElementName() );
				updateImports( singleChangeWriter, asyncTaskVisitor );
				createLocalObservable( singleChangeWriter, asyncTaskDeclaration, ast, asyncTaskVisitor );
				addProgressBlock( ast, icu, asyncTaskVisitor, singleChangeWriter );
				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );

				if ( asyncTaskVisitor.getOnPreExecuteBlock() != null )
					updateonPreExecute( asyncTaskDeclaration, singleChangeWriter, ast, asyncTaskVisitor );

				NUMBER_OF_ASYNC_TASKS++;
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}

			monitor.worked( 1 );
		}
		RxLogger.info( this, "Number of AsynckTasks Subclass=  " + NUMBER_OF_ASYNC_TASKS );
		RxLogger.info( this, "Number of Abstract AsynckTasks Subclass=  " + NUMBER_OF_ABSTRACT_TASKS );

		return WorkerStatus.OK;
	}

	private void updateUsage( Map<ICompilationUnit, List<MethodInvocation>> cuRelevantUsagesMap, AST ast,
			SingleUnitExtensionWriter SingleUnitExtensionWriter, TypeDeclaration asyncTaskDeclaration,
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
						String newName = ASTUtil.findParent( methodInvoke, TypeDeclaration.class ).getName().toString();
						tyDec = ASTUtil.findParent( methodInvoke, TypeDeclaration.class );
						astInvoke = tyDec.getAST();
						replaceCancel( cuRelevantUsagesMap, methodInvoke, icu, astInvoke );
						rxMultipleUnitsWriter.addCompilationUnit( icu );
					}

				}
			}

		}

	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
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
								"METHOD=replaceCancel - updating cancel invocation for class: " + icu.getElementName() );
						replaceExecuteWithSubscription( methodInvoke, icu, astInvoke );
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

			replaceExecute( methodInvoke, icu, astInvoke );
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute( MethodInvocation methodInvoke, ICompilationUnit icu, AST astInvoke )
	{
		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
		astInvoke = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( astInvoke, methodInvoke );
		if ( execute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
		{
			execute.arguments().clear();
		}
		execute.setName( astInvoke.newSimpleName( ASYNC_METHOD_NAME ) );

		MethodInvocation invocation = astInvoke.newMethodInvocation();
		invocation.setExpression( execute );
		invocation.setName( astInvoke.newSimpleName( SUBSCRIBE ) );
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		singleChangeWriter.replace( methodInvoke, invocation );
		// singleChangeWriter.replace(methodInvoke, invocation);
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteWithSubscription( MethodInvocation methodInvoke, ICompilationUnit icu, AST astInvoke )
	{
		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );

		RxLogger.info( this, "METHOD=replaceExecuteWithSubscription - replace Execute With Subscription for class: "
				+ icu.getElementName() );
		createSubscriptionDeclaration( icu, methodInvoke );

		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( astInvoke, methodInvoke );
		execute.setName( astInvoke.newSimpleName( ASYNC_METHOD_NAME ) );
		MethodInvocation invocation = astInvoke.newMethodInvocation();
		invocation.setExpression( execute );
		invocation.setName( astInvoke.newSimpleName( SUBSCRIBE ) );

		Assignment initSubscription = astInvoke.newAssignment();

		initSubscription
				.setLeftHandSide( astInvoke.newSimpleName( getVariableName( methodInvoke.getExpression() ) + SUBSCRIPTION ) );
		initSubscription.setRightHandSide( invocation );
		singleChangeWriter.replace( methodInvoke, initSubscription );
	}

	void createSubscriptionDeclaration( ICompilationUnit icu, MethodInvocation methodInvoke )
	{
		RxLogger.info( this, "METHOD=createSubscriptionDeclaration - create Subscription Declaration for class: "
				+ icu.getElementName() );
		TypeDeclaration tyDec = ASTUtil.findParent( methodInvoke, TypeDeclaration.class );
		AST astInvoke = tyDec.getAST();
		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		Expression e = methodInvoke.getExpression();

		variable.setName( astInvoke.newSimpleName( getVariableName( e ) + SUBSCRIPTION ) );

		FieldDeclaration subscription = astInvoke.newFieldDeclaration( variable );
		subscription.setType( astInvoke.newSimpleType( astInvoke.newSimpleName( SUBSCRIPTION ) ) );
		singleChangeWriter.addStatementToClass( subscription, tyDec );
		singleChangeWriter.addImport( "rx.Subscription" );
	}

	private String getVariableName( Expression e )
	{
		if ( e instanceof FieldAccess )
			return ( (FieldAccess) e ).getName().toString();
		else
			return e.toString();
	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
	private void updateCancelInvocation( ICompilationUnit icu, MethodInvocation methodInvoke, AST astInvoke )
	{

		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
		RxLogger.info( this,
				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + icu.getElementName() );
		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression( astInvoke.newSimpleName( getVariableName( methodInvoke.getExpression() ) + SUBSCRIPTION ) );
		unSubscribe.setName( astInvoke.newSimpleName( UN_SUBSCRIBE ) );
		singleChangeWriter.replace( methodInvoke, unSubscribe );

	}

	private void updateImports( SingleUnitExtensionWriter rewriter, AsyncTaskVisitor asyncTaskVisitor )
	{
		rewriter.addImport( "rx.Observable" );
		rewriter.addImport( "rx.schedulers.Schedulers" );
		rewriter.addImport( "java.util.concurrent.Callable" );
		if ( NUMBER_OF_ABSTRACT_TASKS == 0 )
		{
			rewriter.removeImport( "android.os.AsyncTask" );
		}
		rewriter.removeImport( "javax.swing.SwingWorker" );
		if ( asyncTaskVisitor.getOnPostExecuteBlock() != null )
		{
			rewriter.addImport( "rx.functions.Action1" );
			rewriter.removeImport( "java.util.concurrent.ExecutionException" );
			rewriter.removeImport( "java.util.concurrent.TimeoutException" );

		}
		if ( asyncTaskVisitor.getDoInBackgroundBlock() != null )
		{
			rewriter.addImport( "rx.Subscriber" );
			rewriter.addImport( "java.util.Arrays" );
			rewriter.addImport( "rx.Subscription" );
			rewriter.addImport( "rx.functions.Action1" );
		}
		if ( asyncTaskVisitor.getOnProgressUpdateBlock() != null )
		{
			rewriter.addImport( "java.util.Arrays" );
		}
		if ( asyncTaskVisitor.getOnCancelled() != null )
		{
			rewriter.addImport( "rx.functions.Action0" );
		}
	}

	private void createLocalObservable( SingleUnitExtensionWriter rewriter, TypeDeclaration taskObject, AST ast,
			AsyncTaskVisitor asyncTaskVisitor )
	{
		replacePublishInvocations( asyncTaskVisitor, rewriter );
		String observableStatement = createObservable( asyncTaskVisitor );
		MethodDeclaration getAsyncMethod = ASTNodeFactory.createMethodFromText( ast,
				"public Observable<" + asyncTaskVisitor.getReturnedType() + "> getAsyncObservable( final "
						+ asyncTaskVisitor.getParameters() + "){"
						+ " return "
						+ observableStatement + "}" );
		rewriter.replaceStatement( asyncTaskVisitor.getDoInBackgroundmethod(), getAsyncMethod );

		// Remove postExecute method
		if ( asyncTaskVisitor.getOnPostExecuteBlock() != null )
			rewriter.removeMethod( asyncTaskVisitor.getOnPostExecuteBlock().getParent(), taskObject );
		// Remove updateProgress method
		if ( asyncTaskVisitor.getOnProgressUpdateBlock() != null )
			rewriter.removeMethod( asyncTaskVisitor.getOnProgressUpdateBlock().getParent(), taskObject );
		// Remove onCancelled method
		if ( asyncTaskVisitor.getOnCancelled() != null )
			rewriter.removeMethod( asyncTaskVisitor.getOnCancelled().getParent(), taskObject );
	}

	/**
	 * onPreExecute method will be invoked by getAsyncObservable block as a
	 * first statement.
	 * 
	 * @override has to be removed and super() should be removed from
	 *           onPreExecuteBlock
	 * @param parent
	 * @param singleChangeWriter
	 * @param ast
	 * @param asyncTaskVisitor
	 */
	private void updateonPreExecute( TypeDeclaration parent, SingleUnitExtensionWriter singleChangeWriter, AST ast,
			AsyncTaskVisitor asyncTaskVisitor )
	{

		MethodDeclaration getAsyncMethod = ASTNodeFactory.createMethodFromText( ast,
				"public onPreExecute(){" + asyncTaskVisitor.getOnPreExecuteBlock().toString() + "}" );
		singleChangeWriter.replaceStatement( (MethodDeclaration) asyncTaskVisitor.getOnPreExecuteBlock().getParent(),
				getAsyncMethod );

	}

	private String createObservable( AsyncTaskVisitor asyncTaskVisitor )
	{

		Block doInBackgroundBlock = asyncTaskVisitor.getDoInBackgroundBlock();
		Block onPostExecuteBlock = asyncTaskVisitor.getOnPostExecuteBlock();
		Block doProgressUpdate = asyncTaskVisitor.getOnProgressUpdateBlock();
		Block onCancelled = asyncTaskVisitor.getOnCancelled();
		String type = asyncTaskVisitor.getReturnedType().toString();
		String postExecuteParameters = asyncTaskVisitor.getPostExecuteParameters();
		removeSuperInvocations( asyncTaskVisitor );
		RxObservableStringBuilder rxObservable = RxObservableStringBuilder.newObservable( type, doInBackgroundBlock,
				SchedulerType.JAVA_MAIN_THREAD );
		if ( doProgressUpdate != null )
		{
			if ( onPostExecuteBlock != null )
				rxObservable.addDoOnNext( onPostExecuteBlock.toString(), postExecuteParameters,
						asyncTaskVisitor.getPostExecuteType().toString(), false );
		}
		else
		{
			if ( onPostExecuteBlock != null )
				rxObservable.addDoOnNext( onPostExecuteBlock.toString(), postExecuteParameters, type, false );
		}
		if ( onCancelled != null )
		{
			rxObservable.addDoOnCancelled( onCancelled );
		}
		Block preExec = asyncTaskVisitor.getOnPreExecuteBlock();
		if ( preExec != null )
		{
			rxObservable.addDoOnPreExecute( preExec );
		}
		return rxObservable.build();
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

	/**
	 * If onUpdateProgressmethod exist method will add a new method to class
	 */
	private void addProgressBlock( AST ast, ICompilationUnit icu, AsyncTaskVisitor asyncTaskVisitor,
			SingleUnitExtensionWriter singleChangeWriter )
	{

		if ( asyncTaskVisitor.getOnProgressUpdateBlock() != null )
		{
			TypeDeclaration tyDec = (TypeDeclaration) ASTUtil
					.findParent( asyncTaskVisitor.getOnProgressUpdateBlock().getParent(), TypeDeclaration.class );
			String newMethodString = createNewMethod( asyncTaskVisitor );
			MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText( ast, newMethodString );
			singleChangeWriter.addMethod( newMethod, tyDec );
		}

	}

	/**
	 * Method which will be created if onProgressUpdate method is present in
	 * asyncTask
	 * 
	 * @param asyncTaskVisitor
	 * @return
	 */
	private String createNewMethod( AsyncTaskVisitor asyncTaskVisitor )
	{
		Block doProgressUpdate = asyncTaskVisitor.getOnProgressUpdateBlock();
		Type progressType = asyncTaskVisitor.getProgressType();
		String progressParameters = asyncTaskVisitor.getProgressParameters();
		String newSubscriber = RxSubscriberStringBuilder.newSubscriber( progressType.toString(), doProgressUpdate,
				progressParameters );
		return "private Subscriber<" + progressType.toString() + "[]> getRxUpdateSubscriber() { return " + newSubscriber
				+ "}";
	}

	/**
	 * Iterate on list of invocation of publishProgress
	 * 
	 * @param asynctaskVisitor
	 * @param rewriter
	 */
	private void replacePublishInvocations( AsyncTaskVisitor asynctaskVisitor, SingleUnitExtensionWriter rewriter )
	{
		if ( !asynctaskVisitor.getPublishInvocations().isEmpty() )
		{
			for ( MethodInvocation publishInvocation : asynctaskVisitor.getPublishInvocations() )
			{
				List argumentList = publishInvocation.arguments();
				AST ast = publishInvocation.getAST();
				replacePublishInvocationsAux( publishInvocation, argumentList, ast, rewriter, asynctaskVisitor );
			}
		}
	}

	/**
	 * Convert pubishProgress(parms) method to
	 * getRxUpdateSubscriber(parms).subscribe()
	 * 
	 * @param publishInvocation
	 * @param argumentList
	 * @param ast
	 */
	private <T extends ASTNode> void replacePublishInvocationsAux( T publishInvocation, List argumentList, AST ast,
			SingleUnitExtensionWriter rewriter, AsyncTaskVisitor asyncTaskVisitor )
	{
		ASTNode referenceStatement = publishInvocation;
		String value = argumentList.toString().replace( "[", "" ).replace( "]", "" );
		String newInvocation = "getRxUpdateSubscriber().onNext((" + asyncTaskVisitor.getProgressType()
				+ "[])Arrays.asList(" + value + ").toArray())";
		Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast,
				newInvocation );
		if ( !( publishInvocation instanceof Statement ) )
		{
			referenceStatement = ASTUtil.findParent( publishInvocation, Statement.class );
		}
		System.out.println( publishInvocation + " -> " + newStatement );
		ASTUtil.replaceInStatement( publishInvocation, newStatement );
	}
}
