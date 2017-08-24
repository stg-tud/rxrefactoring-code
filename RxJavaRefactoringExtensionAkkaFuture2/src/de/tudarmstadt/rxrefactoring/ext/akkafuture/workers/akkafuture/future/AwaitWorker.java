package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.AwaitBinding;

public class AwaitWorker extends AbstractAkkaWorker<AkkaFutureCollector, AwaitBinding> {
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
		ChildListPropertyDescriptor clpd = (ChildListPropertyDescriptor) expr.getLocationInParent();
		ListRewrite l = unit.writer().getListRewrite(expr.getParent(), clpd);
		List list = l.getRewrittenList();
		toBlocking.setExpression((Expression) list.get(0));
		
		
		//.single()
		MethodInvocation single = ast.newMethodInvocation();
		single.setName(ast.newSimpleName("single"));
		single.setExpression(toBlocking);
		
		unit.replace(await.getMethodInvocation(), single);
		
	}
	

}

