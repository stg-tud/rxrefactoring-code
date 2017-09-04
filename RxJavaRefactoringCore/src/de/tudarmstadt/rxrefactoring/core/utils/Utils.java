package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.function.Consumer;

public class Utils {
	
	public static <X> void doWithNonNull(X x, Consumer<X> f) {
		if (x != null) {
			f.accept(x);
		}		
	}
	
	

}
