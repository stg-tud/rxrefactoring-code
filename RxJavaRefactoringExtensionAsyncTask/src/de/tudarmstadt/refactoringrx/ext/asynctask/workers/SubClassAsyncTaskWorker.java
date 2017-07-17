/**
 * 
 */
package de.tudarmstadt.refactoringrx.ext.asynctask.workers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.refactoringrx.ext.asynctask.builders.ObservableBuilder;
import de.tudarmstadt.refactoringrx.ext.asynctask.builders.SubscriberBuilder;
import de.tudarmstadt.refactoringrx.ext.asynctask.collect.AsyncTaskCollector;
import de.tudarmstadt.refactoringrx.ext.asynctask.domain.SchedulerType;
import de.tudarmstadt.refactoringrx.ext.asynctask.writers.UnitWriterExt;
import de.tudarmstadt.rxrefactoring.core.codegen.ASTNodeFactory;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractRefactorWorker;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriters;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import com.google.common.collect.Multimap;

/**
 * Description: This worker is in charge of refactoring classes that extends
 * AsyncTasks that are assigned to a variable.<br>
 * Example: class Task extends AsyncTask<>{...}<br>
 * Author: Ram<br>
 * Created: 11/12/2016
 */
public class SubClassAsyncTaskWorker extends AbstractRefactorWorker<AsyncTaskCollector>
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

	public SubClassAsyncTaskWorker( AsyncTaskCollector collector )
	{
		super( collector );
	}

	@Override
	public WorkerStatus refactor()
	{
		NUMBER_OF_ASYNC_TASKS = 0;
		Multimap<ICompilationUnit, TypeDeclaration> cuAnonymousClassesMap = collector.getCuSubclassesMap();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numCunits );
		Log.info( this, "METHOD=refactor - Number of compilation units: " + numCunits );
		
		for ( ICompilationUnit icu : cuAnonymousClassesMap.keySet() )
		{
			Collection<TypeDeclaration> declarations = cuAnonymousClassesMap.get( icu );
			for ( TypeDeclaration asyncTaskDeclaration : declarations )
			{

				Log.info( this, "METHOD=refactor - Extract Information from AsyncTask: " + icu.getElementName() );
				AsyncTaskVisitor asyncTaskVisitor = new AsyncTaskVisitor();
				asyncTaskDeclaration.accept( asyncTaskVisitor );
				if ( asyncTaskVisitor.getDoInBackgroundBlock() == null )
				{
					NUMBER_OF_ABSTRACT_TASKS++;
					continue;
				}
				
				AST ast = asyncTaskDeclaration.getAST();
				UnitWriterExt singleChangeWriter = UnitWriters.getOrElse( icu, () -> new UnitWriterExt( icu, ast, getClass().getSimpleName()));
				updateUsage( collector.getCuRelevantUsagesMap(), ast, singleChangeWriter, asyncTaskDeclaration, icu );
				singleChangeWriter.removeSuperClass( asyncTaskDeclaration );
				Log.info( this, "METHOD=refactor - Updating imports: " + icu.getElementName() );
				updateImports( singleChangeWriter, asyncTaskVisitor );
				createLocalObservable( singleChangeWriter, asyncTaskDeclaration, ast, asyncTaskVisitor );
				addProgressBlock( ast, icu, asyncTaskVisitor, singleChangeWriter );
				Log.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );

				if ( asyncTaskVisitor.getOnPreExecuteBlock() != null )
					updateonPreExecute( asyncTaskDeclaration, singleChangeWriter, ast, asyncTaskVisitor );

				NUMBER_OF_ASYNC_TASKS++;
				execution.addUnitWriter( singleChangeWriter );
			}

			monitor.worked( 1 );
		}
		Log.info( this, "Number of AsynckTasks Subclass=  " + NUMBER_OF_ASYNC_TASKS );
		Log.info( this, "Number of Abstract AsynckTasks Subclass=  " + NUMBER_OF_ABSTRACT_TASKS );

		return WorkerStatus.OK;
	}

	private void updateUsage( Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			UnitWriterExt singleUnitExtensionWriter, TypeDeclaration asyncTaskDeclaration,
			ICompilationUnit icuOuter )
	{
		for ( ICompilationUnit icu : cuRelevantUsagesMap.keySet() )
		{
			TypeDeclaration tyDec = ast.newTypeDeclaration();

			AST astInvoke = ast;
			Log.info( this, "METHOD=updateUsage - updating usage for class: " + icuOuter.getElementName() + " in "
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
						String newName = ASTUtils.findParent( methodInvoke, TypeDeclaration.class ).getName().toString();
						tyDec = ASTUtils.findParent( methodInvoke, TypeDeclaration.class );
						astInvoke = tyDec.getAST();
						replaceCancel( cuRelevantUsagesMap, methodInvoke, icu, astInvoke );
						execution.addUnitWriter( singleUnitExtensionWriter );
					}

				}
			}

		}

	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 */
	void replaceCancel( Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, MethodInvocation methodInvoke,
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
						Log.info( this,
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
		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse( icu, () -> new UnitWriterExt( icu, astInvoke, getClass().getSimpleName()));
		
		AST methodAST = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( methodAST, methodInvoke );
		if ( execute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
		{
			execute.arguments().clear();
		}
		execute.setName( methodAST.newSimpleName( ASYNC_METHOD_NAME ) );

		MethodInvocation invocation = methodAST.newMethodInvocation();
		invocation.setExpression( execute );
		invocation.setName( methodAST.newSimpleName( SUBSCRIBE ) );
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
		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse( icu, () -> new UnitWriterExt( icu, astInvoke, getClass().getSimpleName()));

		Log.info( this, "METHOD=replaceExecuteWithSubscription - replace Execute With Subscription for class: "
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
		Log.info( this, "METHOD=createSubscriptionDeclaration - create Subscription Declaration for class: "
				+ icu.getElementName() );
		TypeDeclaration tyDec = ASTUtils.findParent( methodInvoke, TypeDeclaration.class );
		AST astInvoke = tyDec.getAST();
		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(icu, () -> new UnitWriterExt( icu, astInvoke, getClass().getSimpleName()));
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

		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(icu, () -> new UnitWriterExt( icu, astInvoke, getClass().getSimpleName()));
		Log.info( this,
				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + icu.getElementName() );
		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression( astInvoke.newSimpleName( getVariableName( methodInvoke.getExpression() ) + SUBSCRIPTION ) );
		unSubscribe.setName( astInvoke.newSimpleName( UN_SUBSCRIBE ) );
		singleChangeWriter.replace( methodInvoke, unSubscribe );

	}

	private void updateImports( UnitWriterExt rewriter, AsyncTaskVisitor asyncTaskVisitor )
	{
		rewriter.addImport( "rx.Observable" );
		rewriter.addImport( "rx.schedulers.Schedulers" );
		rewriter.addImport( "java.util.concurrent.Callable" );
		if ( NUMBER_OF_ABSTRACT_TASKS == 0 )
		{
			rewriter.removeImport( "android.os.AsyncTask" );
		}

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
		if ( asyncTaskVisitor.getOnCancelled() != null ) {
			rewriter.addImport( "rx.functions.Action0" );
		}
		if (asyncTaskVisitor.getOnPreExecuteBlock() != null) {
			rewriter.addImport( "rx.functions.Action0" );
		}
	}

	private void createLocalObservable( UnitWriterExt rewriter, TypeDeclaration taskObject, AST ast,
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
	private void updateonPreExecute( TypeDeclaration parent, UnitWriterExt singleChangeWriter, AST ast,
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
		ObservableBuilder rxObservable = ObservableBuilder.newObservable( type, doInBackgroundBlock,
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
			Statement statement = (Statement) ASTUtils.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}

	/**
	 * If onUpdateProgressmethod exist method will add a new method to class
	 */
	private void addProgressBlock( AST ast, ICompilationUnit icu, AsyncTaskVisitor asyncTaskVisitor,
			UnitWriterExt singleChangeWriter )
	{

		if ( asyncTaskVisitor.getOnProgressUpdateBlock() != null )
		{
			TypeDeclaration tyDec = (TypeDeclaration) ASTUtils
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
		String newSubscriber = SubscriberBuilder.newSubscriber( progressType.toString(), doProgressUpdate,
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
	private void replacePublishInvocations( AsyncTaskVisitor asynctaskVisitor, UnitWriterExt rewriter )
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
			UnitWriterExt rewriter, AsyncTaskVisitor asyncTaskVisitor )
	{
		ASTNode referenceStatement = publishInvocation;
		String value = argumentList.toString().replace( "[", "" ).replace( "]", "" );
		String newInvocation = "getRxUpdateSubscriber().onNext((" + asyncTaskVisitor.getProgressType()
				+ "[])Arrays.asList(" + value + ").toArray())";
		Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast,
				newInvocation );
		if ( !( publishInvocation instanceof Statement ) )
		{
			referenceStatement = ASTUtils.findParent( publishInvocation, Statement.class );
		}
		System.out.println( publishInvocation + " -> " + newStatement );
		ASTUtils.replaceInStatement( publishInvocation, newStatement );
	}
}
