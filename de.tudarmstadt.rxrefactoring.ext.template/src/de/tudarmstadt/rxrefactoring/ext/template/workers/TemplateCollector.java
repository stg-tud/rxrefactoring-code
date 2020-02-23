package de.tudarmstadt.rxrefactoring.ext.template.workers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorkerV1;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;

public class TemplateCollector implements IWorkerV1<TemplatePreconditionWorker, TemplateCollector>{
	
	/**
	 * TODO 8 - Determine how the collector will interact with the other workers.
	 * 
	 * If only one type of node is collected, the refactor method can directly return a 
	 * Multimap of the collected nodes (and the output type of the collector class should 
	 * be a Multimap of that node type).
	 * 
	 * Otherwise, keep one Multimap for each type of node that is collected. 
	 * Each map should store for each unit all relevant nodes of that type. In that case, 
	 * the refactor method should return the collector instance.
	 */
	
	private final Multimap<IRewriteCompilationUnit, ASTNode> collectedNodes = HashMultimap.create();
	
	// TODO 9 - Implement appropriate getters.
	public Multimap<IRewriteCompilationUnit, ASTNode> getCollectedNodes() {
		return collectedNodes;
	}
	
	@Override
	public @Nullable TemplateCollector refactor(@NonNull IProjectUnits units, @Nullable TemplatePreconditionWorker input, @NonNull WorkerSummary summary) throws Exception {

		// TODO 10 - Instantiate visitors.
		TemplateVisitor visitor = new TemplateVisitor();
		
		// TODO 11 - Make "accept" call for each of the visitors.
		units.accept(visitor);
		
		summary.setCorrect("numberOfCompilationUnits", units.size());	
		return this;
	}
	
	/**
	 * TODO 12 - Implement visitor classes to visit the ASTNodes of each compilation unit
	 * and collect relevant nodes and information in the collector fields.
	 * 
	 * If a precondition analysis was performed, use this information to determine which 
	 * nodes to collect.
	 */
	class TemplateVisitor extends UnitASTVisitor{
		
		/**
		 * TODO 13 - Override a visit method for each type of ASTNode that is to be visited. 
		 * Here, MethodDeclaration was used as an example. Returning true means the node's
		 * children will be visited next.
		 */
		public boolean visit(MethodDeclaration node) {
			return true;
		}
		
	}

}
