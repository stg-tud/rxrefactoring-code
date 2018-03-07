package de.tudarmstadt.rxrefactoring.core.analysis.classes;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;

public class ClassAnalysis<Result> {
	
	private final DataFlowAnalysis<ASTNode, Result> analysis;
	
	public ClassAnalysis(DataFlowAnalysis<ASTNode, Result> analysis) {
		this.analysis = analysis;
	}
	
	public void analyze(TypeDeclaration decl) {
		
		for (Object o : decl.bodyDeclarations()) {
			
			//TODO: Add other declaration types
			if (o instanceof MethodDeclaration) {
				MethodDeclaration m = (MethodDeclaration) o;
				
				ProgramGraph p = ProgramGraph.createFrom(m.getBody());
				analysis.apply(p, analysis.astExecutor());
				
			} else if (o instanceof FieldDeclaration) {
				FieldDeclaration f = (FieldDeclaration) o;
			}
		}
	}

}
