package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import com.google.common.base.Strings;
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
	
	private String getDurationAsString() {
		if (startTime == 0 || finishTime == 0) {
			return "undefined";
		} else {
			return String.format(Locale.ENGLISH, "%12.3fs", ((double)(finishTime - startTime)) / 1000 / 1000 / 1000);
		}
		
	}
	
	private String getProjectCountAsString() {
		int completed = 0, skipped = 0, error = 0;
		
		for (ProjectSummary project : projects) {
			switch (project.status) {
			case UNDEFINED: case SKIPPED:
				skipped++;
				break;
			case ERROR:
				error++;
				break;
			case COMPLETED:
				completed++;
				break;			
			}
		}
		
		return "completed/total: " + completed + "/" + (completed + skipped + error) + ", skipped or undefined: " + skipped + ", error: " + error; 
	}
	
	@Override
	public String toString() {
		
		String result =  ">>> Summary" +
				"\n" + fromPadding(1) + "Duration: " + getDurationAsString() +
				"\n" + fromPadding(1) + "Number of projects: " + getProjectCountAsString();
				
		for (ProjectSummary project : projects) {
			result += "\n" + project.toString(1);
		}
				
				
		result += "\n" + "<<<";
		return result;
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
		
		public WorkerSummary reportWorker(IWorker<?,?> worker) {
			WorkerSummary result = new WorkerSummary(worker);
			workers.add(result);
			return result;
		}
		
		@Override
		public String toString() {
			return toString(0);
		}
		
		public String toString(int padding) {
			String pad = fromPadding(padding);
			
			String result = pad + ">>> Project: " + project.getName() +
					"\n" + fromPadding(padding + 1) + "Status: " + status;
			
			for (WorkerSummary worker : workers) {
				result += "\n" + worker.toString(padding + 1);
			}
			
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
		
		public static WorkerSummary createNullSummary() {
			return new WorkerSummary(null);
		}
		
		public void addCorrect(String key) {
			getEntryFor(key).addCorrect();			
		}
		
		public void addSkipped(String key) {
			getEntryFor(key).addSkipped();			
		}
		

		public void setCorrect(String key, int correct) {
			getEntryFor(key).setCorrect(correct);
		}
		
		public void setSkipped(String key, int skipped) {
			getEntryFor(key).setSkipped(skipped);
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
		
		@Override
		public String toString() {
			return toString(0);
		}
		
		public String toString(int padding) {
			
			String pad = fromPadding(padding);			
			String result = pad + ">>> Worker: " + worker.getName() +
					"\n" + fromPadding(padding + 1) + "Status: " + status;
			
			for (Entry<String, CountEntry> entry : entries.entrySet()) {
				result += "\n" + fromPadding(padding + 1) + entry.getKey() + ": " + entry.getValue();
			}
			
			//result += pad + "<<<";
			return result;
					
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
			
			public void setCorrect(int correct) {
				this.correct = correct;
			}
			
			public void setSkipped(int skipped) {
				this.skipped = skipped;
			}
			
			@Override
			public String toString() {
				return correct + "/" + (correct + skipped);
			}
		}
	}
	
	private static String fromPadding(int padding) {
		return Strings.repeat("\t", padding); 
	}
	
	
}
