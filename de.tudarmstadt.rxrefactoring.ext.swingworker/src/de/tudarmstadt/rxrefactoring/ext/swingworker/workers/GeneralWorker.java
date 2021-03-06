package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.legacy.IdManager;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.RxObservableModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.RxObserverModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SWSubscriberModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.SwingWorkerWrapper;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 19/01/2018
 */
public abstract class GeneralWorker<In, Out> implements IWorker<In, Out> {

	public static final String KEYWORD_THIS_WITH_DOT = "this.";
	public static final String EMPTY = "";
	public static final String OBJECT_TYPE_NAME = "Object";

	protected final Set<String> addedImports = new HashSet<String>();

	protected void updateImports(IRewriteCompilationUnit unit) {
		synchronized (unit) {
			unit.addImport("io.reactivex.Emitter");
			unit.addImport("io.reactivex.Observable");
			unit.addImport("de.tudarmstadt.stg.rx.swingworker.SWEmitter");
			unit.addImport("de.tudarmstadt.stg.rx.swingworker.SWSubscriber");
			unit.addImport("de.tudarmstadt.stg.rx.swingworker.SWPackage");
		}
	}

	protected RxObservableModel createObservableDto(IRewriteCompilationUnit icuName,
			SwingWorkerWrapper refactoringVisitor) {
		String varName = "rxObservable" + IdManager.getNextObservableId(icuName);
		RxObservableModel observableDto = new RxObservableModel(varName);
		Type resultType = refactoringVisitor.getResultType();
		if (resultType != null) {
			observableDto.setResultType(resultType.toString());
		} else {
			observableDto.setResultType(OBJECT_TYPE_NAME);
		}
		Type processType = refactoringVisitor.getProcessType();
		if (processType != null) {
			observableDto.setProcessType(processType.toString());
		} else {
			observableDto.setProcessType(OBJECT_TYPE_NAME);
		}
		if (refactoringVisitor.getDoInBackgroundBlock() != null) {
			Optional<TypeDeclaration> typeDeclaration = ASTNodes.findParent(refactoringVisitor.getDoInBackgroundBlock(), TypeDeclaration.class);
			
			if (typeDeclaration.isPresent()) {
				String block = refactoringVisitor.getDoInBackgroundBlock().toString();
				String className = typeDeclaration.get().getName().getIdentifier();
				
				observableDto.setDoInBackgroundBlock(specifyClassOfKeywordThis(block, className));
			} else {
				String block = refactoringVisitor.getDoInBackgroundBlock().toString();
				
				observableDto.setDoInBackgroundBlock(block);
			}
			
			
		}
		return observableDto;
	}

	protected RxObserverModel createObserverDto(String observerName, SwingWorkerWrapper refactoringVisitor,
			RxObservableModel observableDto) {
		RxObserverModel observerDto = new RxObserverModel();
		observerDto.setObserverName(observerName);
		Type resultType = refactoringVisitor.getResultType();
		if (resultType != null) {
			observerDto.setResultType(resultType.toString());
		} else {
			observerDto.setResultType(OBJECT_TYPE_NAME);
		}
		Type processType = refactoringVisitor.getProcessType();
		if (processType != null) {
			observerDto.setProcessType(processType.toString());
		} else {
			observerDto.setProcessType(OBJECT_TYPE_NAME);
		}
		observerDto.setObservableName(observableDto.getVarName());
		observerDto.setChunksName(refactoringVisitor.getProcessVariableName());
		Block processBlock = refactoringVisitor.getProcessBlock();
		if (processBlock != null) {
			observerDto.setProcessBlock(processBlock.toString());
		}
		Block doneBlock = refactoringVisitor.getDoneBlock();
		if (doneBlock != null) {
			observerDto.setDoneBlock(doneBlock.toString());
		}
		return observerDto;
	}

	protected void removeSuperInvocations(SwingWorkerWrapper refactoringVisitor) {
		for (SuperMethodInvocation methodInvocation : refactoringVisitor.getSuperMethodInvocationsToRemove()) {
			Statement statement = ASTNodes.findParent(methodInvocation, Statement.class).get();
			statement.delete();
		}
	}

	protected SWSubscriberModel createSWSubscriberDto(String subscriberName, IRewriteCompilationUnit icu,
			SwingWorkerWrapper refactoringVisitor) {
		String nextObserverId = IdManager.getNextObserverId(icu);
		String className = "RxObserver" + nextObserverId;
		subscriberName = subscriberName + nextObserverId;
		SWSubscriberModel model = new SWSubscriberModel();
		Type resultType = refactoringVisitor.getResultType();
		if (resultType != null) {
			model.setResultType(resultType.toString());
		} else {
			model.setResultType(OBJECT_TYPE_NAME);
		}
		Type processType = refactoringVisitor.getProcessType();
		if (processType != null) {
			model.setProcessType(processType.toString());
		} else {
			model.setProcessType(OBJECT_TYPE_NAME);
		}
		model.setClassName(className);
		model.setSubscriberName(subscriberName);
		model.setChunksName(refactoringVisitor.getProcessVariableName());
		Block processBlock = refactoringVisitor.getProcessBlock();
		if (processBlock != null) {
			model.setProcessBlock(processBlock.toString());
		}
		Block doneBlock = refactoringVisitor.getDoneBlock();
		if (doneBlock != null) {
			model.setDoneBlock(doneBlock.toString());
		}
		String block = refactoringVisitor.getDoInBackgroundBlock().toString();
		model.setDoInBackgroundBlock(specifyClassOfKeywordThis(block, className));
		for (FieldDeclaration fieldDeclaration : refactoringVisitor.getFieldDeclarations()) {
			model.getFieldDeclarations().add(fieldDeclaration.toString());
		}
		for (MethodDeclaration methodDeclaration : refactoringVisitor.getAdditionalMethodDeclarations()) {
			/*
			 * overriden methods and methods which have super.xxx call should not be added
			 * as additional methods. Sceanrio(super.xxx call) :
			 * 32--guigarage--JavaVersionChanger : JavaVersionChanger.java Scenario :
			 * 33--kirillcool--flamingo : Sometimes actionePerformed , completed (methods
			 * which are part of some anonymous class declaration) methods should not be
			 * conisdered as extra methods and hence should not be added twice(because they
			 * have already added via process or done blocks. E.g.
			 * AbstractFileViewPanel.java & SvgFileViewPanel.java Scenario :
			 * 46--ggasoftware--indigo : Sometimes methods like compare, compareTo should
			 * not be considered as extra methods as they have already added via
			 * doInBackground block. E.g. MainFrame.java
			 */
			if ((methodDeclaration.modifiers() != null
					&& !methodDeclaration.modifiers().toString().contains("@Override"))
					&& (methodDeclaration.getBody() != null
							&& !methodDeclaration.getBody().toString().contains("super."))
					&& !(methodDeclaration.getParent() != null
							&& methodDeclaration.getParent() instanceof AnonymousClassDeclaration
							&& processBlock == null && doneBlock == null
							&& methodDeclaration.getName().toString().equals("compare"))) {
				model.getMethods().add(methodDeclaration.toString());
			}
		}
		for (TypeDeclaration typeDeclaration : refactoringVisitor.getTypeDeclarations()) {
			model.getTypeDeclarations().add(typeDeclaration.toString());
		}

		return model;
	}

	private String specifyClassOfKeywordThis(String block, String className) {
		String replaceString = className.isEmpty() ? "this" : className + ".this";
		return block.replaceAll("\\bthis\\b", replaceString);

//		StringBuilder sb = new StringBuilder(block);
//		List<Integer> indexes = new ArrayList<>();
//		int startIndex = 0;
//		while (true) {
//			int i = block.indexOf(KEYWORD_THIS_WITH_DOT, startIndex);
//			if (i == -1) {
//				break;
//			}
//			indexes.add(i);
//			startIndex = i + KEYWORD_THIS_WITH_DOT.length();
//		}
//
//		int adjustmentFactor = 0;
//		for (Integer index : indexes) {
//			int newIndex = index + adjustmentFactor;
//			if (index == 0 || (index > 0 && block.charAt(index - 1) != '_' && block.charAt(index - 1) != '.'
//					&& block.charAt(index - 1) != '$'
//					&& (block.charAt(index - 1) < 'a' || block.charAt(index - 1) > 'Z'))) {
//				sb.replace(newIndex, newIndex + KEYWORD_THIS_WITH_DOT.length(), className);
//				adjustmentFactor += className.length() - KEYWORD_THIS_WITH_DOT.length();
//			}
//		}
//		/*
//		 * While refactoring doInBackground blocks, only this.methodname should be
//		 * applied for protected methods which are present in swingworker api like
//		 * publish(), process() etc. Right now only one case is discovered for publish
//		 * method. Scenario: 37--KolakCC--lol-jclient : ChampionsPanel.java
//		 */
//		String doInBgBlockString = "";
//		if (sb.toString().contains(KEYWORD_THIS_WITH_DOT + "publish(")) {
//			doInBgBlockString = sb.toString().replaceAll(className + "publish", KEYWORD_THIS_WITH_DOT + "publish");
//			return doInBgBlockString;
//		}
//		/*
//		 * Scenario: 59--locked-fg--JFeatureLib : ThreadWrapper.java Some cases where
//		 * statements like addPropertyChangeListener(this)(from doInbackground()) method
//		 * might come under SWEmitter class. This will show error and hence
//		 * addPropertyChangeListener(classname.this) needs to be applied.
//		 */
//		else if (sb.toString() != null && sb.toString().contains("descriptor.addPropertyChangeListener(this);")
//				&& className.contains("ThreadWrapper")) {
//			doInBgBlockString = sb.toString().replace("descriptor.addPropertyChangeListener(this)",
//					"descriptor.addPropertyChangeListener(ThreadWrapper.this)");
//			return doInBgBlockString;
//		}
//		/*
//		 * Scenario: 43--kparal--esmska : UpdateInstaller.java Statements like
//		 * dl.execute() needs to be replaced with dl.executeObservable()
//		 */
//		else if (sb.toString() != null && sb.toString().contains("dl.execute") && className.contains("Downloader")) {
//			doInBgBlockString = sb.toString().replaceAll("dl.execute", "dl.executeObservable");
//			return doInBgBlockString;
//		} else {
//			return sb.toString();
//		}
	}
}
