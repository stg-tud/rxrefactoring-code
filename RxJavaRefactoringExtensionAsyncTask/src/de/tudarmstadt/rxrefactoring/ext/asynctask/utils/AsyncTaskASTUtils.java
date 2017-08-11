package de.tudarmstadt.rxrefactoring.ext.asynctask.utils;

import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/18/2017
 */
public class AsyncTaskASTUtils {
	public static <T> ASTNode findOuterParent(ASTNode node, Class<?> target) {
		ASTNode parent = node.getParent();
		ASTNode outerParent = node.getParent();
		while (parent != null) {
			if (target.isInstance(parent)) {
				outerParent = parent;
			}
			parent = parent.getParent();
		}
		if (target.isInstance(outerParent))
			return outerParent;
		return null;
	}

//	public static ClassInstanceCreation getinstannceCreationStatement(AST astInvoke, String name) {
//		ClassInstanceCreation instanceCreationExpr = astInvoke.newClassInstanceCreation();
//		instanceCreationExpr.setType(astInvoke.newSimpleType(astInvoke.newSimpleName(name)));
//		return instanceCreationExpr;
//	}

	/**
	 * Checks whether an AST contains an invocation to a method that is not allowed
	 * for refactoring. Those methods are AsyncTask.isCancelled() and
	 * AsyncTask.getStatus().
	 * 
	 * @param root
	 *            The root of the AST to check.
	 * @return True, if a forbidden method has been found.
	 */
	public static boolean containsForbiddenMethod(ASTNode root) {

		// Log.info(AsyncTaskASTUtils.class, "### Contains");

		boolean result = ASTUtils.containsNode(root, (n) -> {
			if (n instanceof MethodInvocation) {
				MethodInvocation inv = (MethodInvocation) n;
				// Log.info(AsyncTaskASTUtils.class, inv.resolveMethodBinding() + " " +
				// inv.resolveMethodBinding().getReturnType() );
				return ASTUtils.matchSignature(inv, "^android\\.os\\.AsyncTask(<.*>)?$", "isCancelled", "^boolean$")
						|| ASTUtils.matchSignature(inv, "^android\\.os\\.AsyncTask(<.*>)?$", "getStatus",
								"^android.os.AsyncTask.Status$");
			}

			return false;
		});

		// Log.info(AsyncTaskASTUtils.class, "### Result : " + node + ", " + result);
		return result;
	}

	public static void replaceFieldsWithFullyQualifiedNameIn(ASTNode root, RewriteCompilationUnit unit) {

		class FieldsVisitor extends ASTVisitor {
			@Override
			public void endVisit(Assignment node) {

				Expression lhs = node.getLeftHandSide();
				processFieldExpression(lhs);

				return;
			}

			@Override
			public void endVisit(MethodInvocation node) {
				Expression expr = node.getExpression();
				
				if (expr != null) processFieldExpression(expr);

				return;
			}

			private void processFieldExpression(Expression expr) {
				TypeDeclaration t = ASTUtils.findParent(expr, TypeDeclaration.class);
				if (Objects.isNull(t)) {
					Log.error(AsyncTaskASTUtils.class, "Could not find enclosing type declaration.");
					return;
				}

				ITypeBinding binding = t.resolveBinding();
				if (Objects.isNull(binding)) {
					Log.error(AsyncTaskASTUtils.class, "Could not resolve type binding.");
					return;
				}

				// binding.get

				if (expr instanceof SimpleName) {
					SimpleName variableName = (SimpleName) expr;

					// TODO: Fix the warning.
					IVariableBinding variable = Bindings.findFieldInType(binding, variableName.getIdentifier());

					if (variable != null && variable.isField()) {
						//variableName.setIdentifier(variable.getDeclaringClass().getQualifiedName() + ".this." + variableName);
						
						ThisExpression thisExp = unit.getAST().newThisExpression();
						thisExp.setQualifier(unit.getAST().newName(variable.getDeclaringClass().getName()));
						
						FieldAccess fa = unit.getAST().newFieldAccess();
						fa.setName(unit.getAST().newSimpleName(variableName.getIdentifier()));
						fa.setExpression(thisExp);						
						
						
						unit.replace(variableName, fa);
						
						
					}
				}				
				
			}
		}

		FieldsVisitor v = new FieldsVisitor();
		root.accept(v);
	}
	
	
	

	public static Set<SimpleName> getVariableNames(Expression expr) {
		class ExpressionVisitor extends ASTVisitor {

			final Set<SimpleName> names = Sets.newHashSet();

			public boolean visit(VariableDeclarationFragment node) {
				names.add(node.getName());
				return false;
			}

			public boolean visit(SimpleName node) {
				names.add(node);
				return false;
			}
		}

		ExpressionVisitor v = new ExpressionVisitor();
		expr.accept(v);
		return v.names;
	}

}
