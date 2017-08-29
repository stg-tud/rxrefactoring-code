package de.tudarmstadt.rxrefactoring.ext.cfg.workers;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.analysis.ControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;

public class CFGCollector implements IWorker<Void, Void> {

	@Override
	public Void refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		
		MethodDeclarationCollector collector = new MethodDeclarationCollector();
		units.accept(collector);
		
		collector.declarations.forEach(m -> {
			Log.info(CFGCollector.class, "Build CFG for " + m.getName());
			ControlFlowGraph cfg = ControlFlowGraph.from(m.getBody());
			Log.info(CFGCollector.class, cfg.edgeSet());
		});
		
		return null;
	}
	
	
	class MethodDeclarationCollector extends ASTVisitor {
		
		final Set<MethodDeclaration> declarations = Sets.newHashSet();
		
		@Override
		public boolean visit(MethodDeclaration node) {
			if (node.getBody() != null) {
				declarations.add(node);
			}
			
			return false;
		}
	}

	

}
