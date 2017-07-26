package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import java.util.Objects;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

public class ObservableMethodBuilder extends AbstractBuilder<MethodDeclaration> {
	
	private final String id;

	public ObservableMethodBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer, String id) {
		super(asyncTask, writer);
		Objects.requireNonNull(id);
		this.id = id;
		
		node = afterInitial();	
	}
	
	public ObservableMethodBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer) {
		this(asyncTask, writer, IdManager.getNextObservableId(writer.getUnit().getElementName()));
	}
		
	@Override
	public MethodDeclaration create() {
		return node;
	}
	

	
	//Ignore this method. Initializer should be called after id has been set in the constructor.
	@Override
	MethodDeclaration initial() {
		return null;
	}
	
	/*    
	 * Builds
	 *  
	 * public Observable<T> getAsyncObservable() {
	 *   return new Observable ...
	 * }
	 */
	@SuppressWarnings("unchecked")
	private MethodDeclaration afterInitial() {
		//Define method: getAsyncObservable
		MethodDeclaration getAsyncObservable = ast.newMethodDeclaration();
		getAsyncObservable.setName(ast.newSimpleName("getAsyncObservable" + id));
		//Construct return type Observable<T>
		ParameterizedType returnType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Observable")));
		returnType.typeArguments().add(copy(asyncTask.getResultTypeOrVoid()));
		getAsyncObservable.setReturnType2(returnType);		
		getAsyncObservable.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
		//Add arguments of doInBackground to getAsyncObservable. Add 'final' to each parameter.
		MethodDeclaration doInBackground = asyncTask.getDoInBackgroundmethod();
		
		doInBackground.parameters().forEach(parameter -> {
			SingleVariableDeclaration variable = (SingleVariableDeclaration) parameter;
			
			ListRewrite modifierRewrite = astRewrite.getListRewrite(variable, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			modifierRewrite.insertFirst(ast.newModifier(ModifierKeyword.FINAL_KEYWORD), null);		
		
			getAsyncObservable.parameters().add(copy(variable));
			
			return;
		});
		
		//Build body of getAsyncObservable
		Block getAsyncObservableBody = ast.newBlock();
		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(AnonymousClassBuilder.from(asyncTask, writer));		
		getAsyncObservableBody.statements().add(returnStatement);
		getAsyncObservable.setBody(getAsyncObservableBody);
		
		return getAsyncObservable;
	}

}
