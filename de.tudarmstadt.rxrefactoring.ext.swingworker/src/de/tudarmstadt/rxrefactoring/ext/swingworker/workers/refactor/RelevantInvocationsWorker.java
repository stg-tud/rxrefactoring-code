package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactorInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor.RelevantInvocation;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;

public class RelevantInvocationsWorker implements IWorker<TypeOutput, Void> {

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary, RefactorScope scope) throws Exception {

		RefactorInfo info = input.info;

		for (Entry<IRewriteCompilationUnit, RelevantInvocation> entry : input.collector.getRelevantInvocations()
				.entries()) {
			MethodInvocation m = entry.getValue().getMethodInvocation();
			IRewriteCompilationUnit unit = entry.getKey();

			if (Methods.hasSignature(m.resolveMethodBinding(), "java.util.concurrent.Executor", "execute",
					"java.lang.Runnable")) {
				Expression arg = (Expression) m.arguments().get(0);
				ITypeBinding argType = arg.resolveTypeBinding();

				if (info.shouldBeRefactored(argType) && Types.isTypeOf(argType, "javax.swing.SwingWorker")) {
					Expression e = unit.getRewrittenNode(arg);
					unit.replace(m, observableExecute(unit, unit.cloneNode(e)));
					summary.addCorrect("executorExecute");
				} else {
					summary.addSkipped("executorExecute");
				}
			}
		}
		
		//TODO THA only here because its last worker
		summary.setCorrect("numberOfCompilationUnits", (int) units.stream()
				.filter(a -> a.getWriter() != null)
				.count());

		return null;
	}

	private MethodInvocation observableExecute(IRewriteCompilationUnit unit, Expression observable) {
		AST ast = unit.getAST();

		MethodInvocation m = ast.newMethodInvocation();
		m.setName(ast.newSimpleName("executeObservable"));
		m.setExpression(observable);

		return m;
	}

	@Override
	public @Nullable Void refactor(IProjectUnits units, TypeOutput input, WorkerSummary summary) throws Exception {
		// TODO Auto-generated method stub
		// only needed if RefactorScope is not implemented
		return null;
	}

}
