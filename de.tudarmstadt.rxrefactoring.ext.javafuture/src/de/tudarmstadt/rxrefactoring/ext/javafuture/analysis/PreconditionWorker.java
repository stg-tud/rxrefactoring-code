package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;

public class PreconditionWorker implements IWorker<Void, Void>{

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable Void input, @NonNull WorkerSummary summary)
			throws Exception {

		units.forEach(unit -> {
			ASTNode root = unit.getRoot();
			
			System.out.println(root);
			
			ASTVisitor v = new ASTVisitor() {
				
				@Override
				public void preVisit(ASTNode node) {
					System.out.println("#######");
					System.out.println("node:");
					System.out.println(node);
					System.out.println("property:");
					System.out.println(node.getProperty(DataFlowAnalysis.ASTDataFlowExecution.DEFAULT_PROPERTY));
					System.out.println("#######");
				}
			};
			
			unit.accept(v);
			
						
			
		});
		
		
		return null;
	}

}
