package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.Set;

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
import de.tudarmstadt.rxrefactoring.core.analysis.impl.VariableNameAnalysis;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

/**
 * Adds analysis results to the AST.
 * 
 * @author mirko
 *
 */
public class AnalysisWorker implements IWorker<Void, Void> {

	
	private static DataFlowAnalysis<ASTNode, Set<String>> analysis = 
			VariableNameAnalysis.create();
			//DataFlowAnalysis.create(null, null);
		
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable Void input, @NonNull WorkerSummary summary)
			throws Exception {
		
		
		units.accept(new UnitASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				Log.info(getClass(), "method: " + node.getName());
				analysis.apply(ProgramGraph.createFrom(node.getBody()), analysis.astExecutor());
				return false;
			}
		});
		
		
						
		
		
		return null;
	}

}
