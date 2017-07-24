package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

/**
 * The builder is used to create observables from AsyncTasks as
 * inner classes.
 *  
 * @author Mirko Köhler
 *
 */
public class InnerClassBuilder extends AbstractBuilder<TypeDeclaration> {

	//Unique id that gets added to the name to avoid naming conflicts.
	private final String id;

	
	private final String TYPE_NAME = "ComplexRxObservable";
	private final String METHOD_NAME = "getAsyncObservable";
	
	/**
	 * Creates a new builder that transforms AsyncTasks into observables
	 * using inner classes inner classes.
	 * 
	 * @param asyncTask The AsyncTask that should be transformed.
	 * @param writer The writer specifying the compilation unit where
	 * the async task resides.
	 */
	public InnerClassBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer) {
		super(asyncTask, writer);		
		id = IdManager.getNextObservableId(writer.getUnit().getElementName());
	}

	
	/* 
	 * 
	 * private class ComplexRxObservable {
	 *     ASYNC TASK FIELDS
	 *     
	 *     ASYNC TASK METHODS
	 *    
	 *     public Observable<T> getAsyncObservable() {
	 *         return new Observable ...
	 *     }
	 *     
	 *     //If needed
	 *     SUBSCRIBER METHOD DECLARATION
	 * }
	 * 
	 * }
	 */ 
	/**
	 * Creates an observable type declaration that mirrors the
	 * functionality of the given AsyncTask.
	 */
	@Override public TypeDeclaration create() {			
		return create(new SubscriberBuilder(asyncTask, writer));
	}
	
	
	public TypeDeclaration create(SubscriberBuilder builder) {
		if (asyncTask.getOnProgressUpdateBlock() != null) {
			addSubscriber(builder);
		}
		
		return node;
	}
	

	/**
	 * Creates an observable type declaration that mirrors the
	 * functionality of the given AsyncTask.
	 */
	public static TypeDeclaration from(AsyncTaskWrapper asyncTask, UnitWriter writer) {
		InnerClassBuilder builder = new InnerClassBuilder(asyncTask, writer);
		return builder.create();
	}
	
	/* 
	 * 
	 * private class ComplexRxObservable {
	 *     ASYNC TASK FIELDS
	 *     
	 *     ASYNC TASK METHODS
	 *    
	 *     public Observable<T> getAsyncObservable() {
	 *         return new Observable ...
	 *     }
	 *     
	 *     //If needed
	 *     SUBSCRIBER METHOD DECLARATION
	 * }
	 * 
	 * }
	 */ 
	@SuppressWarnings("unchecked")
	@Override TypeDeclaration initial() {
	
		//Define type: ComplexRxObservable
		TypeDeclaration observableType = ast.newTypeDeclaration();
		observableType.setName(ast.newSimpleName(getTypeName()));
		observableType.setInterface(false);
		observableType.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		
		//Add field and additional method declarations
		asyncTask.getFieldDeclarations().forEach(observableType.bodyDeclarations()::add);
		asyncTask.getAdditionalMethodDeclarations().forEach(observableType.bodyDeclarations()::add);
		
		//Define method: getAsyncObservable
		MethodDeclaration getAsyncObservable = ast.newMethodDeclaration();
		getAsyncObservable.setName(ast.newSimpleName("getAsyncObservable" + id));
		//Construct return type Observable<T>
		ParameterizedType returnType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Observable")));
		returnType.typeArguments().add(copy(returnTypeOrVoid()));
		getAsyncObservable.setReturnType2(returnType);
		
		getAsyncObservable.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		getAsyncObservable.modifiers().add(createOverrideAnnotation());
		
		//Build body of getAsyncObservable
		Block getAsyncObservableBody = ast.newBlock();
		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(AnonymousClassBuilder.from(asyncTask, writer));		
		getAsyncObservableBody.statements().add(returnStatement);
		getAsyncObservable.setBody(getAsyncObservableBody);
		
				
		return observableType;
	}
	
	/*
	 * Builds
	 * 
	 * private Subscriber<T> getRxUpdateSubscriberX() {
	 *   return SUBSCRIBER
	 * };
	 */
	@SuppressWarnings("unchecked")
	public InnerClassBuilder addSubscriber(SubscriberBuilder builder) {				
		
		MethodDeclaration method = builder.create();
		node.bodyDeclarations().add(method);
		
		return this;		
	}
	
	
	public String getTypeName() {
		return TYPE_NAME + id;
	}

	
	public String getMethodName() {
		return METHOD_NAME + id;
	}
	
	public String getId() {
		return id;
	}
	
	

}
