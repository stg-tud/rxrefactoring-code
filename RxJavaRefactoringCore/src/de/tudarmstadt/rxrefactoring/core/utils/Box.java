package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Iterator;
import java.util.Objects;

public class Box<T> implements Iterable<T> {

	private T value;

	public Box(T value) {
		this.value = value;
	}
	
	public Box() {
		this(null);
	}
	
	public void set(T value) {
		this.value = value;
	}
	
	public T get() {
		return value;
	}
	
	public int hashCode() {
		return Objects.hashCode(value);
	}
	
	public boolean equals(Object other) {
		return other instanceof Box && Objects.equals(((Box<?>)other).value, value);
	}
	
	@Override
	public String toString() {
		return "Box[" + value + "]";
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			boolean isDone = false;
			
			@Override
			public boolean hasNext() {
				return !isDone;
			}

			@Override
			public T next() {
				isDone = true;
				return value;
			}
			
		};
	}
	
	
	
	

	
}
