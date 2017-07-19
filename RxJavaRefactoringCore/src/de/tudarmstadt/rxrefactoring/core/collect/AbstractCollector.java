package de.tudarmstadt.rxrefactoring.core.collect;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * Description: Abstract class for collectors. The main purpose of this class is
 * to allow polymorphism. So a list can contain different types of
 * collectors<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public abstract class AbstractCollector implements Collector {
	
	
	private final IJavaProject project;
	private final String name;

	public AbstractCollector(IJavaProject project, String name) {	
		this.name = name;
		this.project = project;
	}

	@Override
	public String getName()	{
		return name;
	}
	
	public IJavaProject getProject() {
		return project;
	}
}
