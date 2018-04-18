package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDefAnalysis;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

/**
 * Adds analysis results to the AST.
 * 
 * @author mirko
 *
 */
public class UseDefWorker implements IWorker<Void, Map<ASTNode, UseDef>> {

	private static DataFlowAnalysis<ASTNode, UseDef> analysis = UseDefAnalysis.create();
	// DataFlowAnalysis.create(null, null);

	@Override
	public @Nullable Map<ASTNode, UseDef> refactor(@NonNull IProjectUnits units, @Nullable Void input,
			@NonNull WorkerSummary summary) throws Exception {

		final Map<ASTNode, UseDef> result = Maps.newHashMap();

		units.accept(new UnitASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				Log.info(getClass(), "method: " + node.resolveBinding().getName() + " --- " + node.resolveBinding().getDeclaringClass());
				result.putAll(analysis.apply(ProgramGraph.createFrom(node.getBody()), analysis.mapExecutor()));
				return false;
			}
		});

		result.forEach((node, use) -> Log.info(getClass(), "Node: " + node + "\n" + "Use: " + use));

		return result;
	}

}
