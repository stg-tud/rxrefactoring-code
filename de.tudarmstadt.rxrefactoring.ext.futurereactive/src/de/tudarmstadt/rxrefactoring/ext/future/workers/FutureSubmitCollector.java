package de.tudarmstadt.rxrefactoring.ext.future.workers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.core.utils.Types;


public class FutureSubmitCollector implements IWorker<Void, Multimap<IRewriteCompilationUnit, MethodInvocation>> {

	final Multimap<IRewriteCompilationUnit, MethodInvocation> submitInvocations = HashMultimap.create();
	
	@Override
	public Multimap<IRewriteCompilationUnit, MethodInvocation> refactor(@NonNull IProjectUnits units, @Nullable Void input, @NonNull WorkerSummary summary) throws Exception {		
		
		/*
		 * # SEARCH TEST # 
		 */
		
		Log.info(getClass(), "### START SEARCH ###");
		
		SearchPattern pattern = SearchPattern.createPattern("f", IJavaSearchConstants.FIELD, IJavaSearchConstants.ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH);
						
		
		
		
		SearchRequestor requestor = new SearchRequestor() {
			
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				System.out.println(match.getClass().getName() + " -- " + match.getElement());
				
			}
		};
		
		// step4: start searching
		SearchEngine searchEngine = new SearchEngine();
		try {
			
			searchEngine.search(pattern,
					new SearchParticipant[] { SearchEngine
							.getDefaultSearchParticipant() },
					units.getSearchScope(),
					requestor, 
					null);
		} catch (CoreException e) {
			System.out.println("exception");
			e.printStackTrace();
		}
		
		Log.info(getClass(), "### FINISH SEARCH ###");
		
		/*
		 * # END SEARCH TEST #
		 */
		
		
		units.accept(new FutureVisitor());			
		return submitInvocations;
	}
	
	
	class FutureVisitor extends UnitASTVisitor {
		
		@Override
		public boolean visit(MethodInvocation node) {
			if (node.getExpression() == null) {
				return true;
			}
			
			ITypeBinding expressionType = node.getExpression().resolveTypeBinding();	
			IMethodBinding methodBinding = node.resolveMethodBinding();
			
			if (Types.isExactTypeOf(expressionType, "java.util.concurrent.ExecutorService") 
					&& Methods.hasSignature(methodBinding, null, "submit", "java.util.concurrent.Callable")) {
				submitInvocations.put(getUnit(), node);
				return true;
			}
			
			return true;
		}
	}


	@Override
	public @Nullable Multimap<IRewriteCompilationUnit, MethodInvocation> refactor(IProjectUnits units, Void input,
			WorkerSummary summary, RefactorScope scope) throws Exception {
		// TODO Auto-generated method stub
		// only if RefactorScope is implemented in extension needed
		return null;
	}
	
	

	

}

