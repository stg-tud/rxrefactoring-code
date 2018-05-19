package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.NotConvergingException;
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

	@Override
	public @Nullable Map<ASTNode, UseDef> refactor(@NonNull IProjectUnits units, @Nullable Void input,
			@NonNull WorkerSummary summary) throws Exception {

		final Map<ASTNode, UseDef> result = Maps.newHashMap();

		for (IRewriteCompilationUnit unit : units) {
			CompilationUnitAnalysis cua = new CompilationUnitAnalysis(unit, result);
			cua.analyze();			
		}		
		
		//result.forEach((node, use) -> Log.info(getClass(), "Node: " + node + "\n" + "Use: " + use));	
		return result;
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
		
		
		private void analyzeBodyDeclarations(List<BodyDeclaration> bodyDeclarations) {
			
			for (Object o : bodyDeclarations) {
				if (o instanceof TypeDeclaration) {
					analyzeTypeDeclaration((TypeDeclaration) o);
				} else if (o instanceof MethodDeclaration) {
					analyzeMethodDeclaration((MethodDeclaration) o);
				} else if (o instanceof FieldDeclaration) {
					analyzeFieldDeclaration((FieldDeclaration) o);
				} else if (o instanceof Initializer) {
					analyzeInitializer((Initializer) o);
				} else {
					//Enum and Annotation declarations not included
					
				}
			}			
		}
		
		
		private void analyzeTypeDeclaration(TypeDeclaration type) {
			analyzeBodyDeclarations(type.bodyDeclarations());
		}
		
		
		private void analyzeMethodDeclaration(MethodDeclaration m) {
			Block body = m.getBody();
			
			if (body == null) {
				return;
			}
			
			analyzeBlock(body);			
		}
		
		
		private void analyzeFieldDeclaration(FieldDeclaration f) {
			//Do something with fields
		}
		
		private void analyzeInitializer(Initializer i) {
			analyzeBlock(i.getBody());
		}
		
		private void analyzeLambdaExpression(LambdaExpression l) {
			ASTNode body = l.getBody();
			if (body instanceof Block)
				analyzeBlock((Block) body);
		}
		
		private void analyzeVariableFragments(List<VariableDeclarationFragment> fragments) {
			//Do something with variable declarations
		}		
		
				
		private void analyzeBlock(Block block) {
			AST ast = unit.getAST();
			
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
			
			//Analyze subclasses that are part of a block
			block.accept(new ASTVisitor() {								
				@Override
				public boolean visit(TypeDeclaration type) {
					analyzeTypeDeclaration(type);
					return false;				
				}				
				@Override
				public boolean visit(AnonymousClassDeclaration decl) {
					analyzeBodyDeclarations(decl.bodyDeclarations());
					return false;
				}
				
				@Override
				public boolean visit(LambdaExpression l) {
					analyzeLambdaExpression(l);
					return false;
				}
			});
		}
	}

}
