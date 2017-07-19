package de.tudarmstadt.rxrefactoring.ext.asynctask2.workers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask2.domain.ClassDetails;

/**
 * Description: Visitor to extract relevant information about AsyncTasks<br>
 * <ol>
 * <li>onPreExecute Block</li>
 * <li>doInBackground Block</li>
 * <li>onProgressUpdate Block</li>
 * <li>onPostExecute Block</li>
 * <li>returned type</li>
 * </ol>
 * Author: Grebiel Jose Ifill Brito, Ram<br>
 * Created: 11/11/2016
 */
public class AsyncTaskVisitor extends ASTVisitor {

	private static final String DO_IN_BACKGROUND = "doInBackground";
	private static final String ON_POST_EXECUTE = "onPostExecute";
	private static final String ON_PRE_EXECUTE = "onPreExecute";
	private static final String ON_CANCELLED = "onCancelled";
	private static final String ON_PROGRESS_UPDATE = "onProgressUpdate";
	private static final String PUBLISH = "publishProgress";
	private static boolean HAS_FIELD = false;

	private Block doInBackgroundBlock;
	private Block onPostExecuteBlock;
	private Block onPreExecuteBlock;
	private Block onProgressUpdateBlock;
	private Block onCancelled = null;

	private Type returnedType;
	private Type progressType;
	private String parameters;
	private String progressParameters;
	private String postExecuteParameters;
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
				returnedType = methodDeclaration.getReturnType2();
				parameters = methodDeclaration.parameters().toString().replace("[", "").replace("]", "");
				isVoid = (parameters == null ? false : (parameters.contains("Void") ? true : false));
				doInBackgroundMethod = methodDeclaration;
			} else if (ON_POST_EXECUTE.equals(methodDeclarationName)) {
				onPostExecuteBlock = node;
				postExecuteType = ((SingleVariableDeclaration) methodDeclaration.parameters().get(0)).getType();
				postExecuteParameters = ((SingleVariableDeclaration) methodDeclaration.parameters().get(0)).getName()
						.toString();
			} else if (ON_PRE_EXECUTE.equals(methodDeclarationName)) {
				onPreExecuteBlock = node;
			} else if (ON_PROGRESS_UPDATE.equals(methodDeclarationName)) {
				onProgressUpdateBlock = node;
				progressType = ((SingleVariableDeclaration) methodDeclaration.parameters().get(0)).getType();
				progressParameters = ((SingleVariableDeclaration) methodDeclaration.parameters().get(0)).getName()
						.toString();
			} else if (ON_CANCELLED.equals(methodDeclarationName)) {
				onCancelled = node;
			}
		}
		return true;
	}

	/**
	 * @return the isVoid
	 */
	public Boolean getIsVoid() {
		return isVoid;
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

	public String getParameters() {
		return parameters;
	}

	public Block getDoInBackgroundBlock() {
		return doInBackgroundBlock;
	}

	public Block getOnPostExecuteBlock() {
		return onPostExecuteBlock;
	}

	public Type getReturnedType() {
		if (returnedType == null) {
			System.out.println("ERORR");
		}
		return returnedType;
	}

	public MethodDeclaration getDoInBackgroundmethod() {
		return doInBackgroundMethod;
	}

	public Block getOnPreExecuteBlock() {
		return onPreExecuteBlock;
	}

	public Block getOnProgressUpdateBlock() {
		return onProgressUpdateBlock;
	}

	public String getProgressParameters() {
		return progressParameters;
	}

	public String getPostExecuteParameters() {
		return postExecuteParameters;
	}

	public Type getProgressType() {
		return progressType;
	}

	/**
	 * @return the postExecuteType
	 */
	public Type getPostExecuteType() {
		return postExecuteType;
	}

	public List<SuperMethodInvocation> getSuperClassMethodInvocation() {
		return superClassMethodInvocation;
	}

	public void setDoInBackGround(Block updatedDoInBackgroundBlock) {
		doInBackgroundBlock = updatedDoInBackgroundBlock;

	}

	/**
	 * @return the onCancelled
	 */
	public Block getOnCancelled() {
		return onCancelled;
	}

	/**
	 * @return the publishInvocations
	 */
	public List<MethodInvocation> getPublishInvocations() {
		return publishInvocations;
	}

	/**
	 * @return the hAS_FIELD
	 */
	public boolean hasField() {
		return !fieldDeclarations.isEmpty() || !additionalMethodDeclarations.isEmpty()
				|| !(getOnProgressUpdateBlock() == null);
	}

	public List<FieldDeclaration> getFieldDeclarations() {
		// TODO Auto-generated method stub
		return fieldDeclarations;
	}

	public List<MethodDeclaration> getAdditionalMethodDeclarations() {
		// TODO Auto-generated method stub
		return additionalMethodDeclarations;
	}

	public void setDoOnCompletedBlock(Block doOnCompleted) {
		onPostExecuteBlock = doOnCompleted;

	}

}
