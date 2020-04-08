package de.tudarmstadt.rxrefactoring.ext.template.workers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.NotConvergingException;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDefAnalysis;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

public class TemplateUseDef implements IWorker<Void, Collection<UseDef>> {

	private static DataFlowAnalysis<ASTNode, UseDef> analysis = UseDefAnalysis.create();

	@Override
	public @Nullable Collection<UseDef> refactor(@NonNull IProjectUnits units, @Nullable Void input,
			@NonNull WorkerSummary summary) throws Exception {

		final Map<ASTNode, UseDef> result = Maps.newHashMap();

		for (IRewriteCompilationUnit unit : units) {
			CompilationUnitAnalysis cua = new CompilationUnitAnalysis(unit, result);
			cua.analyze();			
		}		
		
		return result.values();
	}
	
	class CompilationUnitAnalysis {
		
		private final IRewriteCompilationUnit unit;
		private final Map<ASTNode, UseDef> result;
		
		public CompilationUnitAnalysis(IRewriteCompilationUnit unit, Map<ASTNode, UseDef> result) {
			this.unit = unit;
			this.result = result;
		}
		
		public void analyze() {
			unit.findPrimaryTypeDeclaration().ifPresent(this::analyzeTypeDeclaration);	
		}
		
		private void analyzeTypeDeclaration(TypeDeclaration type) {
			analyzeBodyDeclarations(type.bodyDeclarations());
		}
		
		private void analyzeBodyDeclarations(List<BodyDeclaration> bodyDeclarations) {
			/**
			 * TODO 5 - analyze internal declarations in a TypeDeclaration, such as anonymous 
			 * class declarations and method declarations.
			 */
			for (Object o : bodyDeclarations) {
				if (o instanceof TypeDeclaration) {
					analyzeTypeDeclaration((TypeDeclaration) o);
				} else if (o instanceof MethodDeclaration) {
					analyzeMethodDeclaration((MethodDeclaration) o);
				} else {
					//TODO
				}
			}			
		}
		
		private void analyzeMethodDeclaration(MethodDeclaration m) {
			/**
			 * This example analyzes the block inside a MethodDeclaration
			 */
			Block body = m.getBody();
			if (body == null) {
				return;
			}
			analyzeBlock(body);			
		}
		
		private void analyzeBlock(Block block) {
			Map<ASTNode, UseDef> tempResult;
			
			try {
				tempResult = analysis.apply(ProgramGraph.createFrom(block), analysis.mapExecutor());				
			} catch (NotConvergingException e) {
				// TODO Handle case when the dataflow analysis does not yield a correct result.
				tempResult = (Map<ASTNode, UseDef>) e.getUnfinishedOutput();
				Log.error(getClass(), "The dataflow analysis did not converge. Continue with unfinished result.");
				e.printStackTrace();
			}
			
			result.putAll(tempResult);
			
			//TODO 6 - Analyze subclasses that are part of a block
			
		}
	}
}
