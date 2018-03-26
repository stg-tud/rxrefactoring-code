package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.RxCollector;

public class RelevantInvocationsWorker implements IWorker<RxCollector, Void> {

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception { 
		
		for (Entry<IRewriteCompilationUnit, MethodInvocation> entry : input.getRelevantInvocations().entries()) {
			MethodInvocation m = entry.getValue();
			IRewriteCompilationUnit unit = entry.getKey();
						
			if (Methods.hasSignature(m.resolveMethodBinding(), "java.util.concurrent.Executor", "execute", "java.lang.Runnable")) {
				Expression arg = (Expression) m.arguments().get(0);
				if (Types.isTypeOf(arg.resolveTypeBinding(), "javax.swing.SwingWorker")) {
					Expression e = unit.getRewrittenNode(arg);					
					unit.replace(m, observableExecute(unit, unit.cloneNode(e)));
				}
			}
		}
		
		return null;
	}
	
	private MethodInvocation observableExecute(IRewriteCompilationUnit unit, Expression observable) {
		AST ast = unit.getAST();
		
		MethodInvocation m = ast.newMethodInvocation();
		m.setName(ast.newSimpleName("executeObservable"));
		m.setExpression(observable);
		
		return m;
	}

}
