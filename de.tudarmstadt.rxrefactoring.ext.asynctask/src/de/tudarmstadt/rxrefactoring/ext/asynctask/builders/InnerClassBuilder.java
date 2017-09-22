package de.tudarmstadt.rxrefactoring.ext.asynctask.builders;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

/**
 * The builder is used to create observables from AsyncTasks as
 * inner classes.
 *  
 * @author Mirko Köhler
 *
 */
public class InnerClassBuilder extends AbstractBuilder {

	private final TypeDeclaration node;
	private final String name;
	
	//Unique id that gets added to the name to avoid naming conflicts.
	private final String id;

	//Variables that should be added to the constructor.
	private final List<IVariableBinding> variables = Lists.newLinkedList();

	
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
		
		//Set name of this innerclass
		this.name = asyncTask.mapDeclaration(
				//Keep name if it is a type declaration
				type -> type.getName().getIdentifier(),
				//type -> RefactorNames.INNER_CLASS_TYPE_NAME, //TODO: Do not change class name if there is no need to
				//Use default name if it is an anonymous class declaration
				anon -> RefactorNames.INNER_CLASS_TYPE_NAME
			);
		
		
		//Sets the variables that are found in the enclosing block of the AsyncTask
		setEnclosingVariables();
		
		//Define type: ObservableWrapper		
		TypeDeclaration observableType = ast.newTypeDeclaration();		
		observableType.setName(ast.newSimpleName(getTypeName()));		
		observableType.setInterface(false);
		
		//Keep the same modifiers as the original class
		asyncTask.getModifiers().forEach(x -> {
			if (((IExtendedModifier) x).isModifier()) {
				observableType.modifiers().add(unit.copyNode((ASTNode) x));
			}
		});
		
		//Retain interface implementations
		asyncTask.doWithDeclaration(
				type -> type.superInterfaceTypes().forEach(x -> observableType.superInterfaceTypes().add(unit.copyNode((Type) x))),
				anon -> {}
		);		
		
		//Add field declarations from the original class
		asyncTask.getFieldDeclarations().forEach(x -> observableType.bodyDeclarations().add(unit.copyNode(x)));
		
		
		//Add variable declarations to the constructor (and add constructor)
		MethodDeclaration constructor = ast.newMethodDeclaration();
		constructor.setConstructor(true);
		constructor.setName(ast.newSimpleName(name));
		constructor.setBody(ast.newBlock());
		//Add a constructor argument, field, and an assignment for each enclosing variable
		for (IVariableBinding variable : variables)  {			
			
			//Add field declaration
			FieldDeclaration field = ast.newFieldDeclaration(newVariableDeclarationFragmentFrom(variable));
			field.setType(ASTUtils.typeFromBinding(ast, variable.getType()));
			observableType.bodyDeclarations().add(field);
			
			
			//Add constructor parameter
			constructor.parameters().add(newSingleVariableDeclarationFrom(variable));
			
			
			//Add assignment in constructor body
			Assignment assignment = ast.newAssignment();			
			FieldAccess access = ast.newFieldAccess();
			access.setExpression(ast.newThisExpression());
			access.setName(ast.newSimpleName(variable.getName()));
			//this.field = field;
			assignment.setLeftHandSide(access);
			assignment.setRightHandSide(ast.newSimpleName(variable.getName()));
			
			constructor.getBody().statements().add(ast.newExpressionStatement(assignment));	
		}
		
		//Add constructor to type declaration
		if (variables.size() > 0)
			observableType.bodyDeclarations().add(constructor);
		
		//Add method declarations from the original type (below the constructor)
		asyncTask.getAdditionalMethodDeclarations().forEach(declaration -> {
			//Remove override annotations
			removeOverride(declaration);
			
			observableType.bodyDeclarations().add(unit.copyNode(declaration));			
		});
		
		
		//Define method: create
		ObservableMethodBuilder builder = new ObservableMethodBuilder(asyncTask, id);
		observableType.bodyDeclarations().add(builder.buildCreateMethod(getTypeName()));
		
		node = observableType;
	}
	
	@SuppressWarnings("unchecked")
	private void removeOverride(MethodDeclaration declaration) {		
		declaration.modifiers().forEach(element -> {
			if (element instanceof Annotation && ((Annotation) element).getTypeName().getFullyQualifiedName().equals("Override")) 
				unit.getListRewrite(declaration, MethodDeclaration.MODIFIERS2_PROPERTY).remove((ASTNode) element, null);
		});
	}
	
	private SingleVariableDeclaration newSingleVariableDeclarationFrom(IVariableBinding binding) {
		SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
		var.setName(ast.newSimpleName(binding.getName()));	
		var.setType(ASTUtils.typeFromBinding(ast, binding.getType()));
		
		return var;
	}
	
	private VariableDeclarationFragment newVariableDeclarationFragmentFrom(IVariableBinding binding) {
		VariableDeclarationFragment var = ast.newVariableDeclarationFragment();
		var.setName(ast.newSimpleName(binding.getName()));
		
		return var;
	}
	
	
	
	
	public InnerClassBuilder(AsyncTaskWrapper asyncTask) {
		this(asyncTask, IdManager.getNextObservableId(asyncTask.getUnit()));
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
		if (asyncTask.getOnProgressUpdate() != null) {
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
		node.bodyDeclarations().add(builder.buildSubscriberDeclaration());
		node.bodyDeclarations().add(method);
		
		return this;		
	}
	
	/*
	 * Builds
	 * 
	 * new ObservableWrapper()
	 */
	@SuppressWarnings("unchecked")
	public ClassInstanceCreation buildNewObservableWrapper(AST ast) {
		ClassInstanceCreation constructor = ast.newClassInstanceCreation();
		constructor.setType(ast.newSimpleType(ast.newSimpleName(getTypeName())));
		
		for (IVariableBinding variable : variables) {
			constructor.arguments().add(ast.newSimpleName(variable.getName()));
		}
		
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
	
	//Finds variables that are defined in the enclosing block of the AsyncTask
	//and adds them to the variables field.
	private void setEnclosingVariables() {
		BodyDeclaration enclosingBody = asyncTask.getEnclosingDeclaration();
		if (enclosingBody instanceof MethodDeclaration) {
			List<?> statements = ((MethodDeclaration) enclosingBody).getBody().statements();
			
			for (Object element : statements) {
				Statement statement = (Statement) element;
				
				class VariableDeclarationVisitor extends ASTVisitor {
					
					boolean continueVisit = true;
					
					public boolean visit(Block node) {
						return false;
					}
										
					
					public boolean preVisit2(ASTNode node) {
						if (node instanceof VariableDeclaration) {
							IVariableBinding binding = ((VariableDeclaration) node).resolveBinding();
							if (binding != null) {
								variables.add(binding);
							}
						}
						
						if (continueVisit && ASTUtils.containsNode(statement, n -> Objects.equals(n, asyncTask.getDeclaration()))) {
							continueVisit = false;					
						}
						
						return continueVisit;				
					}
				}
				
				VariableDeclarationVisitor v = new VariableDeclarationVisitor();
				statement.accept(v);
				if (!v.continueVisit) {
					break;
				}				
			}
			
		}
	}
	
	

}
