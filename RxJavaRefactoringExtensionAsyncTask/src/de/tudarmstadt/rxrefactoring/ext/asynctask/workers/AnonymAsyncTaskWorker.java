package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.codegen.ASTNodeFactory;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractRefactorWorker;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriters;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.ComplexObservableBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.ObservableBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.SubscriberHolder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.collect.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.SchedulerType;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.writers.UnitWriterExt;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AnonymAsyncTaskWorker extends AbstractRefactorWorker<AsyncTaskCollector>
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

	public AnonymAsyncTaskWorker( AsyncTaskCollector collector )
	{
		super( collector );
	}

	@Override
	public WorkerStatus refactor()
	{

		Multimap<ICompilationUnit, AnonymousClassDeclaration> cuAnonymousClassesMap = collector.getCuAnonymousClassesMap();
		
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numCunits );
		Log.info( getClass(), "METHOD=refactor - Number of compilation units: " + numCunits );
		
		for (ICompilationUnit icu : cuAnonymousClassesMap.keySet()) {
			
			Collection<AnonymousClassDeclaration> declarations = cuAnonymousClassesMap.get(icu);
			
			for (AnonymousClassDeclaration asyncTaskDeclaration : declarations) {
				
				Log.info(getClass(), "METHOD=refactor - Extract Information from AsyncTask: " + icu.getElementName());
				AsyncTaskVisitor asyncTaskVisitor = new AsyncTaskVisitor();
				asyncTaskDeclaration.accept(asyncTaskVisitor);
				
				if ( asyncTaskVisitor.getDoInBackgroundBlock() == null )
					continue;
				
				AST ast = asyncTaskDeclaration.getAST();

				UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(icu, () -> new UnitWriterExt( icu, ast, getClass().getSimpleName() ));
				
				Log.info( getClass(), "METHOD=refactor - Updating imports: " + icu.getElementName() );

				Statement referenceStatement = ASTUtils.findParent( asyncTaskDeclaration, Statement.class );
				addRxObservable( icu, singleChangeWriter, referenceStatement, asyncTaskVisitor, asyncTaskDeclaration );
				
				if (asyncTaskVisitor.hasField()) {
					if ( !checkForimplecitExecute( collector.getCuRelevantUsagesMap(), ast, singleChangeWriter,
							asyncTaskDeclaration, icu ) )
						singleChangeWriter.removeStatement( asyncTaskDeclaration );
					updateUsage( collector.getCuRelevantUsagesMap(), ast, singleChangeWriter, asyncTaskDeclaration, icu );
				}
				
				updateImports( singleChangeWriter, asyncTaskVisitor );
				Log.info( getClass(), "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				NUMBER_OF_ANONYMOUS_TASKS++;

				execution.addUnitWriter(singleChangeWriter);
			}
			monitor.worked( 1 );
		}
		
		Log.info( getClass(), "Number of Anonymous AsynckTasks =  " + NUMBER_OF_ANONYMOUS_TASKS );
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
	private boolean checkForimplecitExecute( Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			UnitWriterExt singleChangeWriter, AnonymousClassDeclaration asyncTaskDeclaration,
			ICompilationUnit icu )
	{
		String variableName = "getObservable";
		MethodInvocation implicitExecute = null;
		ASTNode execute = AsyncTaskASTUtils.findOuterParent( asyncTaskDeclaration, MethodInvocation.class );
		if ( execute != null )
			implicitExecute = (MethodInvocation) execute;
		if ( implicitExecute != null )
			if ( implicitExecute.getName().toString().equals( EXECUTE )
					|| implicitExecute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
			{
				VariableDeclarationFragment var = (VariableDeclarationFragment) ASTUtils.findParent( implicitExecute,
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
				Statement s = (Statement) ASTUtils.findParent( asyncTaskDeclaration, Statement.class );
				replaceExecuteImplicit( me, icu, ast, false, s );
				return true;
			}
		return false;
	}

	private void updateUsage( Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			UnitWriterExt SingleUnitExtensionWriter, AnonymousClassDeclaration asyncTaskDeclaration,
			ICompilationUnit icuOuter )
	{
		for ( ICompilationUnit icu : cuRelevantUsagesMap.keySet() )
		{
			Log.info( getClass(), "METHOD=updateUsage - updating usage for class: " + icuOuter.getElementName() + " in "
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
						Log.info( getClass(),
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

		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(icu, () -> new UnitWriterExt( icu, astInvoke, getClass().getSimpleName()));
		Log.info( getClass(),
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
		
		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(icu, () -> new UnitWriterExt( icu, astInvoke, getClass().getSimpleName()));
		
		AST methodAST = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( methodAST, methodInvoke );
		if ( execute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
		{
			execute.arguments().clear();
		}
		execute.setExpression( AsyncTaskASTUtils.getinstannceCreationStatement( methodAST, complexObservableclassName ) );
		execute.setName( methodAST.newSimpleName( asyncMethodName ) );

		MethodInvocation invocation = methodAST.newMethodInvocation();

		invocation.setExpression( execute );
		invocation.setName( methodAST.newSimpleName( SUBSCRIBE ) );
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		if ( !withSubscription )
		{
			singleChangeWriter.replace( methodInvoke, invocation );
		}
		else
		{
			createSubscriptionDeclaration( icu, methodInvoke );
			Assignment initSubscription = methodAST.newAssignment();

			initSubscription.setLeftHandSide(
					methodAST.newSimpleName( getVariableName( methodInvoke.getExpression() ) + subscription ) );
			initSubscription.setRightHandSide( invocation );
			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
			singleChangeWriter.replace( methodInvoke, initSubscription );
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteImplicit( MethodInvocation methodInvoke, ICompilationUnit icu, AST ast,
			boolean withSubscription, Statement referenceStatement )
	{
		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(icu, () -> new UnitWriterExt( icu, ast, getClass().getSimpleName()));
		
		AST methodAST = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( methodAST, methodInvoke );
		if ( execute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
		{
			execute.arguments().clear();
		}
		execute.setExpression( AsyncTaskASTUtils.getinstannceCreationStatement( methodAST, complexObservableclassName ) );
		execute.setName( methodAST.newSimpleName( asyncMethodName ) );

		MethodInvocation invocation = methodAST.newMethodInvocation();

		invocation.setExpression( execute );
		invocation.setName( methodAST.newSimpleName( SUBSCRIBE ) );
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		Statement ss = ASTNodeFactory.createSingleStatementFromText( methodAST, invocation.toString() );
		if ( !withSubscription )
		{
			singleChangeWriter.replace( referenceStatement, ss );
		}
		else
		{
			createSubscriptionDeclaration( icu, methodInvoke );
			Assignment initSubscription = methodAST.newAssignment();

			initSubscription.setLeftHandSide(
					methodAST.newSimpleName( getVariableName( methodInvoke.getExpression() ) + subscription ) );
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
		TypeDeclaration tyDec = ASTUtils.findParent( methodInvoke, TypeDeclaration.class );
		AST astInvoke = tyDec.getAST();

		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(icu, () -> new UnitWriterExt( icu, astInvoke, getClass().getSimpleName()));

		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		variable.setName( astInvoke.newSimpleName( methodInvoke.getExpression().toString() + subscription ) );
		FieldDeclaration subscription = astInvoke.newFieldDeclaration( variable );
		subscription.setType( astInvoke.newSimpleType( astInvoke.newSimpleName( "Subscription" ) ) );
		singleChangeWriter.addStatementToClass( subscription, tyDec );
		singleChangeWriter.addImport( "rx.Subscription" );
	}

	private void updateImports( UnitWriterExt rewriter, AsyncTaskVisitor asyncTaskVisitor )
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
			Statement statement = (Statement) ASTUtils.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}

	private void addRxObservable( ICompilationUnit icu, UnitWriterExt rewriter, Statement referenceStatement,
			AsyncTaskVisitor asyncTaskVisitor, AnonymousClassDeclaration anonymousCahcedClassDecleration )
	{
		boolean complexRxObservableClassNeeded = asyncTaskVisitor.hasField();
		boolean onProgressUpdateBlock = asyncTaskVisitor.getOnProgressUpdateBlock() != null;
		removeSuperInvocations( asyncTaskVisitor );

		SubscriberHolder subscriberHolder = null;
		if ( onProgressUpdateBlock )
		{
			subscriberHolder = new SubscriberHolder( icu.getElementName(),
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

			ComplexObservableBuilder complexObservable = ComplexObservableBuilder
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

	private ObservableBuilder createObservable( AsyncTaskVisitor asyncTaskVisitor,
			SubscriberHolder subscriberHolder )
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
		ASTUtils.removeUnnecessaryCatchClauses( doOnCompletedBlock );

		ObservableBuilder complexObservable = ObservableBuilder
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

	private void replacePublishInvocations( AsyncTaskVisitor asyncTaskVisitor, SubscriberHolder subscriberHolder )
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

	private <T extends ASTNode> void replacePublishInvocationsAux( SubscriberHolder subscriberHolder,
			T publishInvocation, List<?> argumentList, AST ast )
	{
		String newInvocation = subscriberHolder.getOnNextInvocation( argumentList, subscriberHolder.getType() );
		Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast, newInvocation );
		ASTUtils.replaceInStatement( publishInvocation, newStatement );
	}

	public int getNUMBER_OF_ANONYMOUS_TASKS()
	{
		return NUMBER_OF_ANONYMOUS_TASKS;
	}

}
