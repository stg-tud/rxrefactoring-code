package de.tudarmstadt.rxrefactoring.ext.template.workers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleType;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

public class TemplateWorker implements IWorker<TemplateCollector, Void>{
	
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TemplateCollector input,
			@NonNull WorkerSummary summary) throws Exception {
		
		// TODO 14 - Get the collected nodes that are relevant for this worker.
		Multimap<IRewriteCompilationUnit, ASTNode> collectedNodes = input.getCollectedNodes();
		
		for (IRewriteCompilationUnit unit : units) {
			
			// Get AST from the unit.
			AST ast = unit.getAST();
			
			for (ASTNode node : collectedNodes.get(unit)) {
				
				/**
				 * TODO 15 - If required, adapt imports with "addImport(String qualifiedTypeName)" 
				 * and "removeImport(String qualifiedTypeName)" calls to the compilation unit.
				 */
				
				/**
				 * TODO 16 - Form the new construct with calls to the AST. Consider placing 
				 * methods for the construction of refactored nodes in a ".utils" subpackage. 
				 * Here, a SimpleType node is created as an example.
				 */
				SimpleType newNode = ast.newSimpleType(ast.newSimpleName("name"));
				
				/**
				 * TODO 17 - Replace the original node with the refactored one with a 
				 * "replace(ASTNode node, ASTNode replacement)" call to the compilation unit, 
				 * or delete nodes with "remove(ASTNode node)".
				 */
				unit.replace(node, newNode);
				
				/**
				 * TODO 18 - If a construct could not be refactored, note this in the summary as 
				 *  "summary.addSkipped(String key)", "otherwise note summary.addCorrect(String key)"
				 */
				summary.addCorrect("templateCreation");
			}
			
		}
		return null;
	}

	@Override
	public @Nullable Void refactor(IProjectUnits units, TemplateCollector input, WorkerSummary summary,
			RefactorScope scope) throws Exception {
		// TODO Auto-generated method stub
		//only needed if RefactorScope is implemented in this extension
		return null;
	}

}
