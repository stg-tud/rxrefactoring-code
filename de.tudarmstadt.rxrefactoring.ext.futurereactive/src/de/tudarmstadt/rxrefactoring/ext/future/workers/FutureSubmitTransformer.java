package de.tudarmstadt.rxrefactoring.ext.future.workers;

import static de.tudarmstadt.rxrefactoring.core.ir.NodeSupplier.simpleName;
import static de.tudarmstadt.rxrefactoring.core.ir.NodeSupplier.simpleType;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.ir.EmptyReactiveInput;
import de.tudarmstadt.rxrefactoring.core.ir.IReactiveInput;
import de.tudarmstadt.rxrefactoring.core.ir.NodeSupplier;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveComputation;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveObject;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveOutput;
import de.tudarmstadt.rxrefactoring.core.ir.util.SchedulerBuilder;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.ext.future.Utils;


public class FutureSubmitTransformer implements IWorker<Multimap<RewriteCompilationUnit, MethodInvocation>, Void> {

	@Override
	public Void refactor(ProjectUnits units, Multimap<RewriteCompilationUnit, MethodInvocation> input,
			WorkerSummary summary) throws Exception {
		
		for (RewriteCompilationUnit unit : units) {
			
			for (MethodInvocation invocation : input.get(unit)) {
				
				ReactiveObject reactive = new ReactiveObject(simpleName("ReactiveObject"));
				
				IReactiveInput reactiveInput =  new EmptyReactiveInput(simpleName("input"));
				reactive.addInput("input", reactiveInput);
				
				ReactiveOutput reactiveOutput = new ReactiveOutput(simpleType("String"), simpleName("output"), null, null);
				reactive.addOutput("output", reactiveOutput);
				
				Expression argument = (Expression) invocation.arguments().get(0);								
				
				
				NodeSupplier<Expression> consumerDefinition = Utils.callableToConsumer(argument, NodeSupplier.simpleName("var"), reactiveInput.supplyType());
				NodeSupplier<Expression> consumerDefinition2 = consumerDefinition.<Expression>map((u, expr) -> {
					
					final Block[] body = new Block[1];
					class ExprVisitor extends ASTVisitor {
						
						
						@Override
						public boolean visit(MethodDeclaration node) {
							if (Objects.equals(node.getName().getIdentifier(), "accept")) {
								body[0] = node.getBody();
								return false;
							}
							
							return false;
						}
					}
					expr.accept(new ExprVisitor());
					
					if (body[0] != null) {
						List<ReturnStatement> returns = Utils.findReturnStatements(body[0]);
						
						for (ReturnStatement ret : returns) {
							u.replace(ret, u.getAST().newExpressionStatement(reactiveOutput.supplyOnNext(_u -> _u.cloneNode(ret.getExpression())).apply(u)));
						}
					}					
					
					return expr;
					
				});
				
				
				ReactiveComputation computation = new ReactiveComputation(
						reactiveInput,
						simpleName("internal"),
						consumerDefinition2, 
						SchedulerBuilder.schedulersComputation());
				reactive.addComputation("internal", computation);
				
				
				
				unit.getRoot().accept(new ASTVisitor() {
					public boolean visit(TypeDeclaration node) {
						ListRewrite l = unit.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
						l.insertFirst(reactive.supplyTypeDeclaration().apply(unit), null);
						return false;
					}
				});
				
				
				
			}
		}
		
		return null;
	}

}
