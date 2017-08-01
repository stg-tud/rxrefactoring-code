package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
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

	

	
	/**
	 * Creates a new builder that transforms AsyncTasks into observables
	 * using inner classes inner classes.
	 * 
	 * @param asyncTask The AsyncTask that should be transformed.
	 * @param writer The writer specifying the compilation unit where
	 * the async task resides.
	 */
	@SuppressWarnings("unchecked")
	public InnerClassBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer, String id) {
		super(asyncTask, writer);		
		this.id = id;
		
		/* 
		 * Build:
		 * 
		 * private class ObservableWrapper {
		 *     ASYNC TASK FIELDS
		 *     
		 *     ASYNC TASK METHODS
		 *    
		 *     public Observable<T> create() {
		 *         return new Observable ...
		 *     }
		 *     
		 * }
		 * 
		 * }
		 */ 
		
		//Define type: ObservableWrapper
		TypeDeclaration observableType = ast.newTypeDeclaration();
		observableType.setName(ast.newSimpleName(getTypeName()));
		observableType.setInterface(false);
		observableType.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		
		//Add field and additional method declarations
		asyncTask.getFieldDeclarations().forEach(observableType.bodyDeclarations()::add);
		asyncTask.getAdditionalMethodDeclarations().forEach(observableType.bodyDeclarations()::add);
		
		
		//Define method: create
		ObservableMethodBuilder builder = new ObservableMethodBuilder(asyncTask, writer, id);
		observableType.bodyDeclarations().add(builder.buildCreateMethod());
		
		node = observableType;
	}
	
	public InnerClassBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer) {
		this(asyncTask, writer, IdManager.getNextObservableId(writer.getUnit().getElementName()));
	}

	
	/* 
	 * 
	 * private class ObservableWrapper {
	 *     ASYNC TASK FIELDS
	 *     
	 *     ASYNC TASK METHODS
	 *    
	 *     public Observable<T> create() {
	 *         return new Observable ...
	 *     }
	 *     
	 *     //If needed
	 *     SUBSCRIBER METHOD DECLARATION
	 *  
	 * }
	 */ 
	public TypeDeclaration buildInnerClass() {
		return buildInnerClassWithSubscriber(new SubscriberBuilder(asyncTask, writer, getId()));
	} 
	
	public TypeDeclaration buildInnerClassWithSubscriber(SubscriberBuilder builder) {
		if (asyncTask.getOnProgressUpdateBlock() != null) {
			addSubscriber(builder);
		}
		
		return node;
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
		Objects.requireNonNull(builder);
		
		MethodDeclaration method = builder.buildGetSubscriber();
		node.bodyDeclarations().add(method);
		
		return this;		
	}
	
	/*
	 * Builds
	 * 
	 * new ObservableWrapper()
	 */
	public ClassInstanceCreation buildNewObservableWrapper() {
		ClassInstanceCreation constructor = ast.newClassInstanceCreation();
		constructor.setType(ast.newSimpleType(ast.newSimpleName(getTypeName())));
		
		return constructor;
	}
	
	/*
	 * Cases
	 * 
	 * I.
	 * new MyAsyncTask().execute(ARGS) --> new ObservableWrapper().create(ARGS).subscribe()
	 * 
	 * II.
	 * x = new MyAsyncTask();
	 * ... 
	 * x.execute();
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	
	public String getTypeName() {
		return RefactorNames.INNER_CLASS_TYPE_NAME + id;
	}

	
	public String getMethodName() {
		return RefactorNames.CREATE_OBSERVABLE_METHOD_NAME;
	}
	
	public String getId() {
		return id;
	}
	
	

}
