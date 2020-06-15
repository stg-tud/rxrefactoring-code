package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.RxObservableModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.RxObserverModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SWSubscriberModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactorInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.TemplateUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.TemplateVisitor;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.GeneralWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.SwingWorkerWrapper;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class AssignmentWorker extends GeneralWorker<TypeOutput, Void> {

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, Assignment> assignmentEntry : input.collector.getAssigmentsMap()
				.entries()) {
			IRewriteCompilationUnit icu = assignmentEntry.getKey();
			Assignment assignment = assignmentEntry.getValue();
			ITypeBinding binding = assignment.getRightHandSide().resolveTypeBinding();

			if (assignment.getRightHandSide() instanceof ClassInstanceCreation) {
				ClassInstanceCreation classTest = (ClassInstanceCreation) assignment.getRightHandSide();
				Type type = classTest.getType();

				if (type instanceof ParameterizedType) {
					type = ((ParameterizedType) type).getType();
				}

				binding = type.resolveBinding();
			}

			if (!info.shouldBeRefactored(assignment.getRightHandSide().resolveTypeBinding())
					&& !Types.isExactTypeOf(binding.getErasure(), "javax.swing.SwingWorker")) {
				summary.addSkipped("assignment");
				continue;
			}

			AST ast = assignment.getAST();

			Expression rightHandSide = assignment.getRightHandSide();
			if (rightHandSide instanceof ClassInstanceCreation) {
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightHandSide;

				if (Types.isExactTypeOf(classInstanceCreation.getType().resolveBinding(),
						SwingWorkerInfo.getBinaryName())) {
					Log.info(getClass(),
							"METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName());
					SwingWorkerWrapper refactoringVisitor = new SwingWorkerWrapper();
					assignment.accept(refactoringVisitor);

					Log.info(getClass(), "METHOD=refactor - Refactoring assignment in: " + icu.getElementName());
					refactorAssignment(icu, refactoringVisitor, assignment);
				}
			} else if (rightHandSide instanceof SimpleName) {
				Log.info(getClass(), "METHOD=refactor - Refactoring right variable name: " + icu.getElementName());
				SimpleName simpleName = (SimpleName) rightHandSide;
				String newIdentifier = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());

				if (!newIdentifier.equals(simpleName.getIdentifier())) {
					synchronized (icu) {
						icu.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, newIdentifier));
					}
				}
			}

			Expression leftHandSide = assignment.getLeftHandSide();

			if (leftHandSide instanceof SimpleName && !(assignment.getLeftHandSide() instanceof FieldAccess)) {
				Log.info(getClass(), "METHOD=refactor - Refactoring left variable name: " + icu.getElementName());
				SimpleName simpleName = (SimpleName) leftHandSide;

				if (!isFieldName(simpleName)) {
					String newIdentifier = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());
					if (!newIdentifier.equals(simpleName.getIdentifier())) {
						synchronized (icu) {
							icu.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, newIdentifier));
						}
					}
				}
			}

			Log.info(getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName());

			summary.addCorrect("assignment");
		}

		return null;
	}

	private boolean isFieldName(SimpleName name) {
		if (name.resolveBinding().getKind() == IBinding.VARIABLE) {
			IVariableBinding b = (IVariableBinding) name.resolveBinding();
			return b.isField();
		}

		return false;
	}

	private void refactorAssignment(IRewriteCompilationUnit icu, SwingWorkerWrapper refactoringVisitor,
			Assignment assignment) {
		removeSuperInvocations(refactoringVisitor);
		updateImports(icu);

		if (refactoringVisitor.hasAdditionalFieldsOrMethods()) {
			refactorStatefulSwingWorker(icu, refactoringVisitor, assignment);
		} else {
			refactorStatelessSwingWorker(icu, refactoringVisitor, assignment);
		}
	}

	private void refactorStatefulSwingWorker(IRewriteCompilationUnit icu, SwingWorkerWrapper refactoringVisitor,
			Assignment assignment) {
		SimpleName swingWorkerName;
		if (assignment.getLeftHandSide() instanceof FieldAccess) {
			FieldAccess fa = (FieldAccess) assignment.getLeftHandSide();
			swingWorkerName = (SimpleName) fa.getName();
		} else {
			swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		}
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName(swingWorkerName.getIdentifier());
		SWSubscriberModel subscriberModel = createSWSubscriberDto(rxObserverName, icu, refactoringVisitor);

		// Template is called
		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put("model", subscriberModel);
		String subscriberTemplate = "subscriber.ftl";
		String subscriberString = TemplateUtils.processTemplate(subscriberTemplate, subscriberData);
		AST ast = assignment.getAST();

		// Type declaration of RxObserver is added
		TypeDeclaration typeDeclaration = TemplateVisitor.createTypeDeclarationFromText(ast, subscriberString);
		Statement referenceStatement = ASTNodes.findParent(assignment, Statement.class).get();
		SwingWorkerASTUtils.addBefore(icu, typeDeclaration, referenceStatement);

		// Assignment of new rxObserver is added
		ExpressionStatement newAssignment = SwingWorkerASTUtils.newAssignment(ast, subscriberModel.getSubscriberName(),
				subscriberModel.getClassName());
		Statements.addStatementBefore(icu, newAssignment, referenceStatement);

		// Assignment of new SwingWorker is removed
		SwingWorkerASTUtils.removeStatement(icu, assignment);

	}

	private void refactorStatelessSwingWorker(IRewriteCompilationUnit icu, SwingWorkerWrapper refactoringVisitor,
			Assignment assignment) {
		RxObservableModel observableDto = createObservableDto(icu, refactoringVisitor);
		/*
		 * Sometimes doInbackground Block needs to be refactored futher like
		 * MainController.this.requestWorker.execute() -->
		 * MainController.this.requestRxObserver.executeObservable() Scenario:
		 * 37--KolakCC--lol-jclient : MainController.java
		 */
		observableDto.setDoInBackgroundBlock(refactorDoInBackgroundBlock(refactoringVisitor.getDoInBackgroundBlock()));

		Map<String, Object> observableData = new HashMap<>();
		observableData.put("model", observableDto);
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate(observableTemplate, observableData);

		AST ast = assignment.getAST();

		Statement observableStatement = TemplateVisitor.createSingleStatementFromText(ast, observableString);

		Statement referenceStatement = ASTNodes.findParent(assignment, Statement.class).get();
		Statements.addStatementBefore(icu, observableStatement, referenceStatement);

		SimpleName swingWorkerName = null;
		if (assignment.getLeftHandSide() instanceof SimpleName) {
			swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		} else if (assignment.getLeftHandSide() instanceof FieldAccess) {
			FieldAccess fa = (FieldAccess) assignment.getLeftHandSide();
			swingWorkerName = (SimpleName) fa.getName();
		}
		if (swingWorkerName != null) {
			String rxObserverName = RefactoringUtils.cleanSwingWorkerName(swingWorkerName.getIdentifier());
			RxObserverModel observerDto = createObserverDto(rxObserverName, refactoringVisitor, observableDto);
			observerDto.setVariableDecl(false);

			Map<String, Object> observerData = new HashMap<>();
			observerData.put("model", observerDto);
			String observerTemplate = "observer.ftl";

			String observerString = TemplateUtils.processTemplate(observerTemplate, observerData);
			Statement observerStatement = TemplateVisitor.createSingleStatementFromText(ast, observerString);
			Statements.addStatementBefore(icu, observerStatement, referenceStatement);

			SwingWorkerASTUtils.removeStatement(icu, assignment);
		}
	}

	@SuppressWarnings("unchecked")
	private String refactorDoInBackgroundBlock(Block doInBgBlock) {
		String doInBgBlockString = "";
		List<Statement> list = doInBgBlock.statements();
		StringBuilder sb = new StringBuilder();
		sb.append("{ " + System.lineSeparator());
		for (int i = 0; i < list.size(); i++) {
			Statement stmnt = list.get(i);
			if (stmnt instanceof ExpressionStatement) {
				ExpressionStatement exprstmnt = (ExpressionStatement) stmnt;
				if (exprstmnt.getExpression() instanceof MethodInvocation) {
					MethodInvocation methodInvocation = (MethodInvocation) exprstmnt.getExpression();
					SimpleName methodSimpleName = methodInvocation.getName();
					String newMethodName = RefactoringUtils.getNewMethodName(methodSimpleName.toString());
					stmnt = TemplateVisitor.createSingleStatementFromText(doInBgBlock.getAST(),
							stmnt.toString().replace(methodSimpleName.toString(), newMethodName));
				}
			}
			sb.append(stmnt.toString() + System.lineSeparator());
		}
		sb.append("}");
		doInBgBlockString = RefactoringUtils.cleanSwingWorkerName(sb.toString());
		return doInBgBlockString;
	}
}
