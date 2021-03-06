package de.tudarmstadt.rxrefactoring.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary.CountEntry;

public class RefactorSummary {

	private final Set<ProjectSummary> projects;
	private final String name;

	// fields for storing the start and end time of the refactoring
	private long startTime;
	private long finishTime;

	public RefactorSummary(String name) {
		this.projects = Sets.newHashSet();
		this.name = name;
	}

	public @NonNull ProjectSummary reportProject(IProject project) {
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
			long totalTime = finishTime - startTime;
			return String.format(Locale.ENGLISH, "%12.3fs (%dms)", ((double) (totalTime)) / 1000000000,
					totalTime / 1000000);
		}

	}

	private String getProjectCountAsString() {
		int completed = 0, skipped = 0, error = 0;

		for (ProjectSummary project : projects) {
			switch (project.status) {
			case UNDEFINED:
			case SKIPPED:
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

		return "completed/total: " + completed + "/" + (completed + skipped + error) + ", skipped or undefined: "
				+ skipped + ", error: " + error;
	}

	@Override
	public String toString() {

		String result = ">>> Summary: " + name;

		for (ProjectSummary project : projects) {
			result += "\n" + "\n" + project.toString(1);
		}

		Map<String, CountEntry> total = getTotal();
		if (!total.isEmpty()) {
			result += "\n" + "\n" + fromPadding(1) + ">>> Workspace Total:" + WorkerSummary.mapToString(getTotal(), 2);
		}

		result += "\n" + "\n" + fromPadding(1) + "Duration: " + getDurationAsString() + "\n" + fromPadding(1)
				+ "Number of projects: " + getProjectCountAsString();

		result += "\n" + "<<<";
		return result;
	}

	public Map<String, CountEntry> getTotal() {
		Map<String, CountEntry> result = Maps.newHashMap();
		for (ProjectSummary project : projects) {
			result = WorkerSummary.combine(project.getTotal(), result);
		}
		return result;
	}

	/**
	 * Specifies a status that a refactoring may have.
	 * 
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

		public WorkerSummary reportWorker(IWorker<?, ?> worker) {
			WorkerSummary result = new WorkerSummary(worker);
			workers.add(result);
			return result;
		}

		@Override
		public String toString() {
			return toString(0);
		}

		public Map<String, CountEntry> getTotal() {
			Map<String, CountEntry> result = Maps.newHashMap();
			for (WorkerSummary worker : workers) {
				result = WorkerSummary.combine(worker.entries, result);
			}
			return result;
		}

		public String toString(int padding) {
			String pad = fromPadding(padding);

			String result = pad + ">>> Project[" + status + "]: " + project.getName();

			for (WorkerSummary worker : workers) {
				result += "\n" + worker.toString(padding + 1);
			}

			Map<String, CountEntry> total = getTotal();
			if (!total.isEmpty()) {
				result += "\n" + "\n" + fromPadding(padding + 1) + ">>> Project Total:"
						+ WorkerSummary.mapToString(getTotal(), padding + 2);
			}

			return result;
		}
	}

	public enum WorkerStatus {
		UNDEFINED, ERROR, COMPLETED, SKIPPED
	}

	public static class WorkerSummary {

		private final String workerName;

		private WorkerStatus status;
		private Throwable throwable = null;
		private final Map<String, CountEntry> entries;

		WorkerSummary(IWorker<?, ?> worker) {
			this.workerName = worker == null ? "NO-WORKER" : worker.getName();

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

		public boolean hasKey(String key) {
			return entries.containsKey(key);
		}

		public synchronized void setStatus(WorkerStatus status) {
			Objects.requireNonNull(status);
			this.status = status;
		}
		
		public synchronized void setThrowable(Throwable throwable) {
			this.throwable = throwable;
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
			String result = pad + ">>> Worker[" + status + "]: " + workerName;

			result += mapToString(entries, padding + 1);
			
			if (throwable != null) {				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(out);
				throwable.printStackTrace(ps);
				result += "\n" + withPadding(out.toString(), padding + 1);
			}

			// result += pad + "<<<";
			return result;

		}

		private static String mapToString(Map<String, CountEntry> map, int padding) {			
			return map.entrySet().stream()
				.sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
				.reduce("", (acc, s) -> acc + "\n" + fromPadding(padding) + s.getKey() + ": " + s.getValue(), (s1, s2) -> s1 + "\n" + s2);
		}

		private static Map<String, CountEntry> combine(Map<String, CountEntry> a, Map<String, CountEntry> b) {
			if (a == null) {
				return b;
			} else if (b == null) {
				return a;
			} else {
				Map<String, CountEntry> result = Maps.newHashMap();
				result.putAll(a);

				for (Entry<String, CountEntry> entry : b.entrySet()) {
					CountEntry count = result.get(entry.getKey());
					if (count == null) {
						result.put(entry.getKey(), entry.getValue());
					} else {
						result.put(entry.getKey(), count.combineWith(entry.getValue()));
					}
				}

				return result;
			}
		}

		static class CountEntry {
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

			public CountEntry combineWith(CountEntry other) {
				CountEntry result = new CountEntry();
				result.setCorrect(correct + other.correct);
				result.setSkipped(skipped + other.skipped);

				return result;
			}
		}
	}

	private static String fromPadding(int padding) {
		return Strings.repeat("\t", padding);
	}
	
	private static String withPadding(String s, int padding) {
		String pad = fromPadding(padding);
		return pad + s.replace("\n", "\n" + pad);		
	}

}
