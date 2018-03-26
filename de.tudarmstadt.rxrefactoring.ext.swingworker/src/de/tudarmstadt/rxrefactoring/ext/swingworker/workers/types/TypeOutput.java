package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactorInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.RxCollector;

public class TypeOutput {
	@NonNull public final RxCollector collector;
	@NonNull public final RefactorInfo info;
	
	public TypeOutput(RxCollector collector, RefactorInfo info) {
		super();
		this.collector = Objects.requireNonNull(collector);
		this.info = Objects.requireNonNull(info);
	} 	
}


