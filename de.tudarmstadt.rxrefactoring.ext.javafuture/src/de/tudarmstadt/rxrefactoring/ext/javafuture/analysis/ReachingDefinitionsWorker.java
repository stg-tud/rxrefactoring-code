package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.ReachingDefinition;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.ReachingDefinitionsAnalysis;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

/**
 * Adds analysis results to the AST.
 * 
 * @author mirko
 *
 */
public class ReachingDefinitionsWorker implements IWorker<Void, Map<ASTNode, ReachingDefinition>> {

	
	private static DataFlowAnalysis<ASTNode, ReachingDefinition> analysis = 
			ReachingDefinitionsAnalysis.create();
			//DataFlowAnalysis.create(null, null);
		
	@Override
	public @Nullable Map<ASTNode, ReachingDefinition> refactor(@NonNull IProjectUnits units, @Nullable Void input, @NonNull WorkerSummary summary)
			throws Exception {
			
		final Map<ASTNode, ReachingDefinition> result = Maps.newHashMap();
		
		units.accept(new UnitASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				Log.info(getClass(), "method: " + node.getName());
				result.putAll(analysis.apply(ProgramGraph.createFrom(node.getBody()), analysis.mapExecutor()));
				return false;
			}
		});		
		
		return result;
	}

}
