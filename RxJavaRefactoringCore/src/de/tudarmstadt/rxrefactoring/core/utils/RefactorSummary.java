package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Set;

import org.eclipse.core.resources.IProject;

import com.google.common.collect.Sets;

public class RefactorSummary {

	
	private final Set<ProjectSummary> projects;
	
	//fields for storing the start and end time of the refactoring
	private long startTime;
	private long finishTime;
	
	public RefactorSummary() {
		this.projects = Sets.newHashSet();
	}
	
	public void reportProject(IProject project, Status status) {
		projects.add(new ProjectSummary(project, status));
	}
	
	public void reportStarted() {
		startTime = System.nanoTime();
	}
	
	public void reportFinished() {
		finishTime = System.nanoTime();
	}
	
	
	/**
	 * Specifies a status that a refactoring may have.
	 * @author mirko
	 *
	 */
	public enum Status {
		ERROR, SKIPPED, COMPLETED
	}
	
	
	class ProjectSummary {
		
		private final IProject project;
		private final Status status; 
		
		public ProjectSummary(IProject project, Status status) {
			this.project = project;
			this.status = status;
		}		
	}
	
	
}
