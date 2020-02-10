package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
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
 * Created: 12/22/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class FieldDeclarationWorker extends GeneralWorker<TypeOutput, Void> {
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, FieldDeclaration> entry : input.collector.getFieldDeclMap().entries()) {
			IRewriteCompilationUnit icu = entry.getKey();
			FieldDeclaration fieldDeclaration = entry.getValue();

			if (!info.shouldBeRefactored(fieldDeclaration.getType()) && !fieldDeclaration.getType().resolveBinding()
					.getErasure().getQualifiedName().equals("javax.swing.SwingWorker")) {
				summary.addSkipped("fieldDeclarations");
				continue;
			}

			AST ast = fieldDeclaration.getAST();

			Type type = fieldDeclaration.getType();
			if (type instanceof ParameterizedType) {
				type = ((ParameterizedType) type).getType();
			}
			if (Types.isExactTypeOf(type.resolveBinding(), true, SwingWorkerInfo.getBinaryName())) {
				SimpleType newType = SwingWorkerASTUtils.newSimpleType(ast, "SWSubscriber");
				synchronized (icu) {
					icu.replace(type, newType);
				}
			}

			VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
			String oldIdentifier = varDeclFrag.getName().getIdentifier();
			synchronized (icu) {
				icu.replace(varDeclFrag.getName(),
						SwingWorkerASTUtils.newSimpleName(ast, RefactoringUtils.cleanSwingWorkerName(oldIdentifier)));
				icu.addImport("de.tudarmstadt.stg.rx.swingworker.SWSubscriber");
			}

			Expression initializer = varDeclFrag.getInitializer();
			if (initializer != null && initializer instanceof ClassInstanceCreation) {
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;
				if (ASTUtils.isClassOf(classInstanceCreation, SwingWorkerInfo.getBinaryName())) {
					SwingWorkerWrapper refactoringVisitor = new SwingWorkerWrapper();
					classInstanceCreation.accept(refactoringVisitor);

					refactor(icu, refactoringVisitor, fieldDeclaration);
				}
			}

			summary.addCorrect("fieldDeclarations");
		}
		
		return null;
	}

	private void refactor(IRewriteCompilationUnit icu, SwingWorkerWrapper refactoringVisitor,
			FieldDeclaration fieldDeclaration) {
		removeSuperInvocations(refactoringVisitor);
		updateImports(icu);

		VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
		String oldIdentifier = varDeclFrag.getName().getIdentifier();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName(oldIdentifier);
		SWSubscriberModel subscriberDto = createSWSubscriberDto(rxObserverName, icu, refactoringVisitor);

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put("model", subscriberDto);
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate(subscriberTemplate, subscriberData);
		AST ast = fieldDeclaration.getAST();
		TypeDeclaration typeDeclaration = TemplateVisitor.createTypeDeclarationFromText(ast, subscriberString);

		SwingWorkerASTUtils.addInnerClassAfter(icu, typeDeclaration, fieldDeclaration);

		ClassInstanceCreation newClassInstanceCreation = SwingWorkerASTUtils.newClassInstanceCreation(ast,
				subscriberDto.getClassName());

		ClassInstanceCreation oldClassInstanceCreation = (ClassInstanceCreation) varDeclFrag.getInitializer();
		synchronized (icu) {
			icu.replace(oldClassInstanceCreation, newClassInstanceCreation);
		}
	}

}
