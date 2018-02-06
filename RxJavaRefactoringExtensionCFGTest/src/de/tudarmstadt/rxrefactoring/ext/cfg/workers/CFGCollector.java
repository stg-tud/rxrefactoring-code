package de.tudarmstadt.rxrefactoring.ext.cfg.workers;


import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.analysis.Analyses;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.utils.Log;


public class CFGCollector implements IWorker<Void, Void> {
	
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable Void input, @NonNull WorkerSummary summary)
			throws Exception {
		
		MethodDeclarationCollector collector = new MethodDeclarationCollector();
		units.accept(collector);
		
		collector.declarations.forEach(m -> {
			Log.info(CFGCollector.class, "### Build CFG for " + m.getName() + " ###");
			ProgramGraph cfg = ProgramGraph.createFrom(m.getBody());
			Log.info(CFGCollector.class, cfg.listEdges());
			
			Log.info(CFGCollector.class, "### Start Analysis for " + m.getName() + " ###");
			DataFlowAnalysis analysis = Analyses.VariableNameAnalysis.create();
			Object result = analysis.apply(cfg, analysis.mapExecutor());	
			Log.info(CFGCollector.class, result);
		});
		
		
		
		
//		ReactiveObject builder = new ReactiveObject(simpleName("ReactiveTest"));
//				
//		IReactiveInput reactiveInput = new ComplexReactiveInput(simpleType("Integer"), simpleName("input"), SchedulerBuilder.schedulersComputation(), null);	
//		builder.addInput("input", reactiveInput);
//		
//		ReactiveOutput reactiveOutput = new ReactiveOutput(simpleType("String"), simpleName("output"), null, null);
//		builder.addOutput("output", reactiveOutput);
//		
//		final ConsumerBuilder consumer = new ConsumerBuilder(
//				reactiveInput.supplyType(), 
//				simpleName("x"), 
//				unit -> {
//					Block block = unit.getAST().newBlock();
//					return block;
//				});
//		
//		ReactiveComputation reactiveCompute = new ReactiveComputation(
//				reactiveInput, 
//				simpleName("internal"), 
//				consumer.supplyClassInstanceCreation(),
//				SchedulerBuilder.schedulersComputation());
//		
//		builder.addComputation("internal", reactiveCompute);
//		
//		
//		
//		/*
//		 * Add class declarations to body declarations
//		 */
//		for (RewriteCompilationUnit unit : units) {
//			unit.getRoot().accept(new ASTVisitor() {
//				public boolean visit(TypeDeclaration node) {
//					ListRewrite l = unit.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
////					l.insertFirst(builder.supplyTypeDeclaration().apply(unit), null);
//					return false;
//				}
//			});
//		}
		
		return null;
	}
			
	
	class MethodDeclarationCollector extends UnitASTVisitor {
		
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
