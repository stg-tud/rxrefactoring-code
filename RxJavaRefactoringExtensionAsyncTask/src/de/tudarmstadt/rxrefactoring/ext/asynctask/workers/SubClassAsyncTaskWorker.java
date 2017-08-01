/**
 * 
 */
package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractWorker;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriters;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders2.InnerClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders2.RefactorNames;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders2.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.collect.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;
import de.tudarmstadt.rxrefactoring.ext.asynctask.writers.UnitWriterExt;


/**
 * Description: This worker is in charge of refactoring classes that extends
 * AsyncTasks that are assigned to a variable.<br>
 * Example: class Task extends AsyncTask<>{...}<br>
 * Author: Ram<br>
 * Created: 11/12/2016
 */
public class SubClassAsyncTaskWorker extends AbstractWorker<AsyncTaskCollector> implements WorkerEnvironment {
	final String SUBSCRIPTION = "Subscription";
	final String EXECUTE = "execute";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	final String CANCEL = "cancel";
	final String SUBSCRIBE = "subscribe";
	final String UN_SUBSCRIBE = "unsubscribe";
	
	private int numOfAsyncTasks = 0;
	private int numOfAbstractClasses = 0;

	/**
	 * @return the nUMBER_OF_ASYNC_TASKS
	 */
	public int numOfAsyncTasks() {
		return numOfAsyncTasks;
	}

	/**
	 * @return the nUMBER_OF_ABSTRACT_TASKS
	 */
	public int numOfAbstractClasses() {
		return numOfAbstractClasses;
	}

	public SubClassAsyncTaskWorker(AsyncTaskCollector collector) {
		super(collector);
	}

	@Override
	public WorkerStatus refactor() {
		//Reset counters
		numOfAsyncTasks = 0;
		numOfAbstractClasses = 0;
		
		Multimap<ICompilationUnit, TypeDeclaration> cuAnonymousClassesMap = collector.getSubclasses();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask(getClass().getSimpleName(), numCunits);
		Log.info(getClass(), "METHOD=refactor - Number of compilation units: " + numCunits);

		for (ICompilationUnit unit : cuAnonymousClassesMap.keySet()) {
			Collection<TypeDeclaration> declarations = cuAnonymousClassesMap.get(unit);
			for (TypeDeclaration asyncTaskDeclaration : declarations) {

				Log.info(getClass(), "METHOD=refactor - Extract Information from AsyncTask: " + unit.getElementName());
				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(asyncTaskDeclaration, unit);

				if (asyncTask.getDoInBackgroundBlock() == null) {
					numOfAbstractClasses++;
					continue;
				}

				AST ast = asyncTaskDeclaration.getAST();
				ASTNode root = collector.getRootNode(unit);
				UnitWriterExt writer = UnitWriters.getOrPut(unit,
						() -> new UnitWriterExt(unit, ast));
				
				
				
				updateUsage(collector.getRelevantUsages(), ast, asyncTaskDeclaration, unit);
				writer.removeSuperClass(asyncTaskDeclaration);
				
				Log.info(getClass(), "METHOD=refactor - Updating imports: " + unit.getElementName());
				updateImports(writer, asyncTask);
				createLocalObservable(asyncTask, writer, asyncTaskDeclaration, root);
				//addProgressBlock(ast, unit, asyncTask, writer);
				
				Log.info(getClass(), "METHOD=refactor - Refactoring class: " + unit.getElementName());
				
				numOfAsyncTasks++;
				execution.addUnitWriter(writer);
			}

			monitor.worked(1);
		}
		Log.info(getClass(), "Number of AsynckTasks Subclass=  " + numOfAsyncTasks);
		Log.info(getClass(), "Number of Abstract AsynckTasks Subclass=  " + numOfAbstractClasses);

		return WorkerStatus.OK;
	}

	private void updateUsage(Multimap<ICompilationUnit, MethodInvocation> relevantUsages, AST ast,
			TypeDeclaration asyncTaskDeclaration, ICompilationUnit unit) {
		
		
		for (ICompilationUnit usageUnit : relevantUsages.keySet()) {
			TypeDeclaration tyDec = ast.newTypeDeclaration();

			Log.info(getClass(), "METHOD=updateUsage - updating usage for class: " + unit.getElementName() + " in "
					+ usageUnit.getElementName());
			
			for (MethodInvocation methodInvoke : relevantUsages.get(usageUnit)) {
				if (methodInvoke.getName().toString().equals(EXECUTE)
						|| methodInvoke.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {

					if (unit.getElementName().equals(usageUnit.getElementName())) {
						UnitWriterExt writer = UnitWriters.getOrPut(unit, () -> new UnitWriterExt(unit, ast));
						replaceCancel(writer, relevantUsages, methodInvoke, unit, ast);
					} else {
						tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
						AST astInvoke = tyDec.getAST();
						UnitWriterExt writer = UnitWriters.getOrPut(usageUnit, () -> new UnitWriterExt(usageUnit, astInvoke));
						replaceCancel(writer, relevantUsages, methodInvoke, usageUnit, astInvoke);
						execution.addUnitWriter(writer);
					}

				}
			}

		}

	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 */
	void replaceCancel(UnitWriterExt writer, Multimap<ICompilationUnit, MethodInvocation> relevantUsages, MethodInvocation methodInvoke,
			ICompilationUnit unit, AST astInvoke) {
		
		
		
		boolean isCancelPresent = false;
		
		for (MethodInvocation methodReference : relevantUsages.get(unit)) {
			if (methodInvoke.getExpression().toString().equals(methodReference.getExpression().toString())) {
				if (methodReference.getName().toString().equals(CANCEL)) {
					isCancelPresent = true;
					Log.info(getClass(), "METHOD=replaceCancel - updating cancel invocation for class: "
							+ unit.getElementName());
					replaceExecuteWithSubscription(writer, methodInvoke, astInvoke);
					updateCancelInvocation(writer, methodReference, astInvoke);

				}
			}			
		}
		
		if (!isCancelPresent) {
			replaceExecute(writer, methodInvoke, unit, astInvoke);
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute(UnitWriterExt writer, MethodInvocation methodInvoke, ICompilationUnit unit, AST astInvoke) {

		AST methodAST = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(methodAST, methodInvoke);
		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
			execute.arguments().clear();
		}
		execute.setName(methodAST.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));

		MethodInvocation invocation = methodAST.newMethodInvocation();
		invocation.setExpression(execute);
		invocation.setName(methodAST.newSimpleName(SUBSCRIBE));
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		writer.replace(methodInvoke, invocation);
		// singleChangeWriter.replace(methodInvoke, invocation);
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteWithSubscription(UnitWriterExt writer, MethodInvocation methodInvoke, AST astInvoke) {


		Log.info(getClass(), "METHOD=replaceExecuteWithSubscription - replace Execute With Subscription for class: "
				+ writer.getUnit().getElementName());
		
		createSubscriptionDeclaration(writer, methodInvoke);

		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(astInvoke, methodInvoke);
		execute.setName(astInvoke.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));
		MethodInvocation invocation = astInvoke.newMethodInvocation();
		invocation.setExpression(execute);
		invocation.setName(astInvoke.newSimpleName(SUBSCRIBE));

		Assignment initSubscription = astInvoke.newAssignment();

		initSubscription
				.setLeftHandSide(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + SUBSCRIPTION));
		initSubscription.setRightHandSide(invocation);
		writer.replace(methodInvoke, initSubscription);
	}

	void createSubscriptionDeclaration(UnitWriterExt writer, MethodInvocation methodInvoke) {

		
		TypeDeclaration tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
		AST astInvoke = tyDec.getAST();

		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		Expression e = methodInvoke.getExpression();

		variable.setName(astInvoke.newSimpleName(getVariableName(e) + SUBSCRIPTION));

		FieldDeclaration subscription = astInvoke.newFieldDeclaration(variable);
		subscription.setType(astInvoke.newSimpleType(astInvoke.newSimpleName(SUBSCRIPTION)));
		writer.addStatementToClass(subscription, tyDec);
		writer.addImport("rx.Subscription");
	}

	private String getVariableName(Expression e) {
		if (e instanceof FieldAccess)
			return ((FieldAccess) e).getName().toString();
		else
			return e.toString();
	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
	private void updateCancelInvocation(UnitWriterExt writer, MethodInvocation methodInvoke, AST astInvoke) {

		Log.info(getClass(),
				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + writer.getUnit().getElementName());
		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + SUBSCRIPTION));
		unSubscribe.setName(astInvoke.newSimpleName(UN_SUBSCRIBE));
		writer.replace(methodInvoke, unSubscribe);

	}

	private void updateImports(UnitWriterExt rewriter, AsyncTaskWrapper asyncTask) {
		rewriter.addImport("rx.Observable");
		rewriter.addImport("rx.schedulers.Schedulers");
		rewriter.addImport("java.util.concurrent.Callable");

		if (numOfAbstractClasses == 0) {
			rewriter.removeImport("android.os.AsyncTask");
		}

		if (asyncTask.getOnPostExecuteBlock() != null) {
			rewriter.addImport("rx.functions.Action1");
			rewriter.removeImport("java.util.concurrent.ExecutionException");
			rewriter.removeImport("java.util.concurrent.TimeoutException");

		}

		if (asyncTask.getDoInBackgroundBlock() != null) {
			rewriter.addImport("rx.Subscriber");
			rewriter.addImport("java.util.Arrays");
			rewriter.addImport("rx.Subscription");
			rewriter.addImport("rx.functions.Action1");
		}

		if (asyncTask.getOnProgressUpdateBlock() != null) {
			rewriter.addImport("java.util.Arrays");
		}

		if (asyncTask.getOnCancelled() != null) {
			rewriter.addImport("rx.functions.Action0");
		}

		if (asyncTask.getOnPreExecuteBlock() != null) {
			rewriter.addImport("rx.functions.Action0");
		}
	}

	private void createLocalObservable(AsyncTaskWrapper asyncTask, UnitWriterExt writer, TypeDeclaration taskObject, ASTNode root) {
		
		//replacePublishInvocations(asyncTask, rewriter);
		
		SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask, writer);
		InnerClassBuilder innerClassBuilder = new InnerClassBuilder(asyncTask, writer, subscriberBuilder.getId());
		
		TypeDeclaration typeDecl = innerClassBuilder.buildInnerClassWithSubscriber(subscriberBuilder);
		if (asyncTask.hasProgressUpdate()) {
			replacePublishInvocations(asyncTask, writer, subscriberBuilder);
		}
		
				
		writer.replace(taskObject, typeDecl);
		//replaceInstanceCreations(writer, innerClassBuilder, taskObject, root);
		
		//Define method
		/*
		 * public Observable<T> getAsyncObservable(final PARAMETER) {
		 *   return NEW OBSERVABLE
		 * }
		 */		
//		SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask, writer);
//		ObservableMethodBuilder observableMethodBuilder = new ObservableMethodBuilder(asyncTask, writer, subscriberBuilder.getId());
//		writer.replaceStatement(asyncTask.getDoInBackgroundmethod(), observableMethodBuilder.buildCreateMethod());
//
//		// Remove postExecute method
//		if (asyncTask.getOnPostExecuteBlock() != null)
//			writer.removeMethod(asyncTask.getOnPostExecuteBlock().getParent(), taskObject);
//		// Remove updateProgress method
//		if (asyncTask.getOnProgressUpdateBlock() != null) {
//			replacePublishInvocations(asyncTask, writer, subscriberBuilder);
//			writer.removeMethod(asyncTask.getOnProgressUpdateBlock().getParent(), taskObject);
//		}
//			
//		// Remove onCancelled method
//		if (asyncTask.getOnCancelled() != null)
//			writer.removeMethod(asyncTask.getOnCancelled().getParent(), taskObject);
	
	}
	
	private void replaceInstanceCreations(UnitWriter writer, InnerClassBuilder builder, TypeDeclaration oldClass, ASTNode root) {
		
		
		final ITypeBinding oldClassType = oldClass.resolveBinding();
		if (oldClassType == null) {
			Log.info(getClass(), "Class could not have been resolved to a type: " + oldClass.getName());
			return;
		}
		
		
		class ClassInstanceCreationVisitor extends ASTVisitor {
			
						
			@Override 
			public boolean visit(ClassInstanceCreation node) {
				
				ITypeBinding type = node.resolveTypeBinding();
				
				if (type == null) return true;
				
				//if (ASTUtils.isTypeOf(node.getType(), oldClassType)) {
				if (type.getQualifiedName().equals(oldClassType.getQualifiedName())) {
					System.out.println("Test");
					writer.replace(node, builder.buildNewObservableWrapper());
				}					
				
				return true;
			}			
		}
		
		
		//TODO: This only looks for subclasses that have been found until now. 
		root.accept(new ClassInstanceCreationVisitor()); 
		
//		for (Entry<ICompilationUnit, ASTNode> entry : collector.getRootNodes().entrySet()) {
//			
//			UnitWriter writer = UnitWriters.getOrPut(entry.getKey(), () -> new UnitWriterExt(entry.getKey(), entry.getValue().getAST()));
//			
//			ClassInstanceCreationVisitor v = new ClassInstanceCreationVisitor(writer);
//			entry.getValue().accept(v);
//		}	
		
		System.out.println("Finished!");
		
		
		
	}


//	private Expression createObservable(AsyncTaskWrapper asyncTask, UnitWriter writer) {
//
////		Block doInBackgroundBlock = asyncTask.getDoInBackgroundBlock();
////		Block onPostExecuteBlock = asyncTask.getOnPostExecuteBlock();
////		Block doProgressUpdate = asyncTask.getOnProgressUpdateBlock();
////		Block onCancelled = asyncTask.getOnCancelled();
////		String type = asyncTask.getReturnType().toString();
////		String postExecuteParameters = asyncTask.getPostExecuteParameter().toString();
//		
//		
//		AnonymousClassBuilder builder = new AnonymousClassBuilder(asyncTask, writer);
//		
//		
//		ObservableBuilder rxObservable = ObservableBuilder.newObservable(asyncTask, writer, type, doInBackgroundBlock,
//				SchedulerType.JAVA_MAIN_THREAD);
//		
//		
//	
//		
////		if (doProgressUpdate != null) {
////			if (onPostExecuteBlock != null) {
////				rxObservable.addDoOnNext(onPostExecuteBlock.toString(), postExecuteParameters,
////						asyncTask.getPostExecuteType().toString(), true);
////			}
////
////		} else {
////			if (onPostExecuteBlock != null) {
////				rxObservable.addDoOnNext(onPostExecuteBlock.toString(), postExecuteParameters, type, true);
////			}
////		}
////		if (onCancelled != null) {
////			rxObservable.addDoOnCancelled(onCancelled);
////		}
////		Block preExec = asyncTask.getOnPreExecuteBlock();
////		if (preExec != null) {
////
////			rxObservable.addDoOnPreExecute(preExec);
////		}
//		return builder.create();
//	}

	/**
	 * Remove super method invocation from AsyncTask methods
	 * 
	 * @param asyncTask
	 */
//	private void removeSuperInvocations(AsyncTaskWrapper asyncTask) {
//		for (SuperMethodInvocation methodInvocation : asyncTask.getSuperClassMethodInvocation()) {
//			Statement statement = (Statement) ASTUtils.findParent(methodInvocation, Statement.class);
//			statement.delete();
//		}
//	}

//	/**
//	 * If onUpdateProgressmethod exist method will add a new method to class
//	 */
//	private void addProgressBlock(AST ast, ICompilationUnit unit, AsyncTaskWrapper asyncTask,
//			UnitWriterExt singleChangeWriter) {
//
//		if (asyncTask.getOnProgressUpdateBlock() != null) {
//			TypeDeclaration tyDec = (TypeDeclaration) ASTUtils
//					.findParent(asyncTask.getOnProgressUpdateBlock().getParent(), TypeDeclaration.class);
//			String newMethodString = createNewMethod(asyncTask);
//			MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText(ast, newMethodString);
//			singleChangeWriter.addMethod(newMethod, tyDec);
//		}
//
//	}
//
//	/**
//	 * Method which will be created if onProgressUpdate method is present in
//	 * asyncTask
//	 * 
//	 * @param asyncTask
//	 * @return
//	 */
//	private String createNewMethod(AsyncTaskWrapper asyncTask) {
//		Block doProgressUpdate = asyncTask.getOnProgressUpdateBlock();
//		Type progressType = asyncTask.getProgressParameter().getType();
//		String progressParameters = asyncTask.getProgressParameter().toString();
//		
//		String newSubscriber = SubscriberBuilder.newSubscriber(progressType.toString(), doProgressUpdate,
//				progressParameters);
//		return "private Subscriber<" + progressType.toString() + "[]> getRxUpdateSubscriber() { return " + newSubscriber
//				+ "}";
//	}

	
}


//public class SubClassAsyncTaskWorker extends AbstractRefactorWorker<CuCollector>
//{
//	final String SUBSCRIPTION = "Subscription";
//	final String EXECUTE = "execute";
//	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
//	final String CANCEL = "cancel";
//	final String ASYNC_METHOD_NAME = "getAsyncObservable";
//	final String SUBSCRIBE = "subscribe";
//	final String UN_SUBSCRIBE = "unsubscribe";
//	private int NUMBER_OF_ASYNC_TASKS = 0;
//	private int NUMBER_OF_ABSTRACT_TASKS = 0;
//
//	/**
//	 * @return the nUMBER_OF_ASYNC_TASKS
//	 */
//	public int getNUMBER_OF_ASYNC_TASKS()
//	{
//		return NUMBER_OF_ASYNC_TASKS;
//	}
//
//	/**
//	 * @return the nUMBER_OF_ABSTRACT_TASKS
//	 */
//	public int getNUMBER_OF_ABSTRACT_TASKS()
//	{
//		return NUMBER_OF_ABSTRACT_TASKS;
//	}
//
//	public SubClassAsyncTaskWorker( CuCollector collector )
//	{
//		super( collector );
//	}
//
//	@Override
//	protected WorkerStatus refactor()
//	{
//		NUMBER_OF_ASYNC_TASKS = 0;
//		Map<ICompilationUnit, List<TypeDeclaration>> cuAnonymousClassesMap = collector.getCuSubclassesMap();
//		int numCunits = collector.getNumberOfCompilationUnits();
//		monitor.beginTask( getClass().getSimpleName(), numCunits );
//		RxLogger.info( this, "METHOD=refactor - Number of compilation units: " + numCunits );
//		for ( ICompilationUnit icu : cuAnonymousClassesMap.keySet() )
//		{
//			List<TypeDeclaration> declarations = cuAnonymousClassesMap.get( icu );
//			for ( TypeDeclaration asyncTaskDeclaration : declarations )
//			{
//
//				RxLogger.info( this, "METHOD=refactor - Extract Information from AsyncTask: " + icu.getElementName() );
//				AsyncTaskVisitor asyncTaskVisitor = new AsyncTaskVisitor();
//				asyncTaskDeclaration.accept( asyncTaskVisitor );
//				if ( asyncTaskVisitor.getDoInBackgroundBlock() == null )
//				{
//					NUMBER_OF_ABSTRACT_TASKS++;
//					continue;
//				}
//				AST ast = asyncTaskDeclaration.getAST();
//				SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, ast, getClass().getSimpleName() );
//				SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
//				updateUsage( collector.getCuRelevantUsagesMap(), ast, singleChangeWriter, asyncTaskDeclaration, icu );
//				singleChangeWriter.removeSuperClass( asyncTaskDeclaration );
//				RxLogger.info( this, "METHOD=refactor - Updating imports: " + icu.getElementName() );
//				updateImports( singleChangeWriter, asyncTaskVisitor );
//				createLocalObservable( singleChangeWriter, asyncTaskDeclaration, ast, asyncTaskVisitor );
//				addProgressBlock( ast, icu, asyncTaskVisitor, singleChangeWriter );
//				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
//
//				if ( asyncTaskVisitor.getOnPreExecuteBlock() != null )
//					updateonPreExecute( asyncTaskDeclaration, singleChangeWriter, ast, asyncTaskVisitor );
//
//				NUMBER_OF_ASYNC_TASKS++;
//				rxMultipleUnitsWriter.addCompilationUnit( icu );
//			}
//
//			monitor.worked( 1 );
//		}
//		RxLogger.info( this, "Number of AsynckTasks Subclass=  " + NUMBER_OF_ASYNC_TASKS );
//		RxLogger.info( this, "Number of Abstract AsynckTasks Subclass=  " + NUMBER_OF_ABSTRACT_TASKS );
//
//		return WorkerStatus.OK;
//	}
//
//	private void updateUsage( Map<ICompilationUnit, List<MethodInvocation>> cuRelevantUsagesMap, AST ast,
//			SingleUnitExtensionWriter SingleUnitExtensionWriter, TypeDeclaration asyncTaskDeclaration,
//			ICompilationUnit icuOuter )
//	{
//		for ( ICompilationUnit icu : cuRelevantUsagesMap.keySet() )
//		{
//			TypeDeclaration tyDec = ast.newTypeDeclaration();
//			SingleUnitExtensionWriter singleChangeWriterNew = null;
//			AST astInvoke = ast;
//			RxLogger.info( this, "METHOD=updateUsage - updating usage for class: " + icuOuter.getElementName() + " in "
//					+ icu.getElementName() );
//			for ( MethodInvocation methodInvoke : cuRelevantUsagesMap.get( icu ) )
//			{
//				if ( methodInvoke.getName().toString().equals( EXECUTE )
//						|| methodInvoke.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
//				{
//
//					if ( icuOuter.getElementName().equals( icu.getElementName() ) )
//						replaceCancel( cuRelevantUsagesMap, methodInvoke, icuOuter, ast );
//					else
//					{
//						String newName = ASTUtil.findParent( methodInvoke, TypeDeclaration.class ).getName().toString();
//						tyDec = ASTUtil.findParent( methodInvoke, TypeDeclaration.class );
//						astInvoke = tyDec.getAST();
//						replaceCancel( cuRelevantUsagesMap, methodInvoke, icu, astInvoke );
//						rxMultipleUnitsWriter.addCompilationUnit( icu );
//					}
//
//				}
//			}
//
//		}
//
//	}
//
//	/**
//	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
//	 */
//	void replaceCancel( Map<ICompilationUnit, List<MethodInvocation>> cuRelevantUsagesMap, MethodInvocation methodInvoke,
//			ICompilationUnit icu, AST astInvoke )
//	{
//		boolean isCancelPresent = false;
//		for ( MethodInvocation methodReference : cuRelevantUsagesMap.get( icu ) )
//		{
//			try
//			{
//				if ( methodInvoke.getExpression().toString().equals( methodReference.getExpression().toString() ) )
//				{
//					if ( methodReference.getName().toString().equals( CANCEL ) )
//					{
//						isCancelPresent = true;
//						RxLogger.info( this,
//								"METHOD=replaceCancel - updating cancel invocation for class: " + icu.getElementName() );
//						replaceExecuteWithSubscription( methodInvoke, icu, astInvoke );
//						updateCancelInvocation( icu, methodReference, astInvoke );
//
//					}
//				}
//			}
//			catch ( Exception e )
//			{
//
//			}
//		}
//		if ( !isCancelPresent )
//		{
//
//			replaceExecute( methodInvoke, icu, astInvoke );
//		}
//	}
//
//	/**
//	 * Method to refactor method invocation statements with name execute EX: new
//	 * Task().execute();
//	 */
//	void replaceExecute( MethodInvocation methodInvoke, ICompilationUnit icu, AST astInvoke )
//	{
//		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
//		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
//		astInvoke = methodInvoke.getAST();
//		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( astInvoke, methodInvoke );
//		if ( execute.getName().toString().equals( EXECUTE_ON_EXECUTOR ) )
//		{
//			execute.arguments().clear();
//		}
//		execute.setName( astInvoke.newSimpleName( ASYNC_METHOD_NAME ) );
//
//		MethodInvocation invocation = astInvoke.newMethodInvocation();
//		invocation.setExpression( execute );
//		invocation.setName( astInvoke.newSimpleName( SUBSCRIBE ) );
//		// ASTUtil.replaceInStatement(methodInvoke, invocation);
//		singleChangeWriter.replace( methodInvoke, invocation );
//		// singleChangeWriter.replace(methodInvoke, invocation);
//	}
//
//	/**
//	 * Method to refactor method invocation statements with name execute EX: new
//	 * Task().execute();
//	 */
//	void replaceExecuteWithSubscription( MethodInvocation methodInvoke, ICompilationUnit icu, AST astInvoke )
//	{
//		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
//		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
//
//		RxLogger.info( this, "METHOD=replaceExecuteWithSubscription - replace Execute With Subscription for class: "
//				+ icu.getElementName() );
//		createSubscriptionDeclaration( icu, methodInvoke );
//
//		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree( astInvoke, methodInvoke );
//		execute.setName( astInvoke.newSimpleName( ASYNC_METHOD_NAME ) );
//		MethodInvocation invocation = astInvoke.newMethodInvocation();
//		invocation.setExpression( execute );
//		invocation.setName( astInvoke.newSimpleName( SUBSCRIBE ) );
//
//		Assignment initSubscription = astInvoke.newAssignment();
//
//		initSubscription
//				.setLeftHandSide( astInvoke.newSimpleName( getVariableName( methodInvoke.getExpression() ) + SUBSCRIPTION ) );
//		initSubscription.setRightHandSide( invocation );
//		singleChangeWriter.replace( methodInvoke, initSubscription );
//	}
//
//	void createSubscriptionDeclaration( ICompilationUnit icu, MethodInvocation methodInvoke )
//	{
//		RxLogger.info( this, "METHOD=createSubscriptionDeclaration - create Subscription Declaration for class: "
//				+ icu.getElementName() );
//		TypeDeclaration tyDec = ASTUtil.findParent( methodInvoke, TypeDeclaration.class );
//		AST astInvoke = tyDec.getAST();
//		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
//		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
//		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
//		Expression e = methodInvoke.getExpression();
//
//		variable.setName( astInvoke.newSimpleName( getVariableName( e ) + SUBSCRIPTION ) );
//
//		FieldDeclaration subscription = astInvoke.newFieldDeclaration( variable );
//		subscription.setType( astInvoke.newSimpleType( astInvoke.newSimpleName( SUBSCRIPTION ) ) );
//		singleChangeWriter.addStatementToClass( subscription, tyDec );
//		singleChangeWriter.addImport( "rx.Subscription" );
//	}
//
//	private String getVariableName( Expression e )
//	{
//		if ( e instanceof FieldAccess )
//			return ( (FieldAccess) e ).getName().toString();
//		else
//			return e.toString();
//	}
//
//	/**
//	 * Add cancel method if AsyncTask.cacel() method was invoked
//	 */
//	private void updateCancelInvocation( ICompilationUnit icu, MethodInvocation methodInvoke, AST astInvoke )
//	{
//
//		SingleUnitExtensionWriter writerTemplate = new SingleUnitExtensionWriter( icu, astInvoke, getClass().getSimpleName() );
//		SingleUnitExtensionWriter singleChangeWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, writerTemplate );
//		RxLogger.info( this,
//				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + icu.getElementName() );
//		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
//		unSubscribe
//				.setExpression( astInvoke.newSimpleName( getVariableName( methodInvoke.getExpression() ) + SUBSCRIPTION ) );
//		unSubscribe.setName( astInvoke.newSimpleName( UN_SUBSCRIBE ) );
//		singleChangeWriter.replace( methodInvoke, unSubscribe );
//
//	}
//
//	private void updateImports( SingleUnitExtensionWriter rewriter, AsyncTaskVisitor asyncTaskVisitor )
//	{
//		rewriter.addImport( "rx.Observable" );
//		rewriter.addImport( "rx.schedulers.Schedulers" );
//		rewriter.addImport( "java.util.concurrent.Callable" );
//		if ( NUMBER_OF_ABSTRACT_TASKS == 0 )
//		{
//			rewriter.removeImport( "android.os.AsyncTask" );
//		}
//		rewriter.removeImport( "javax.swing.SwingWorker" );
//		if ( asyncTaskVisitor.getOnPostExecuteBlock() != null )
//		{
//			rewriter.addImport( "rx.functions.Action1" );
//			rewriter.removeImport( "java.util.concurrent.ExecutionException" );
//			rewriter.removeImport( "java.util.concurrent.TimeoutException" );
//
//		}
//		if ( asyncTaskVisitor.getDoInBackgroundBlock() != null )
//		{
//			rewriter.addImport( "rx.Subscriber" );
//			rewriter.addImport( "java.util.Arrays" );
//			rewriter.addImport( "rx.Subscription" );
//			rewriter.addImport( "rx.functions.Action1" );
//		}
//		if ( asyncTaskVisitor.getOnProgressUpdateBlock() != null )
//		{
//			rewriter.addImport( "java.util.Arrays" );
//		}
//		if ( asyncTaskVisitor.getOnCancelled() != null )
//		{
//			rewriter.addImport( "rx.functions.Action0" );
//		}
//	}
//
//	private void createLocalObservable( SingleUnitExtensionWriter rewriter, TypeDeclaration taskObject, AST ast,
//			AsyncTaskVisitor asyncTaskVisitor )
//	{
//		replacePublishInvocations( asyncTaskVisitor, rewriter );
//		String observableStatement = createObservable( asyncTaskVisitor );
//		MethodDeclaration getAsyncMethod = ASTNodeFactory.createMethodFromText( ast,
//				"public Observable<" + asyncTaskVisitor.getReturnedType() + "> getAsyncObservable( final "
//						+ asyncTaskVisitor.getParameters() + "){"
//						+ " return "
//						+ observableStatement + "}" );
//		rewriter.replaceStatement( asyncTaskVisitor.getDoInBackgroundmethod(), getAsyncMethod );
//
//		// Remove postExecute method
//		if ( asyncTaskVisitor.getOnPostExecuteBlock() != null )
//			rewriter.removeMethod( asyncTaskVisitor.getOnPostExecuteBlock().getParent(), taskObject );
//		// Remove updateProgress method
//		if ( asyncTaskVisitor.getOnProgressUpdateBlock() != null )
//			rewriter.removeMethod( asyncTaskVisitor.getOnProgressUpdateBlock().getParent(), taskObject );
//		// Remove onCancelled method
//		if ( asyncTaskVisitor.getOnCancelled() != null )
//			rewriter.removeMethod( asyncTaskVisitor.getOnCancelled().getParent(), taskObject );
//	}
//
//	/**
//	 * onPreExecute method will be invoked by getAsyncObservable block as a
//	 * first statement.
//	 * 
//	 * @override has to be removed and super() should be removed from
//	 *           onPreExecuteBlock
//	 * @param parent
//	 * @param singleChangeWriter
//	 * @param ast
//	 * @param asyncTaskVisitor
//	 */
//	private void updateonPreExecute( TypeDeclaration parent, SingleUnitExtensionWriter singleChangeWriter, AST ast,
//			AsyncTaskVisitor asyncTaskVisitor )
//	{
//
//		MethodDeclaration getAsyncMethod = ASTNodeFactory.createMethodFromText( ast,
//				"public onPreExecute(){" + asyncTaskVisitor.getOnPreExecuteBlock().toString() + "}" );
//		singleChangeWriter.replaceStatement( (MethodDeclaration) asyncTaskVisitor.getOnPreExecuteBlock().getParent(),
//				getAsyncMethod );
//
//	}
//
//	private String createObservable( AsyncTaskVisitor asyncTaskVisitor )
//	{
//
//		Block doInBackgroundBlock = asyncTaskVisitor.getDoInBackgroundBlock();
//		Block onPostExecuteBlock = asyncTaskVisitor.getOnPostExecuteBlock();
//		Block doProgressUpdate = asyncTaskVisitor.getOnProgressUpdateBlock();
//		Block onCancelled = asyncTaskVisitor.getOnCancelled();
//		String type = asyncTaskVisitor.getReturnedType().toString();
//		String postExecuteParameters = asyncTaskVisitor.getPostExecuteParameters();
//		removeSuperInvocations( asyncTaskVisitor );
//		RxObservableStringBuilder rxObservable = RxObservableStringBuilder.newObservable( type, doInBackgroundBlock,
//				SchedulerType.JAVA_MAIN_THREAD );
//		if ( doProgressUpdate != null )
//		{
//			if ( onPostExecuteBlock != null )
//				rxObservable.addDoOnNext( onPostExecuteBlock.toString(), postExecuteParameters,
//						asyncTaskVisitor.getPostExecuteType().toString(), false );
//		}
//		else
//		{
//			if ( onPostExecuteBlock != null )
//				rxObservable.addDoOnNext( onPostExecuteBlock.toString(), postExecuteParameters, type, false );
//		}
//		if ( onCancelled != null )
//		{
//			rxObservable.addDoOnCancelled( onCancelled );
//		}
//		Block preExec = asyncTaskVisitor.getOnPreExecuteBlock();
//		if ( preExec != null )
//		{
//			rxObservable.addDoOnPreExecute( preExec );
//		}
//		return rxObservable.build();
//	}
//
//	/**
//	 * Remove super method invocation from AsyncTask methods
//	 * 
//	 * @param asyncTaskVisitor
//	 */
//	private void removeSuperInvocations( AsyncTaskVisitor asyncTaskVisitor )
//	{
//		for ( SuperMethodInvocation methodInvocation : asyncTaskVisitor.getSuperClassMethodInvocation() )
//		{
//			Statement statement = (Statement) ASTUtil.findParent( methodInvocation, Statement.class );
//			statement.delete();
//		}
//	}
//
//	/**
//	 * If onUpdateProgressmethod exist method will add a new method to class
//	 */
//	private void addProgressBlock( AST ast, ICompilationUnit icu, AsyncTaskVisitor asyncTaskVisitor,
//			SingleUnitExtensionWriter singleChangeWriter )
//	{
//
//		if ( asyncTaskVisitor.getOnProgressUpdateBlock() != null )
//		{
//			TypeDeclaration tyDec = (TypeDeclaration) ASTUtil
//					.findParent( asyncTaskVisitor.getOnProgressUpdateBlock().getParent(), TypeDeclaration.class );
//			String newMethodString = createNewMethod( asyncTaskVisitor );
//			MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText( ast, newMethodString );
//			singleChangeWriter.addMethod( newMethod, tyDec );
//		}
//
//	}
//
//	/**
//	 * Method which will be created if onProgressUpdate method is present in
//	 * asyncTask
//	 * 
//	 * @param asyncTaskVisitor
//	 * @return
//	 */
//	private String createNewMethod( AsyncTaskVisitor asyncTaskVisitor )
//	{
//		Block doProgressUpdate = asyncTaskVisitor.getOnProgressUpdateBlock();
//		Type progressType = asyncTaskVisitor.getProgressType();
//		String progressParameters = asyncTaskVisitor.getProgressParameters();
//		String newSubscriber = RxSubscriberStringBuilder.newSubscriber( progressType.toString(), doProgressUpdate,
//				progressParameters );
//		return "private Subscriber<" + progressType.toString() + "[]> getRxUpdateSubscriber() { return " + newSubscriber
//				+ "}";
//	}
//
//	/**
//	 * Iterate on list of invocation of publishProgress
//	 * 
//	 * @param asynctaskVisitor
//	 * @param rewriter
//	 */
//	private void replacePublishInvocations( AsyncTaskVisitor asynctaskVisitor, SingleUnitExtensionWriter rewriter )
//	{
//		if ( !asynctaskVisitor.getPublishInvocations().isEmpty() )
//		{
//			for ( MethodInvocation publishInvocation : asynctaskVisitor.getPublishInvocations() )
//			{
//				List argumentList = publishInvocation.arguments();
//				AST ast = publishInvocation.getAST();
//				replacePublishInvocationsAux( publishInvocation, argumentList, ast, rewriter, asynctaskVisitor );
//			}
//		}
//	}
//
//	/**
//	 * Convert pubishProgress(parms) method to
//	 * getRxUpdateSubscriber(parms).subscribe()
//	 * 
//	 * @param publishInvocation
//	 * @param argumentList
//	 * @param ast
//	 */
//	private <T extends ASTNode> void replacePublishInvocationsAux( T publishInvocation, List argumentList, AST ast,
//			SingleUnitExtensionWriter rewriter, AsyncTaskVisitor asyncTaskVisitor )
//	{
//		ASTNode referenceStatement = publishInvocation;
//		String value = argumentList.toString().replace( "[", "" ).replace( "]", "" );
//		String newInvocation = "getRxUpdateSubscriber().onNext((" + asyncTaskVisitor.getProgressType()
//				+ "[])Arrays.asList(" + value + ").toArray())";
//		Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast,
//				newInvocation );
//		if ( !( publishInvocation instanceof Statement ) )
//		{
//			referenceStatement = ASTUtil.findParent( publishInvocation, Statement.class );
//		}
//		System.out.println( publishInvocation + " -> " + newStatement );
//		ASTUtil.replaceInStatement( publishInvocation, newStatement );
//	}
//}


