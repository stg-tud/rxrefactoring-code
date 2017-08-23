package de.tudarmstadt.rxrefactoring.ext.akkafuture.domain;

public class ObservableInfo {
	public static final String name = "Observable";
	public static final String binaryName = "rx.Observable";
	
	public String generateName(String oldName) {
		return oldName + "Observable";
	}
	
	public String generateCollectionName(String oldName) {
		return oldName + "Observables";
	}
}
