package de.tudarmstadt.rxrefactoring.ext.asynctask.utils;

import com.google.common.collect.Sets;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.OldUseDefAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.OldUseDefAnalysis.UseDefResult;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDefAnalysis;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;



/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/18/2017
 */
public class AsyncTaskASTUtils {

	public static <T> T findOuterParent(ASTNode node, Class<T> target) {
		ASTNode parent = node.getParent();
		ASTNode outerParent = node.getParent();
		while (parent != null) {
			if (target.isInstance(parent)) {
				outerParent = parent;
			}
			parent = parent.getParent();
		}
		if (target.isInstance(outerParent)) {
			return target.cast(outerParent);
		}
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
	 * @param root The root of the AST to check.
	 * @return True, if a forbidden method has been found.
	 */
	public static boolean containsForbiddenMethod(ASTNode root) {

		boolean result = ASTNodes.containsNode(root, (n) -> {
			if (n instanceof MethodInvocation) {
				MethodInvocation inv = (MethodInvocation) n;

				return ASTUtils.matchSignature(inv, "^android\\.os\\.AsyncTask(<.*>)?$", "isCancelled", "^boolean$")
						|| ASTUtils.matchSignature(inv, "^android\\.os\\.AsyncTask(<.*>)?$", "getStatus",
						"^android.os.AsyncTask.Status$");
			}

			return false;
		});

		return result;
	}

	public static void replaceThisWithFullyQualifiedThisIn(ASTNode root, IRewriteCompilationUnit unit,
			String thisClassName) {

		final AST ast = unit.getAST();

		class ThisVisitor extends ASTVisitor {

			public boolean visit(ThisExpression node) {
				if (node.getQualifier() == null) {
					ThisExpression thisExpr = ast.newThisExpression();
					thisExpr.setQualifier(ast.newSimpleName(thisClassName));

					unit.replace(node, thisExpr);
				}

				return true;
			}
		}

		root.accept(new ThisVisitor());
	}

	/**
	 * Replaces all field retrieves with the fully qualified name
	 * for the field, e.g. f = ... --> MyClass.this.f = ...
	 */
	public static void replaceFieldsWithFullyQualifiedNameIn(ASTNode root, IRewriteCompilationUnit unit) {

		class FieldsVisitor extends ASTVisitor {

			@Override
			public boolean visit(Assignment node) {

				Expression lhs = node.getLeftHandSide();
				processFieldExpression(lhs);

				return true;
			}

			@Override
			public boolean visit(MethodInvocation node) {
				Expression expr = node.getExpression();

				if (expr != null) {
					processFieldExpression(expr);
				}

				return true;
			}

			private void processFieldExpression(Expression expr) {
				Optional<TypeDeclaration> t = ASTNodes.findParent(expr, TypeDeclaration.class);
				if (!t.isPresent()) {
					Log.error(AsyncTaskASTUtils.class, "Could not find enclosing type declaration.");
					return;
				}

				ITypeBinding binding = t.get().resolveBinding();
				if (Objects.isNull(binding)) {
					Log.error(AsyncTaskASTUtils.class, "Could not resolve type binding.");
					return;
				}

				// binding.get

				if (expr instanceof SimpleName) {
					SimpleName variableName = (SimpleName) expr;

					for (IVariableBinding variable : binding.getDeclaredFields()) {

						if (variable != null
								//Name is the same as the expression name
								&& variable.getName().equals(variableName.getIdentifier())
								//The field is not static
								&& ((variable.getModifiers() & Modifier.STATIC) == 0)) {
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

	public static boolean canBeRefactored(AsyncTaskWrapper asyncTask) {

		//The AsyncTask needs to have a doInBackground method.
		if (asyncTask.getDoInBackground() == null) {
			return false;
		}

		//Checks whether the implementation directly extends AsyncTask
		//TODO: Activate this working code if we want to improve refactor detection.
//		ITypeBinding superClass = asyncTask.getSuperClass();
//		if (superClass == null || !Objects.equals(superClass.getErasure().getQualifiedName(), "android.os.AsyncTask")) {
//			return false;
//		}

//		ASTNode declarationNode = asyncTask.getDeclaration();
//		if (declarationNode instanceof AnonymousClassDeclaration) {
//			Assignment assignment = ASTNodes.findParent(declarationNode, Assignment.class).orElse(null);
//			VariableDeclarationStatement parentStatement = ASTNodes.findParent(declarationNode, VariableDeclarationStatement.class).orElse(null);
//			Block parentBlock = ASTNodes.findParent(declarationNode, Block.class).orElse(null);
//			if (assignment != null && parentStatement != null && parentBlock != null) {
//				IControlFlowGraph<ASTNode> cfg = ProgramGraph.createFrom(parentBlock);
//				UseDefAnalysis analysis = UseDefAnalysis.create();
//				Map<ASTNode, UseDef> useDef = analysis.apply(cfg, analysis.mapExecutor());
//
//				for (SimpleName name : useDef.get(parentStatement).uses.get(assignment.getLeftHandSide().toString())) {
//					if (name.getIdentifier().equals("cancel")) {
//						return false;
//					}
//				}
//			}
//		}

		return true;
	}
}
