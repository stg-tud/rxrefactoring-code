package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import java.util.Objects;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;
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
	private String name;
	
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
	public InnerClassBuilder(AsyncTaskWrapper asyncTask, String id) {
		super(asyncTask);		
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
		this.name = asyncTask.doIfTypeDeclaration(t -> t.getName().getIdentifier(), () -> RefactorNames.INNER_CLASS_TYPE_NAME);
		
		TypeDeclaration observableType = ast.newTypeDeclaration();	
		
		observableType.setName(ast.newSimpleName(getTypeName()));
		
		observableType.setInterface(false);
		
		asyncTask.getModifiers().forEach(x -> {
			if (((IExtendedModifier) x).isModifier()) {
				observableType.modifiers().add(unit.copyNode((ASTNode) x));
			}
		});
		//observableType.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		
		//Add field and additional method declarations
		asyncTask.getFieldDeclarations().forEach(x -> observableType.bodyDeclarations().add(unit.copyNode(x)));
		asyncTask.getAdditionalMethodDeclarations().forEach(x -> observableType.bodyDeclarations().add(unit.copyNode(x)));
		
		
		//Define method: create
		ObservableMethodBuilder builder = new ObservableMethodBuilder(asyncTask, id);
		observableType.bodyDeclarations().add(builder.buildCreateMethod());
		
		node = observableType;
	}
	
	public InnerClassBuilder(AsyncTaskWrapper asyncTask) {
		this(asyncTask, IdManager.getNextObservableId(asyncTask.getUnit().getElementName()));
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
		return buildInnerClassWithSubscriber(new SubscriberBuilder(asyncTask, getId()));
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
	public ClassInstanceCreation buildNewObservableWrapper(AST ast) {
		ClassInstanceCreation constructor = ast.newClassInstanceCreation();
		constructor.setType(ast.newSimpleType(ast.newSimpleName(getTypeName())));
		
		return constructor;
	}
	
	public ClassInstanceCreation buildNewObservableWrapper() {
		return buildNewObservableWrapper(ast);
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
	
//	public void replaceClassInstanceCreationsIn() {
//		AST ast = writer.getAST();
//	}
	
	
	
	public String getTypeName() {
		//return RefactorNames.INNER_CLASS_TYPE_NAME + id;
		return name;
	}

	
	public String getMethodName() {
		return RefactorNames.CREATE_OBSERVABLE_METHOD_NAME;
	}
	
	public String getId() {
		return id;
	}
	
	

}
