package de.tudarmstadt.rxrefactoring.ext.future.workers;

import static de.tudarmstadt.rxrefactoring.core.NodeSupplier.simpleName;
import static de.tudarmstadt.rxrefactoring.core.NodeSupplier.simpleType;

import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorkerV1;
import de.tudarmstadt.rxrefactoring.core.NodeSupplier;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.ir.ComplexReactiveComputation;
import de.tudarmstadt.rxrefactoring.core.ir.EmptyReactiveInput;
import de.tudarmstadt.rxrefactoring.core.ir.IReactiveInput;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveObject;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveObject.InstanceCreationBuilder;
import de.tudarmstadt.rxrefactoring.core.ir.ReactiveOutput;
import de.tudarmstadt.rxrefactoring.core.ir.util.SchedulerBuilder;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;
import de.tudarmstadt.rxrefactoring.ext.future.Utils;


public class FutureSubmitTransformer implements IWorkerV1<Multimap<IRewriteCompilationUnit, MethodInvocation>, Void> {

	@SuppressWarnings("null")
	@Override
	public Void refactor(@NonNull IProjectUnits units, @Nullable Multimap<IRewriteCompilationUnit, MethodInvocation> input,
			@NonNull WorkerSummary summary) throws Exception {
		
		for (IRewriteCompilationUnit unit : units) {
			
			for (MethodInvocation invocation : input.get(unit)) {
				
				AST ast = unit.getAST();
				
				ReactiveObject reactive = new ReactiveObject(simpleName("ReactiveObject"));
				
				IReactiveInput reactiveInput =  new EmptyReactiveInput(simpleName("input"));
				reactive.addInput("input", reactiveInput);
				
				ReactiveOutput reactiveOutput = new ReactiveOutput(simpleType("String"), simpleName("output"), null, null);
				reactive.addOutput("output", reactiveOutput);
				
				Expression argument = (Expression) invocation.arguments().get(0);								
				
				
				NodeSupplier<Expression> consumerDefinition = Utils.callableToConsumer(argument, simpleName("var"), reactiveInput.supplyType());
				@SuppressWarnings("null")
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
						@SuppressWarnings("null")
						List<ReturnStatement> returns = Utils.findReturnStatements(body[0]);
						
						for (ReturnStatement ret : returns) {
							u.replace(ret, u.getAST().newExpressionStatement(reactiveOutput.supplyOnNext(_u -> _u.cloneNode(ret.getExpression())).apply(u)));
						}
					}					
					
					return expr;
					
				});
				
				
				@SuppressWarnings("null")
				ComplexReactiveComputation computation = new ComplexReactiveComputation(
						reactiveInput,
						simpleName("internal"),
						consumerDefinition2, 
						SchedulerBuilder.schedulersComputation());
				reactive.addComputation("internal", computation);
				
				
				TypeDeclarationStatement stmt = ast.newTypeDeclarationStatement(reactive.supplyTypeDeclaration().apply(unit));				
				Statements.addStatementBefore(unit, stmt, Statements.enclosingStatement(invocation));
				
				InstanceCreationBuilder instanceCreationBuilder = reactive.supplyInstanceCreation();
				instanceCreationBuilder.accessOutput("output");
				
				unit.replace(invocation, instanceCreationBuilder.apply(unit));
				
				
				
//				unit.getRoot().accept(new ASTVisitor() {
//					public boolean visit(TypeDeclaration node) {
//						ListRewrite l = unit.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
//						l.insertFirst(reactive.supplyTypeDeclaration().apply(unit), null);
//						return false;
//					}
//				});
				
				
				
			}
		}
		
		return null;
	}

}
