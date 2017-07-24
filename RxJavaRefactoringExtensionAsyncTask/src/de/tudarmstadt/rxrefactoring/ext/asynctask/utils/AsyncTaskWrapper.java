package de.tudarmstadt.rxrefactoring.ext.asynctask.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.*;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.ClassDetails;

/**
 * Wraps the declaration of an AsyncTask class in order to extract information
 * about implemented methods. This includes: <br>
 * <ol>
 * <li>onPreExecute Block</li>
 * <li>doInBackground Block</li>
 * <li>onProgressUpdate Block</li>
 * <li>onPostExecute Block</li>
 * <li>returned type</li>
 * </ol>
 * 
 * @author Grebiel Jose Ifill Brito, Ram, Mirko Köhler
 * 
 * @since 11/11/2016
 */
public class AsyncTaskWrapper extends ASTVisitor {

	private static final String DO_IN_BACKGROUND = "doInBackground";
	private static final String ON_POST_EXECUTE = "onPostExecute";
	private static final String ON_PRE_EXECUTE = "onPreExecute";
	private static final String ON_CANCELLED = "onCancelled";
	private static final String ON_PROGRESS_UPDATE = "onProgressUpdate";
	private static final String PUBLISH = "publishProgress";

	/*
	 * This visitor retrieves and stores the relevant data of the AsyncTask.
	 */
	private final AsyncTaskVisitor visitor;

	/**
	 * Creates a new wrapper given the class declaration of a class that is an
	 * AsyncTask.
	 * 
	 * @param declaration
	 *            (Sub-)class of an AsyncTask.
	 */
	public AsyncTaskWrapper(ASTNode declaration) {
		if (!(declaration instanceof TypeDeclaration || declaration instanceof AnonymousClassDeclaration))
			throw new IllegalArgumentException("Can only wrap around TypeDeclaration or AnonymousClassDeclaration, but got " + declaration);
		
		visitor = new AsyncTaskVisitor();
		declaration.accept(visitor);
	}

	/**
	 * Visits the relevant parts of the AsyncTask declaration.
	 * 
	 * @author Mirko Köhler
	 *
	 */
	private class AsyncTaskVisitor extends ASTVisitor {

		private Block doInBackgroundBlock;
		private Block onPostExecuteBlock;
		private Block onPreExecuteBlock;
		private Block onProgressUpdateBlock;
		private Block onCancelled = null;

		private Type returnType;

		private String parameters;
		private SingleVariableDeclaration progressParameter;
		private SingleVariableDeclaration postExecuteParameter;
		private MethodDeclaration doInBackgroundMethod;
		private Type postExecuteType;
		private List<MethodInvocation> publishInvocations = new ArrayList<>();
		private List<SuperMethodInvocation> superClassMethodInvocation = new ArrayList<>();
		private Boolean isVoid;
		// for "stateful" classes
		private List<FieldDeclaration> fieldDeclarations = new ArrayList<>();
		private List<MethodDeclaration> additionalMethodDeclarations = new ArrayList<>();

		@Override
		public boolean visit(Block node) {
			ASTNode parent = node.getParent();
			// Method which overloads AsyncTask method should have at least 2
			// modifiers they are override and protectedÏ
			if (parent instanceof MethodDeclaration && ((MethodDeclaration) parent).modifiers().size() > 1) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) parent;
				String methodDeclarationName = methodDeclaration.getName().toString();
				if (DO_IN_BACKGROUND.equals(methodDeclarationName)) {
					doInBackgroundBlock = node;
					returnType = methodDeclaration.getReturnType2();
					parameters = methodDeclaration.parameters().toString().replace("[", "").replace("]", "");
					isVoid = (parameters == null ? false : (parameters.contains("Void") ? true : false));
					doInBackgroundMethod = methodDeclaration;
				} else if (ON_POST_EXECUTE.equals(methodDeclarationName)) {
					onPostExecuteBlock = node;
					postExecuteType = ((SingleVariableDeclaration) methodDeclaration.parameters().get(0)).getType();
					postExecuteParameter = (SingleVariableDeclaration) methodDeclaration.parameters().get(0);
				} else if (ON_PRE_EXECUTE.equals(methodDeclarationName)) {
					onPreExecuteBlock = node;
				} else if (ON_PROGRESS_UPDATE.equals(methodDeclarationName)) {
					onProgressUpdateBlock = node;
					progressParameter = (SingleVariableDeclaration) methodDeclaration.parameters().get(0);
				} else if (ON_CANCELLED.equals(methodDeclarationName)) {
					onCancelled = node;
				}
			}
			return true;
		}

		@Override
		public boolean visit(MethodInvocation node) {
			if (ASTUtils.matchesTargetMethod(node, PUBLISH, ClassDetails.ASYNC_TASK.getBinaryName())) {
				publishInvocations.add(node);

			}
			return true;
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			fieldDeclarations.add(node);
			return true;
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			String methodDeclarationName = node.getName().toString();
			if (!DO_IN_BACKGROUND.equals(methodDeclarationName) && !ON_POST_EXECUTE.equals(methodDeclarationName)
					&& !ON_PRE_EXECUTE.equals(methodDeclarationName) && !ON_CANCELLED.equals(methodDeclarationName)
					&& !ON_PROGRESS_UPDATE.equals(methodDeclarationName)) {
				additionalMethodDeclarations.add(node);
			}
			return true;
		}

		@Override
		public boolean visit(SuperMethodInvocation node) {
			if (ASTUtils.matchesTargetMethod(node, "onCancelled", ClassDetails.ASYNC_TASK.getBinaryName())) {
				superClassMethodInvocation.add(node);
			}
			
			if (ASTUtils.matchesTargetMethod(node, "onPreExecute", ClassDetails.ASYNC_TASK.getBinaryName())) {
				superClassMethodInvocation.add(node);
			}
			
			if (ASTUtils.matchesTargetMethod(node, "onPostExecute", ClassDetails.ASYNC_TASK.getBinaryName())) {
				superClassMethodInvocation.add(node);
			}
			
			if (ASTUtils.matchesTargetMethod(node, "onProgressUpdate", ClassDetails.ASYNC_TASK.getBinaryName())) {
				superClassMethodInvocation.add(node);
			}
			return true;
		}
	}

	/**
	 * @return the isVoid
	 */
	public Boolean getIsVoid() {
		return visitor.isVoid;
	}

	public String getParameters() {
		return visitor.parameters;
	}

	public Block getDoInBackgroundBlock() {
		return visitor.doInBackgroundBlock;
	}

	public Block getOnPostExecuteBlock() {
		return visitor.onPostExecuteBlock;
	}

	/**
	 * Gets the return type of the doInBackground method.
	 * 
	 * @return The type of the doInBackground method, or null if the type could not
	 *         be resolved.
	 */
	public Type getReturnType() {		
		return visitor.returnType;
	}

	public MethodDeclaration getDoInBackgroundmethod() {
		return visitor.doInBackgroundMethod;
	}

	public Block getOnPreExecuteBlock() {
		return visitor.onPreExecuteBlock;
	}

	public Block getOnProgressUpdateBlock() {
		return visitor.onProgressUpdateBlock;
	}

	public SingleVariableDeclaration getProgressParameter() {
		return visitor.progressParameter;
	}

	public SingleVariableDeclaration getPostExecuteParameter() {
		return visitor.postExecuteParameter;
	}

	
	/**
	 * @return the postExecuteType
	 */
	public Type getPostExecuteType() {
		return visitor.postExecuteType;
	}

	public List<SuperMethodInvocation> getSuperClassMethodInvocation() {
		return visitor.superClassMethodInvocation;
	}

	public void setDoInBackGround(Block updatedDoInBackgroundBlock) {
		visitor.doInBackgroundBlock = updatedDoInBackgroundBlock;
	}

	/**
	 * @return the onCancelled
	 */
	public Block getOnCancelled() {
		return visitor.onCancelled;
	}

	/**
	 * @return the publishInvocations
	 */
	public List<MethodInvocation> getPublishInvocations() {
		return visitor.publishInvocations;
	}

	/**
	 * Checks whether the AsyncTask has additional functionality, i.e. extra fields, 
	 * method declarations, or a progress update (= intermediate results).
	 * 
	 * @return True, if the AsyncTask has additional functionality.
	 */
	public boolean hasAdditionalAccess() {
		return !visitor.fieldDeclarations.isEmpty() || !visitor.additionalMethodDeclarations.isEmpty()
				|| getOnProgressUpdateBlock() != null;
	}

	public List<FieldDeclaration> getFieldDeclarations() {
		return visitor.fieldDeclarations;
	}

	public List<MethodDeclaration> getAdditionalMethodDeclarations() {
		return visitor.additionalMethodDeclarations;
	}

	public void setDoOnCompletedBlock(Block doOnCompleted) {
		visitor.onPostExecuteBlock = doOnCompleted;

	}

}
