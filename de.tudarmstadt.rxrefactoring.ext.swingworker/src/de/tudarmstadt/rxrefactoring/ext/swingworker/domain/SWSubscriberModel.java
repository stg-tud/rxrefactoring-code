package de.tudarmstadt.rxrefactoring.ext.swingworker.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/27/2016<br>
 * Adapted to new core by Camila Gonzalez on 18/01/2018
 */
public class SWSubscriberModel {
	private String resultType;
	private String processType;
	private String doInBackgroundBlock;
	private String className;
	private String subscriberName;
	private String chunksName;
	private String processBlock;
	private String doneBlock;
	private List<String> fieldDeclarations;
	private List<String> methods;
	private List<String> typeDeclarations;

	public SWSubscriberModel() {
		fieldDeclarations = new ArrayList<>();
		methods = new ArrayList<>();
		typeDeclarations = new ArrayList<>();
	}

	public String getSubscriberName() {
		return subscriberName;
	}

	public void setSubscriberName(String subscriberName) {
		this.subscriberName = subscriberName;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	public String getProcessType() {
		return processType;
	}

	public void setProcessType(String processType) {
		this.processType = processType;
	}

	public String getDoInBackgroundBlock() {
		return doInBackgroundBlock;
	}

	public void setDoInBackgroundBlock(String doInBackgroundBlock) {
		this.doInBackgroundBlock = doInBackgroundBlock;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getChunksName() {
		return chunksName;
	}

	public void setChunksName(String chunksName) {
		this.chunksName = chunksName;
	}

	public String getProcessBlock() {
		return processBlock;
	}

	public void setProcessBlock(String processBlock) {
		this.processBlock = processBlock;
	}

	public String getDoneBlock() {
		return doneBlock;
	}

	public void setDoneBlock(String doneBlock) {
		this.doneBlock = doneBlock;
	}

	public List<String> getFieldDeclarations() {
		return fieldDeclarations;
	}

	public void setFieldDeclarations(List<String> fieldDeclarations) {
		this.fieldDeclarations = fieldDeclarations;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	public List<String> getTypeDeclarations() {
		return typeDeclarations;
	}

	public void setTypeDeclarations(List<String> typeDeclarations) {
		this.typeDeclarations = typeDeclarations;
	}
}
