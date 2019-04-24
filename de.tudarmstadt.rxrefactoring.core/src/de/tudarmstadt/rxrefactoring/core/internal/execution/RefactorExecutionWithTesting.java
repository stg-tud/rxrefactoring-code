package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.text.edits.MalformedTreeException;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.ProjectSummary;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.RandoopGenerator;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect.Pair;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

public class RefactorExecutionWithTesting extends RefactorExecution {

    /**
     * IPL: Mapping of projects to found methods that contains all impacted and
     * changed methods for each project.
     */
	private final Map<IProject, Pair<Set<String>, Set<String>>> foundMethods = new HashMap<>();
	
	private final Map<IProject, RandoopGenerator> generators = new HashMap<>();
	
	
	public RefactorExecutionWithTesting(IRefactorExtension env) {
		super(env);		
	}
	
	protected void doRefactorProject(@NonNull ProjectUnits units, @NonNull CompositeChange changes, @NonNull ProjectSummary projectSummary, IProject project)
			throws IllegalArgumentException, MalformedTreeException, BadLocationException, CoreException, InterruptedException {
		super.doRefactorProject(units, changes, projectSummary, project);
		
		Log.info(RefactorExecutionWithTesting.class, "Scan methods for testing...");
	
		// IPL: Find the changing and calling methods
		foundMethods.put(project, MethodScanner.findMethods(units));
		// IPL: Copy the pre-refactoring binaries over
		RandoopGenerator rgen = new RandoopGenerator(project);
		generators.put(project, rgen);	
		rgen.copyBinariesToPre();
	}
	
	protected void postRefactor() {
		
		// IPL: OK has been pressed, time to continue post-refactoring
	    IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = workspace.getProjects();
        for(IProject project : projects) {
        	
        	Objects.requireNonNull(project);     
        	            	
            if(considerProject(project)) {
            	    
            	Log.info(getClass(), "Create Randoop specification for " + project.getName() + "...");
            	
            	Objects.requireNonNull(foundMethods);
            	Objects.requireNonNull(foundMethods.get(project));
            	
                Set<String> impacted = foundMethods.get(project).getFirst();
                Set<String> calling = foundMethods.get(project).getSecond();
                
                RandoopGenerator rgen = generators.get(project);

                // IPL: Copy over post-refactoring binaries
                rgen.copyBinariesToPost();

                // IPL: Parse the refactored compilation units to obtain the ASTs
                ProjectUnits units;
                try {
                    units = parseCompilationUnits(JavaCore.create(project));
                }
                catch(JavaModelException e) {
                    throw new RuntimeException(e);
                }

                // IPL: Throw out any impacted methods that changed signatures,
                // because those can't be tested
                try {
                    impacted = MethodScanner.retainUnchangedMethods(impacted, units);
                }
                catch(Throwable e) {
                    Log.error(RefactorExecutionWithTesting.class, "Failed to determine unchanged methods.", e);
                }

                // IPL: The methods to test are the union of the unchanged
                // impacted methods and the methods that call any impacted
                // methods.
                Set<String> methodsToTest = new HashSet<>(impacted);
                methodsToTest.addAll(calling);
                // IPL: Throw out any inaccessible (i.e. non-public)
                // methods, since those can't be tested
                MethodScanner.removeInaccessibleMethods(methodsToTest, units);
                Log.info(RefactorExecutionWithTesting.class, "Found total of " + methodsToTest.size() + " method(s) suitable for testing.");
                Log.info(RefactorExecutionWithTesting.class, methodsToTest);
                // IPL: For debugging only
                //Log.info(RefactorExecution.class, "Methods to test: " + methodsToTest);

                // IPL: Compute the set of classes that will be tested
                Set<String> classesToTest = MethodScanner.extractClassNames(methodsToTest);
                // IPL: For debugging only
                //Log.info(RefactorExecution.class, "Classes to test: " + classesToTest);

                // IPL: Compute the set of methods that should NOT be tested
                Set<String> methodsToOmit = MethodScanner.findAllMethods(units, classesToTest);
                methodsToOmit.removeAll(methodsToTest);
                methodsToOmit = RandoopGenerator.convertToRegexFormat(methodsToOmit);
                // IPL: Disgusting hack, because Randoop LOVES calling
                // getClass() and comparing the result to null
                methodsToOmit.add("java\\.lang\\.Object\\.getClass\\(");

                // IPL: Finally, create the output
                rgen.createOutput(classesToTest, methodsToOmit);
            }
        }
		
	}

}
