package de.tudarmstadt.rxrefactoring.ext.asynctask.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
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
public class AsyncTaskWrapper {

	private static final String DO_IN_BACKGROUND = "doInBackground";
	private static final String ON_POST_EXECUTE = "onPostExecute";
	private static final String ON_PRE_EXECUTE = "onPreExecute";
	private static final String ON_CANCELLED = "onCancelled";
	private static final String ON_PROGRESS_UPDATE = "onProgressUpdate";
	private static final String PUBLISH = "publishProgress";

	/**
	 * This visitor retrieves and stores the relevant data of the AsyncTask.
	 */
	private final AsyncTaskVisitor visitor;
	
	/**
	 * The root node of the declaration of the AsyncTask. Can either be a TypeDeclaration
	 * or an AnonymousClassDeclaration.
	 */
	private final ASTNode declaration;
	
	/**
	 * The compilation unit that contains the declaration. 
	 */
	private final RewriteCompilationUnit unit;

	/**
	 * Creates a new wrapper given the class declaration of a class that is an
	 * AsyncTask.
	 * 
	 * @param declaration
	 *            (Sub-)class of an AsyncTask.
	 *            
	 * @throws NullPointerException if either argument is null.
	 */
	public AsyncTaskWrapper(ASTNode declaration, RewriteCompilationUnit unit) {
		Objects.requireNonNull(declaration);
		Objects.requireNonNull(unit);
		
		if (!(declaration instanceof TypeDeclaration || declaration instanceof AnonymousClassDeclaration))
			throw new IllegalArgumentException("Can only wrap around TypeDeclaration or AnonymousClassDeclaration, but got " + declaration);
		
		this.declaration = declaration;
		this.unit = unit;
		
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

		private MethodDeclaration doInBackground;
		private MethodDeclaration onPostExecute;
		private MethodDeclaration onPreExecute;
		private MethodDeclaration onProgressUpdate;
		private MethodDeclaration onCancelled;


		private List<MethodInvocation> publishInvocations = new ArrayList<>();


		@Override
		public boolean visit(MethodDeclaration node) {
			
			
			// Method which overloads AsyncTask method should have at least 2
			// modifiers they are override and protected
			//if (node.modifiers().size() > 1) {
								
			String methodDeclarationName = node.getName().toString();
			
			if (Objects.equals(DO_IN_BACKGROUND, methodDeclarationName) && node.parameters().size() == 1) {
				doInBackground = node;
//					resultType = node.getReturnType2();
////					parameters = methodDeclaration.parameters().toString().replace("[", "").replace("]", "");
//					isVoid = (parameters == null ? false : (parameters.contains("Void") ? true : false));
//					doInBackgroundMethod = methodDeclaration;
			} else if (ON_POST_EXECUTE.equals(methodDeclarationName) && node.parameters().size() == 1) {
				onPostExecute = node;
//					postExecuteType = ((SingleVariableDeclaration) methodDeclaration.parameters().get(0)).getType();
//					postExecuteParameter = (SingleVariableDeclaration) methodDeclaration.parameters().get(0);
			} else if (ON_PRE_EXECUTE.equals(methodDeclarationName) && node.parameters().size() == 0) {
				onPreExecute = node;
			} else if (ON_PROGRESS_UPDATE.equals(methodDeclarationName) && node.parameters().size() == 1) {
				onProgressUpdate = node;
//					progressParameter = (SingleVariableDeclaration) methodDeclaration.parameters().get(0);
			} else if (ON_CANCELLED.equals(methodDeclarationName) && node.parameters().size() == 1) {
				onCancelled = node;
			}
			//}
			
			return true;
		}

		@Override
		public boolean visit(MethodInvocation node) {
			if (ASTUtils.matchesTargetMethod(node, PUBLISH, ClassDetails.ASYNC_TASK.getBinaryName())) {
				publishInvocations.add(node);

			}
			return true;
		}
	}

	/**
	 * Returns the declaration where this wrapper is built of.
	 * 
	 * @return Either a {@link TypeDeclaration} or {@link AnonymousClassDeclaration}. Can not be null.
	 */
	public ASTNode getDeclaration() {
		return declaration;
	}
	
	public List<?> getModifiers() {
		return mapDeclaration(
				type -> type.modifiers(),
				anon -> Lists.newLinkedList());		
	}
	

	public <V> V mapDeclaration(Function<TypeDeclaration, V> ifTypeDeclaration, Function<AnonymousClassDeclaration, V> ifAnoynmousClassDeclaration) {
		if (declaration instanceof TypeDeclaration)
			return ifTypeDeclaration.apply((TypeDeclaration) declaration);
		else if (declaration instanceof AnonymousClassDeclaration)
			return ifAnoynmousClassDeclaration.apply((AnonymousClassDeclaration) declaration);
		else
			throw new IllegalStateException("AsyncTaskWrapper has no valid declaration");			
	}
	
	public void doWithDeclaration(Consumer<TypeDeclaration> ifTypeDeclaration, Consumer<AnonymousClassDeclaration> ifAnoynmousClassDeclaration) {
		if (declaration instanceof TypeDeclaration)
			ifTypeDeclaration.accept((TypeDeclaration) declaration);
		else if (declaration instanceof AnonymousClassDeclaration)
			ifAnoynmousClassDeclaration.accept((AnonymousClassDeclaration) declaration);
		else
			throw new IllegalStateException("AsyncTaskWrapper has no valid declaration");
	}
	
	
	/**
	 * Returns whether this wrapper has been built using an anonymous class.
	 * 
	 * @return True, if the wrapper is built from an anonymous class.
	 */
	public boolean isAnonymousClass() {
		return declaration instanceof AnonymousClassDeclaration;
	}
	
	/**
	 * Returns whether this wrapper has been built using an inner class.
	 * 
	 * @return True, if the wrapper is built from an inner class.
	 */
	public boolean isInnerClass() {
		return declaration instanceof TypeDeclaration && declaration.getParent() instanceof TypeDeclaration;
	}
	
	public RewriteCompilationUnit getUnit() {
		return unit;
	}
	
	public AST getAST() {
		return declaration.getAST();
	}
	
	/**
	 * Resolves and returns the binding for the type declared by this AsyncTask.<br>
	 * <br>	
	 * Note that bindings are generally unavailable unless requested when the AST is being built.
	 *	
	 * @return the binding, or null if the binding cannot be resolved
	 */
	public ITypeBinding resolveTypeBinding() {
		return mapDeclaration(
				type -> type.resolveBinding(),
				anon -> anon.resolveBinding());		
	}
	
	
	
	/**
	 * @return the isVoid
	 */
	public Boolean inputIsVoid() {
		if (getDoInBackground() == null)
			return false;
		
		for (Object element : getDoInBackground().parameters()) {
			SingleVariableDeclaration var = (SingleVariableDeclaration) element;
			
			if (typeIsVoid(var.getType())) {
				return true;
			}				
		}
		return false;
	}
	
	private Boolean typeIsVoid(Type type) {
		return type instanceof SimpleType && ((SimpleType) type).getName() instanceof SimpleName
				&& ((SimpleName)((SimpleType) type).getName()).getIdentifier().equals("Void");
	}


	
	
	/**
	 * Returns the doInBackground method declaration of
	 * this AsyncTask.
	 * 
	 * @return The doInBackground declaration, or null
	 * if there is none.
	 */
	public MethodDeclaration getDoInBackground() {
		return visitor.doInBackground;
	}

	/**
	 * Returns the onPostExecute method declaration of
	 * this AsyncTask.
	 * 
	 * @return The onPostExecute declaration, or null
	 * if there is none.
	 */
	public MethodDeclaration getOnPostExecute() {
		return visitor.onPostExecute;
	}
	
	/**
	 * Returns the onPreExecute method declaration of
	 * this AsyncTask.
	 * 
	 * @return The onPreExecute declaration, or null
	 * if there is none.
	 */
	public MethodDeclaration getOnPreExecute() {
		return visitor.onPreExecute;
	}

	/**
	 * Returns the onPreExecute method declaration of
	 * this AsyncTask.
	 * 
	 * @return The onPreExecute declaration, or null
	 * if there is none.
	 */
	public MethodDeclaration getOnProgressUpdate() {
		return visitor.onProgressUpdate;
	}
	
	/**
	 * Returns the onCancelled method declaration of
	 * this AsyncTask.
	 * 
	 * @return The onCancelled declaration, or null
	 * if there is none.
	 */
	public MethodDeclaration getOnCancelled() {
		return visitor.onCancelled;
	}

	/**
	 * Gets the return type of the doInBackground method.
	 * 
	 * @return The type of the doInBackground method, or null if the type could not
	 *         be resolved.
	 */
	public Type getResultType() {
		if (getDoInBackground() == null)
			return null;
		
		return getDoInBackground().getReturnType2();
	}
	
	/**
	 * Returns the parameter declaration of the onPostExecute
	 * method.
	 * 
	 * @return The declaration of the parameter, or null if
	 * the method is not defined.
	 */
	public SingleVariableDeclaration getOnPostExecuteParameter() {
		if (getOnPostExecute() == null)
			return null;
		
		//The visitor checks if the number of parameters is 1. 
		return (SingleVariableDeclaration) getOnPostExecute().parameters().get(0);
	}
	
	/**
	 * Returns the parameter declaration of the onProgressUpdate
	 * method.
	 * 
	 * @return The declaration of the parameter, or null if
	 * the method is not defined.
	 */
	public SingleVariableDeclaration getOnProgressUpdateParameter() {
		if (getOnProgressUpdate() == null)
			return null;
		
		//The visitor checks if the number of parameters is 1. 
		return (SingleVariableDeclaration) getOnProgressUpdate().parameters().get(0);
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
		return !getFieldDeclarations().isEmpty() || !getAdditionalMethodDeclarations().isEmpty()
				|| hasOnProgressUpdate();
	}
	
	/**
	 * Checks whether this AsyncTask is using a progress update (= intermediate results).
	 * 
	 * @return {@code getOnProgressUpdateBlock() != null}
	 */
	public boolean hasOnProgressUpdate() {
		return getOnProgressUpdate() != null;
	}
	
	

	
	/**
	 * Returns a list of all body declarations.
	 * 
	 * @return The list of all body declarations (element type: {@link BodyDeclaration}).
	 * 
	 * @see TypeDeclaration#bodyDeclarations()
	 * @see AnonymousClassDeclaration#bodyDeclarations()
	 */
	public List<?> getBodyDeclarations() {
		return mapDeclaration(
				type -> type.bodyDeclarations(),
				anon -> anon.bodyDeclarations());
	}
	
	
	/**
	 * Creates a list of all field declarations that are present
	 * in the body of the declaration of this AsyncTask.
	 * 
	 * @return A list of all field declaration. An empty list
	 * if there are none.
	 */
	public List<FieldDeclaration> getFieldDeclarations() {
		List<?> bodyDeclarations = getBodyDeclarations();
		List<FieldDeclaration> result = Lists.newLinkedList();
		
		for (Object bodyDeclaration : bodyDeclarations) {
			if (bodyDeclaration instanceof FieldDeclaration) {
				result.add((FieldDeclaration) bodyDeclaration);
			}
		}	
		
		return result;
	}
	
	public List<MethodDeclaration> getConstructors() {
		List<?> bodyDeclarations = getBodyDeclarations();
		List<MethodDeclaration> result = Lists.newLinkedList();
		
		for (Object bodyDeclaration : bodyDeclarations) {
			if (bodyDeclaration instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) bodyDeclaration;
				if (method.isConstructor()) {
					result.add(method);
				}					
			}
		}	
		
		return result;
	}
	
	
	public ITypeBinding getSuperClass() {
		return mapDeclaration(
				type -> {
					ITypeBinding typeBinding = type.resolveBinding();
					if (typeBinding != null)
						return typeBinding.getSuperclass();
					else
						return null;
				},
				anon -> {
					ITypeBinding typeBinding = anon.resolveBinding();
					if (typeBinding != null)
						return typeBinding.getSuperclass();
					else
						return null;
				});
	}

	/**
	 * Creates a list of all method declarations that are present
	 * in the body of the declaration of this AsyncTask.
	 * Methods of the AsyncTask interface are omitted from
	 * this list.
	 * 
	 * @return A list of all method declarations without those
	 * that are defined by the AsyncTask interface.
	 * An empty list if there are none.
	 */
	public List<MethodDeclaration> getAdditionalMethodDeclarations() {
				
		List<?> bodyDeclarations = getBodyDeclarations();
		List<MethodDeclaration> result = Lists.newLinkedList();
		
		for (Object bodyDeclaration : bodyDeclarations) {
			if (bodyDeclaration instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) bodyDeclaration;
				String methodDeclarationName = method.getName().getIdentifier();
				
				if (!DO_IN_BACKGROUND.equals(methodDeclarationName) && !ON_POST_EXECUTE.equals(methodDeclarationName)
						&& !ON_PRE_EXECUTE.equals(methodDeclarationName) && !ON_CANCELLED.equals(methodDeclarationName)
						&& !ON_PROGRESS_UPDATE.equals(methodDeclarationName)) {
					result.add(method);
				}
			}
		}	
		
		return result;
	}
	
	public Optional<BodyDeclaration> getEnclosingDeclaration() {
		return ASTNodes.findParent(declaration, BodyDeclaration.class);
	}
	
	
	/**
	 * Finds all class instance creation expressions where this task
	 * is created.
	 * 
	 * @param units The units where to search for class instance creations.
	 * Can only be null, if the declaration of this AsyncTaskWrapper is
	 * an anonymous class.
	 * 
	 * @return A set of all class instance creation expressions that create
	 * this AsyncTask. The set is empty if there are no such expressions. 
	 */
	public Set<ClassInstanceCreation> findClassInstanceCreationsIn(ProjectUnits units) {		
		
		class InstanceCreationVisitor extends UnitASTVisitor {
			
			final Set<ClassInstanceCreation> classInstanceCreations = Sets.newHashSet();
			final ITypeBinding asyncTaskBinding = resolveTypeBinding();			
			
			public boolean visit(ClassInstanceCreation node) {
				ITypeBinding nodeType = node.resolveTypeBinding();
				if (nodeType != null && nodeType.isEqualTo(asyncTaskBinding)) {
					classInstanceCreations.add(node);
				}
				return true;
			}
		}
		
		return mapDeclaration(
				type -> {					
					Objects.requireNonNull(units, "units can not be null.");
					
					InstanceCreationVisitor visitor = new InstanceCreationVisitor();
					units.accept(visitor);					
					return visitor.classInstanceCreations;
				}, 
				anon -> {
					ASTNode parent = anon.getParent();
					if (parent != null && parent instanceof ClassInstanceCreation) {
						return Sets.newHashSet((ClassInstanceCreation) parent);
					} else {
						return Collections.emptySet();
					}
				});
	}
	
	
	public Set<MethodInvocation> findUsagesIn(ProjectUnits units) {
		return null;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public Type getResultTypeOrVoid() {
		Type t = getResultType(); 		
		if (t == null) return getAST().newSimpleType(getAST().newSimpleName("Void"));
		else return t;
	}
	
	@Override
	public String toString() {
		return "AsyncTaskWrapper(unit = " + unit.getElementName() + ", anoynmous/inner class = " + isAnonymousClass() + "/" + isInnerClass() + ", declaration =\n" + declaration.toString();
	}

}
