package de.tudarmstadt.rxrefactoring.ext.asynctask.builders;

import java.util.Objects;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

public class ObservableMethodBuilder extends AbstractBuilder {
	
	private final String id;
	
	public ObservableMethodBuilder(AsyncTaskWrapper asyncTask, String id) {
		super(asyncTask);
		Objects.requireNonNull(id);
		this.id = id;	
	}
	
	public ObservableMethodBuilder(AsyncTaskWrapper asyncTask) {
		this(asyncTask, IdManager.getNextObservableId(asyncTask.getUnit().getElementName()));
	}
		
		
	/*    
	 * Builds
	 *  
	 * public Observable<T> create() {
	 *   return new Observable ...
	 * }
	 */
	@SuppressWarnings("unchecked")
	public MethodDeclaration buildCreateMethod(String superClassName) {
		//Define method: create
		MethodDeclaration method = ast.newMethodDeclaration();
		method.setName(ast.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));
		//Construct return type Observable<T>
		ParameterizedType returnType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Observable")));
		returnType.typeArguments().add(unit.copyNode(asyncTask.getResultTypeOrVoid()));
		method.setReturnType2(returnType);		
		method.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
		//Add arguments of doInBackground to create. 
		MethodDeclaration doInBackground = asyncTask.getDoInBackground();
		
		//Add 'final' to each parameter.
		doInBackground.parameters().forEach(parameter -> {
			SingleVariableDeclaration variable = (SingleVariableDeclaration) parameter;
						
			//Adds final modifier (if not already present) so that parameters can be referenced in the anonymous class.
			if (!hasFinalModifier(variable)) {
				ListRewrite modifierRewrite = unit.getListRewrite(variable, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
				modifierRewrite.insertFirst(ast.newModifier(ModifierKeyword.FINAL_KEYWORD), null);
			}
					
			method.parameters().add(unit.copyNode(variable));
			
			return;
		});
		
		//Build body of create
		Block methodBody = ast.newBlock();
		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(AnonymousClassBuilder.from(asyncTask, superClassName));		
		methodBody.statements().add(returnStatement);
		method.setBody(methodBody);
		
		return method;
	}
	
	private boolean hasFinalModifier(SingleVariableDeclaration variable) {
		for (Object element : variable.modifiers()) {
			if (element instanceof Modifier && ((Modifier) element).isFinal()) {				
				return true;				
			}			
		}
		return false;
	}
	
	public String getId() {
		return id;
	}

}
