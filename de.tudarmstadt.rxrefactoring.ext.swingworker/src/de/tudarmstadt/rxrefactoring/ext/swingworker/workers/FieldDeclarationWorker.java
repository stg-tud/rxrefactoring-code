package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.*;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SWSubscriberModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.TemplateUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.RefactoringVisitor;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.TemplateVisitor;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/22/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class FieldDeclarationWorker extends GeneralWorker {
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units,
			de.tudarmstadt.rxrefactoring.ext.swingworker.workers.@Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception {
		Multimap<IRewriteCompilationUnit, FieldDeclaration> fieldDeclMap = input.getFieldDeclMap();
		int total = fieldDeclMap.values().size();
		Log.info(getClass(), "METHOD=refactor - Total number of <<FieldDeclaration>>: " + total);

		for (Map.Entry<IRewriteCompilationUnit, FieldDeclaration> fieldDeclEntry : fieldDeclMap.entries()) {
			IRewriteCompilationUnit icu = fieldDeclEntry.getKey();
			FieldDeclaration fieldDeclaration = fieldDeclEntry.getValue();

			AST ast = fieldDeclaration.getAST();

			Log.info(getClass(), "METHOD=refactor - Changing type: " + icu.getElementName());
			Type type = fieldDeclaration.getType();
			if (type instanceof ParameterizedType) {
				type = ((ParameterizedType) type).getType();
			}
			if (ASTUtils.isClassOf(type, SwingWorkerInfo.getBinaryName())) {
				SimpleType newType = SwingWorkerASTUtils.newSimpleType(ast, "SWSubscriber");
				synchronized (icu) {
					icu.replace(type, newType);
				}
			}

			Log.info(getClass(), "METHOD=refactor - Changing field name: " + icu.getElementName());
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
					Log.info(getClass(),
							"METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName());
					RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
					classInstanceCreation.accept(refactoringVisitor);

					Log.info(getClass(), "METHOD=refactor - Refactoring assignment in: " + icu.getElementName());
					refactor(icu, refactoringVisitor, fieldDeclaration);
				}
			}

			// Add changes to the multiple compilation units write object
			Log.info(getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName());

			summary.addCorrect("FieldDeclarations");
		}
		return null;
	}

	private void refactor(IRewriteCompilationUnit icu, RefactoringVisitor refactoringVisitor,
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
