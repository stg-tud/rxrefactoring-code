package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.workers.IWorker;

public class RefactorSummary {

	
	private final Set<ProjectSummary> projects;
	
	//fields for storing the start and end time of the refactoring
	private long startTime;
	private long finishTime;
	
	public RefactorSummary() {
		this.projects = Sets.newHashSet();
	}
	
	public ProjectSummary reportProject(IProject project) {
		ProjectSummary result = new ProjectSummary(project);		
		projects.add(result);
		return result;
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
	public enum ProjectStatus {
		UNDEFINED, ERROR, SKIPPED, COMPLETED
	}	
	
	public static class ProjectSummary {
		
		private final IProject project;
		private ProjectStatus status; 
		
		private final Set<WorkerSummary> workers;
		
		ProjectSummary(IProject project) {
			this.project = project;
			this.status = ProjectStatus.UNDEFINED;
			
			workers = Sets.newHashSet();
		}		
		
		public void reportStatus(ProjectStatus status) {
			Objects.requireNonNull(status);			
			this.status = status;
		}
		
		public WorkerSummary reportWorker(IWorker worker) {
			WorkerSummary result = new WorkerSummary(worker);
			workers.add(result);
			return result;
		}
	}
	
	public enum WorkerStatus {
		UNDEFINED, ERROR, COMPLETED
	}
	
	public static class WorkerSummary {
		
		private final IWorker<?,?> worker;
		
		private WorkerStatus status;
		private final Map<String, CountEntry> entries;
		
		
		WorkerSummary(IWorker<?,?> worker) {
			this.worker = worker;
			
			this.status = WorkerStatus.UNDEFINED;			
			this.entries = Maps.newHashMap();
		}
		
		public void addCorrect(String key) {
			getEntryFor(key).addCorrect();			
		}
		
		public void addSkipped(String key) {
			getEntryFor(key).addSkipped();			
		}
		
		public void setStatus(WorkerStatus status) {
			Objects.requireNonNull(status);
			this.status = status;
		}
		
		private CountEntry getEntryFor(String key) {
			Objects.requireNonNull(key, "The key can not be null.");
			
			CountEntry entry = entries.get(key);
			
			if (entry == null) {
				entry = new CountEntry();
				entries.put(key, entry);
			}
			
			return entry;
		}
		
		class CountEntry {
			private int correct = 0;
			private int skipped = 0;
			
			public void addCorrect() {
				correct++;
			}
			
			public void addSkipped() {
				skipped++;				
			}		
			
		}
	}
	
	
}
