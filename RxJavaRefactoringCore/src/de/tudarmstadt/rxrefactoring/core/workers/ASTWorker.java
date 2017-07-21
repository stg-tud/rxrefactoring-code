package de.tudarmstadt.rxrefactoring.core.workers;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;

import de.tudarmstadt.rxrefactoring.core.collect.ASTCollector;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriters;

public abstract class ASTWorker extends AbstractWorker<ASTCollector> {

	public ASTWorker(ASTCollector collector) {
		super(collector);
	}
	
	protected abstract Visitor createVisitor(UnitWriter writer);
	protected abstract String[] imports();
	
	
	@Override
	public WorkerStatus refactor() {
		Log.info(getClass(), "Start refactoring...");
		
		for (ICompilationUnit unit : collector.getUnits()) {
			
			UnitWriter writer = 
					UnitWriters.getOrElse(unit, () -> new UnitWriter(unit, collector.getRootNode(unit).getAST(), getClass().getSimpleName()));				
			
			synchronized (writer) {
				Visitor v = createVisitor(writer);				
				collector.getRootNode(unit).accept(v);
				
				if (v.hasChanged()) {
					for (String imprt : imports()) writer.addImport(imprt);
					execution.addUnitWriter(writer);
				}
			}			
		}		
		
		return WorkerStatus.OK;
	}
	
	protected abstract class Visitor extends ASTVisitor {
			
		protected final AST ast;
		protected final UnitWriter writer;
		
		private boolean hasChanged = false;
		
		public Visitor(UnitWriter writer) {
			this.writer = writer;			
			this.ast = writer.getAST();
		}		
		
		protected void setChanged() {
			hasChanged = true;
		}		
		
		public boolean hasChanged() {
			return hasChanged;
		}
	}

}
