package de.tudarmstadt.rxrefactoring.core.analysis.dataflow;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;

public class NotConvergingException extends Exception {
	
	private static final long serialVersionUID = -7940820475651429116L;
	
	private final @NonNull IControlFlowGraph<?> cfg;
	private final int maximumIterations;
	
	private final Object output;
	
	public NotConvergingException(@NonNull IControlFlowGraph<?> cfg, int maximumIterations, Object output) {
		super("The data flow analysis did not converge to a result in " + maximumIterations + " iterations.");
		
		this.maximumIterations = maximumIterations;
		this.cfg = cfg;
		this.output = output;
	}
	
	public @NonNull IControlFlowGraph<?> getControlFlowGraph() {
		return cfg;
	}
	
	public int getMaximumIterations() {
		return maximumIterations;
	}
	
	public Object getUnfinishedOutput() {
		return output;
	}
	

}
