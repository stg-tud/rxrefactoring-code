package de.tudarmstadt.rxrefactoring.ext.javafuture.domain;

import java.util.List;

public class ClassInfo {
	
	private final String name;
	private final String binaryName;
	private final List<String> publicMethods;
	private final List<String> unsupportedMethods;
	
	public ClassInfo(String name, String binaryName, List<String> publicMethods, List<String> unsupportedMethods) {
		this.name = name;
		this.binaryName = binaryName;
		this.publicMethods = publicMethods;
		this.unsupportedMethods = unsupportedMethods;
	}

	public String getName() {
		return name;
	}

	public String getBinaryName() {
		return binaryName;
	}


	public List<String> getPublicMethods() {
		return publicMethods;
	}

	public List<String> getUnsupportedMethods() {
		return unsupportedMethods;
	}
}
