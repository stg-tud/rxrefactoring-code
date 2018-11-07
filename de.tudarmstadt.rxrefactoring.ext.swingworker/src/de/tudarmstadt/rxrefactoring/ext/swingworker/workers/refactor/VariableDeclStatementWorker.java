package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
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
public class VariableDeclStatementWorker extends GeneralWorker<TypeOutput, Void> {

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, VariableDeclarationStatement> entry : input.collector.getVarDeclMap()
				.entries()) {

			IRewriteCompilationUnit icu = entry.getKey();
			VariableDeclarationStatement varDeclStatement = entry.getValue();

			ITypeBinding typeBinding = varDeclStatement.getType().resolveBinding();
			
			if (!info.shouldBeRefactored(typeBinding) && !Types.isExactTypeOf(typeBinding.getErasure(), "javax.swing.SwingWorker")) {
				summary.addSkipped("variableDeclarations");
				continue;
			}

			AST ast = varDeclStatement.getAST();

			VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDeclStatement.fragments().get(0);
			Expression initializer = fragment.getInitializer();
			if (initializer instanceof ClassInstanceCreation) {
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;

				//if (Types.isExactTypeOf(classInstanceCreation.resolveTypeBinding().getErasure(), "javax.swing.SwingWorker")) {
				if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
						//&& Types.isExactTypeOf(classInstanceCreation.resolveTypeBinding(), "javax.swing.SwingWorker")) {
					SwingWorkerWrapper refactoringVisitor = new SwingWorkerWrapper();
					varDeclStatement.accept(refactoringVisitor);
					refactorVarDecl(icu, refactoringVisitor, varDeclStatement, fragment);
				} else {
					SimpleName simpleName = fragment.getName();
					String newIdentifier = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());
					synchronized (icu) {
						icu.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, newIdentifier));
					}
				}
			} else if (initializer instanceof SimpleName) {
				SimpleName assignedVarSimpleName = (SimpleName) initializer;
				String newAssignedVarName = RefactoringUtils
						.cleanSwingWorkerName(assignedVarSimpleName.getIdentifier());
				SimpleName newAssignedVar = SwingWorkerASTUtils.newSimpleName(ast, newAssignedVarName);
				
				synchronized (icu) {
					icu.replace(assignedVarSimpleName, newAssignedVar);
				}
			}

			// change type
			Type type = varDeclStatement.getType();
			if (type instanceof ParameterizedType) {
				type = ((ParameterizedType) type).getType();
			}

			if (Types.isExactTypeOf(type.resolveBinding().getErasure(), SwingWorkerInfo.getBinaryName())) {
				synchronized (icu) {
					icu.addImport("de.tudarmstadt.stg.rx.swingworker.SWSubscriber");
					SimpleType newType = SwingWorkerASTUtils.newSimpleType(ast, "SWSubscriber");
					icu.replace(type, newType);
				}
			}

			String newVarName = RefactoringUtils.cleanSwingWorkerName(fragment.getName().getIdentifier());
			synchronized (icu) {
				icu.replace(fragment.getName(), SwingWorkerASTUtils.newSimpleName(ast, newVarName));
			}

			// Add changes to the multiple compilation units write object
			Log.info(getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName());

			summary.addCorrect("variableDeclarations");

		}

		return null;
	}

	private void refactorVarDecl(IRewriteCompilationUnit icu, SwingWorkerWrapper refactoringVisitor,
			VariableDeclarationStatement varDeclStatement, VariableDeclarationFragment fragment) {
		removeSuperInvocations(refactoringVisitor);
		updateImports(icu);

		if (refactoringVisitor.hasAdditionalFieldsOrMethods()) {
			refactorStatefulSwingWorker(icu, refactoringVisitor, varDeclStatement, fragment);
		} else {
			refactorStatelessSwingWorker(icu, refactoringVisitor, varDeclStatement, fragment);
			//refactorStatefulSwingWorker(icu, refactoringVisitor, varDeclStatement, fragment);
		}
	}

	private void refactorStatefulSwingWorker(IRewriteCompilationUnit icu, SwingWorkerWrapper refactoringVisitor,
			VariableDeclarationStatement varDeclStatement, VariableDeclarationFragment fragment) {
		SimpleName swingWorkerName = fragment.getName();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName(swingWorkerName.getIdentifier());
		SWSubscriberModel subscriberDto = createSWSubscriberDto(rxObserverName, icu, refactoringVisitor);

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put("model", subscriberDto);
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate(subscriberTemplate, subscriberData);
		AST ast = varDeclStatement.getAST();
		TypeDeclaration typeDeclaration = TemplateVisitor.createTypeDeclarationFromText(ast, subscriberString);

		SwingWorkerASTUtils.addBefore(icu, typeDeclaration, varDeclStatement);

		String newVarDeclStatementString = "SWSubscriber<" + subscriberDto.getResultType() + ", "
				+ subscriberDto.getProcessType() + "> " + subscriberDto.getSubscriberName() + " = new "
				+ subscriberDto.getClassName() + "()";
		Statement newVarDeclStatement = TemplateVisitor.createSingleStatementFromText(ast, newVarDeclStatementString);
		Statements.addStatementBefore(icu, newVarDeclStatement, varDeclStatement);
		SwingWorkerASTUtils.removeStatement(icu, varDeclStatement);
	}

	private void refactorStatelessSwingWorker(IRewriteCompilationUnit icu, SwingWorkerWrapper refactoringVisitor,
			VariableDeclarationStatement varDeclStatement, VariableDeclarationFragment fragment) {
		RxObservableModel observableDto = createObservableDto(icu, refactoringVisitor);
		/*
		 * Sometimes doInbackground Block needs to be refactored futher for handling
		 * java.util.concurrent.ExecutorService.submit() method calls
		 * executor.submit(singleRun) --> singleRun.executeObservable() Scenario:
		 * CrysHomModeling-master : MainMenu.java
		 */
		observableDto.setDoInBackgroundBlock(cleanBlock(refactoringVisitor.getDoInBackgroundBlock()));
		Map<String, Object> observableData = new HashMap<>();
		observableData.put("model", observableDto);
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate(observableTemplate, observableData);

		AST ast = varDeclStatement.getAST();
		Statement observableStatement = TemplateVisitor.createSingleStatementFromText(ast, observableString);

		Statements.addStatementBefore(icu, observableStatement, varDeclStatement);

		SimpleName swingWorkerName = fragment.getName();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName(swingWorkerName.getIdentifier());
		RxObserverModel subscriberDto = createObserverDto(rxObserverName, refactoringVisitor, observableDto);
		subscriberDto.setVariableDecl(true);

		Map<String, Object> observerData = new HashMap<>();
		observerData.put("model", subscriberDto);
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate(observerTemplate, observerData);
		Statement observerStatement = TemplateVisitor.createSingleStatementFromText(ast, observerString);
		Statements.addStatementBefore(icu, observerStatement, varDeclStatement);
		SwingWorkerASTUtils.removeStatement(icu, varDeclStatement);
	}

	private String cleanBlock(Block block) {	
		return RefactoringUtils.cleanSwingWorkerName(block.toString());
	}

}
