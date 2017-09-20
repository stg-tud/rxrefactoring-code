package de.tudarmstadt.rxrefactoring.ext.cfg.workers;

import static de.tudarmstadt.rxrefactoring.core.ir.NodeSupplier.*;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.Log;
import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.analysis.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.example.VariableNameAnalysis;
import de.tudarmstadt.rxrefactoring.core.ir.ComplexReactiveInput;
import de.tudarmstadt.rxrefactoring.core.ir.IReactiveInput;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveComputation;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveObject;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveOutput;
import de.tudarmstadt.rxrefactoring.core.ir.util.ConsumerBuilder;
import de.tudarmstadt.rxrefactoring.core.ir.util.SchedulerBuilder;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;


public class CFGCollector implements IWorker<Void, Void> {

	@Override
	public Void refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		
		MethodDeclarationCollector collector = new MethodDeclarationCollector();
		units.accept(collector);
		
		collector.declarations.forEach(m -> {
			Log.info(CFGCollector.class, "### Build CFG for " + m.getName() + " ###");
			ControlFlowGraph cfg = ControlFlowGraph.from(m.getBody());
			Log.info(CFGCollector.class, cfg.listEdges());
			
			Log.info(CFGCollector.class, "### Start Analysis for " + m.getName() + " ###");
			DataFlowAnalysis<?> analysis = new VariableNameAnalysis();
			Map<?, ?> result = analysis.apply(cfg);	
			Log.info(CFGCollector.class, result);
		});
		
		
		
		
		ReactiveObject builder = new ReactiveObject(simpleName("ReactiveTest"));
				
		IReactiveInput reactiveInput = new ComplexReactiveInput(simpleType("Integer"), simpleName("input"), SchedulerBuilder.schedulersComputation(), null);	
		builder.addInput("input", reactiveInput);
		
		ReactiveOutput reactiveOutput = new ReactiveOutput(simpleType("String"), simpleName("output"), null, null);
		builder.addOutput("output", reactiveOutput);
		
		final ConsumerBuilder consumer = new ConsumerBuilder(
				reactiveInput.supplyType(), 
				simpleName("x"), 
				unit -> {
					Block block = unit.getAST().newBlock();
					return block;
				});
		
		ReactiveComputation reactiveCompute = new ReactiveComputation(
				reactiveInput, 
				simpleName("internal"), 
				consumer.supplyClassInstanceCreation(),
				SchedulerBuilder.schedulersComputation());
		
		builder.addComputation("internal", reactiveCompute);
		
		
		
		/*
		 * Add class declarations to body declarations
		 */
		for (RewriteCompilationUnit unit : units) {
			unit.getRoot().accept(new ASTVisitor() {
				public boolean visit(TypeDeclaration node) {
					ListRewrite l = unit.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
//					l.insertFirst(builder.supplyTypeDeclaration().apply(unit), null);
					return false;
				}
			});
		}
		
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
