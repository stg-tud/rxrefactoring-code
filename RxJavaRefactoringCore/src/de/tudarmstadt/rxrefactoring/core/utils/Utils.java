package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public class Utils {
	
	public static <X> void doWithNonNull(X x, Consumer<X> f) {
		if (x != null) {
			f.accept(x);
		}		
	}

}
