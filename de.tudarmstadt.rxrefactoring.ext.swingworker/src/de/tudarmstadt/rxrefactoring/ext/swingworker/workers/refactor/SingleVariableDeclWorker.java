package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactorInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class SingleVariableDeclWorker implements IWorker<TypeOutput, Void> {

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, SingleVariableDeclaration> singVarDeclEntry : input.collector
				.getSingleVarDeclMap().entries()) {

			IRewriteCompilationUnit unit = singVarDeclEntry.getKey();
			SingleVariableDeclaration singleVarDecl = singVarDeclEntry.getValue();

			if (!info.shouldBeRefactored(singleVarDecl.getType()) && !singleVarDecl.getType().resolveBinding()
					.getErasure().getQualifiedName().equals("javax.swing.SwingWorker")) {
				summary.addSkipped("variableDeclarations");
				continue;
			}

			AST ast = singleVarDecl.getAST();

			SimpleTypeVisitor visitor = new SimpleTypeVisitor();
			singleVarDecl.accept(visitor);

			for (SimpleName simpleName : visitor.simpleNames) {
				if ("SwingWorker".equals(simpleName.toString())) {
					synchronized (unit) {
						unit.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, "SWSubscriber"));
						unit.addImport("de.tudarmstadt.stg.rx.swingworker.SWSubscriber");
					}
					/*
					 * Scenario : 46--ggasoftware--indigo : variables present as an argument of some
					 * method, after renaming import should be added. E.g.
					 * ProgressStatusDialog.java(See executeSwingWorker method argument variable)
					 */
				} else {

					ITypeBinding typeBinding = simpleName.resolveTypeBinding();
					boolean isTopLevel = typeBinding != null && typeBinding.isTopLevel();

					IBinding binding = simpleName.resolveBinding();
					boolean isTypeName = binding != null && binding.getKind() == IBinding.TYPE;

					if (!(simpleName.getParent() instanceof QualifiedName) &&
					// if isTypeName then !isTopLevel. Only rename types if they are not top level
					// types.
							(!isTypeName || !isTopLevel))
						synchronized (unit) {
							String s = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());
							if(!s.equals(simpleName.getIdentifier())) {
								unit.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, s));
							}
						}
				}
			}

			summary.addCorrect("variableDeclarations");
		}

		return null;
	}

	private class SimpleTypeVisitor extends ASTVisitor {
		private List<SimpleName> simpleNames = new ArrayList<>();

		@Override
		public boolean visit(SimpleName node) {
			ITypeBinding type = node.resolveTypeBinding();
			if (ASTUtils.isTypeOf(type, SwingWorkerInfo.getBinaryName())) {
				simpleNames.add(node);
			}
			return true;
		}
	}
}
