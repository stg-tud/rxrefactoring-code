package de.tudarmstadt.rxrefactoring.core.writers;

public interface UnitWriterFactory<T extends UnitWriter> {
	
	public T create();
}
