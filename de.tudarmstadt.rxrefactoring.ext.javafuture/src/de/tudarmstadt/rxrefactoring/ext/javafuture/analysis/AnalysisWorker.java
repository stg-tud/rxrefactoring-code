package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;

/**
 * Adds analysis results to the AST.
 * 
 * @author mirko
 *
 */
public class AnalysisWorker implements IWorker<Void, Void> {

	
	private static DataFlowAnalysis<ASTNode,String> analysis = 
			DataFlowAnalysis.create(null, null);
		
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable Void input, @NonNull WorkerSummary summary)
			throws Exception {
		
		
		units.accept(new UnitASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				analysis.apply(ProgramGraph.createFrom(node.getBody()), analysis.astExecutor());
				return true;
			}
		});
		
		
						
		
		
		return null;
	}

}
