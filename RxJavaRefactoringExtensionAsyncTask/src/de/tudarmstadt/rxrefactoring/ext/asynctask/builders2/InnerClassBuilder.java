package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
public class InnerClassBuilder extends AbstractBuilder {

	private TypeDeclaration node;
	
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
		
		/* 
		 * Build:
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
		
		//Define type: ComplexRxObservable
		TypeDeclaration observableType = ast.newTypeDeclaration();
		observableType.setName(ast.newSimpleName(getTypeName()));
		observableType.setInterface(false);
		observableType.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		
		//Add field and additional method declarations
		asyncTask.getFieldDeclarations().forEach(observableType.bodyDeclarations()::add);
		asyncTask.getAdditionalMethodDeclarations().forEach(observableType.bodyDeclarations()::add);
		
		
		//Define method: getAsyncObservable
		ObservableMethodBuilder builder = new ObservableMethodBuilder(asyncTask, writer, id);
		observableType.bodyDeclarations().add(builder.create());
		
		node = observableType;
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
	public TypeDeclaration create() {			
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
