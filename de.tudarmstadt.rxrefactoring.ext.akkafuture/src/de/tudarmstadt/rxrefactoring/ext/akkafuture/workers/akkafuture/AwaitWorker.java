package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaFutureWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.AwaitBinding;

public class AwaitWorker extends AbstractAkkaFutureWorker<AkkaFutureCollector, AwaitBinding> {
	public AwaitWorker() {
		super("Await.result");
	}

	@Override
	protected Multimap<RewriteCompilationUnit, AwaitBinding> getNodesMap() {
		return collector.awaits;
	}

	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addSubjectImport(unit);
		addCallableImport(unit);
		addSchedulersImport(unit);
		
		super.endRefactorNode(unit);
	}
	
	@SuppressWarnings("unused")
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, AwaitBinding await) {
		
		AST ast = unit.getAST();
		
		//future.toBlocking()
		MethodInvocation toBlocking = ast.newMethodInvocation();
		toBlocking.setName(ast.newSimpleName("toBlocking"));		
		
		Expression expr = await.getFuture();
//		ChildListPropertyDescriptor clpd = (ChildListPropertyDescriptor) expr.getLocationInParent();
//		ListRewrite l = unit.writer().getListRewrite(expr.getParent(), clpd);
//		List list = l.getRewrittenList();		
		toBlocking.setExpression(unit.copyNode((Expression) unit.getRewrittenNode(expr, 0)));
		
		
		//.single()
		MethodInvocation single = ast.newMethodInvocation();
		single.setName(ast.newSimpleName("single"));
		single.setExpression(toBlocking);
		
		unit.replace(await.getMethodInvocation(), single);
		
	}
	

}

