package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import java.util.Objects;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;

/**
 * Wrapper for Futures.sequence(...) invocations
 * @author mirko
 *
 */
public class FuturesSequenceWrapper implements FutureMethodWrapper {
	
	private final MethodInvocation futureInvocation;
	
	private FuturesSequenceWrapper(MethodInvocation futureInvocation) {
		this.futureInvocation = futureInvocation;
	}
	
	public static FuturesSequenceWrapper create(Expression futureInvocation) {
		
		if (!isFuturesSequence(futureInvocation))
			throw new IllegalArgumentException("You need to specify an invocation to Futures.sequence as parameter, but was: " + futureInvocation);
		
		return new FuturesSequenceWrapper((MethodInvocation) futureInvocation);
	}
	
	public Expression getFuture() {
		return (Expression) futureInvocation.arguments().get(0);
	}
	
	public FutureTypeWrapper getFutureType() {
		return FutureTypeWrapper.create(
				getFuture().resolveTypeBinding().getTypeArguments()[0]);
	}
	
	public MethodInvocation getExpression() {
		return futureInvocation;
	}
	
	public MethodInvocation createZipExpression(IRewriteCompilationUnit unit) {
		/*
		 * Builds:		
		 * Observable.zip(futures, objects -> Arrays.stream(objects).map(o -> (T) o).collect(Collectors.toList()));
		 * 
		 * from: 
		 * Future<Iterable<T>> ... = Futures.sequence(futures, ec);
		 * 
		 */
		
		
		AST ast = unit.getAST();
		
		String parameterName = "objects";
		LambdaExpression zipLambda = ast.newLambdaExpression();
		
		VariableDeclarationFragment zipVar = ast.newVariableDeclarationFragment();
		zipVar.setName(ast.newSimpleName(parameterName));
		
		zipLambda.parameters().add(zipVar);
		
		
		//Arrays.stream(objects)
		MethodInvocation arraysStream = ast.newMethodInvocation();
		arraysStream.setName(ast.newSimpleName("stream"));
		arraysStream.setExpression(ast.newSimpleName("Arrays"));
		arraysStream.arguments().add(ast.newSimpleName(parameterName));
		
		//.map
		MethodInvocation map = ast.newMethodInvocation();
		map.setName(ast.newSimpleName("map"));
		map.setExpression(arraysStream);
		
		//o -> (T) o
		VariableDeclarationFragment mapVar = ast.newVariableDeclarationFragment();
		mapVar.setName(ast.newSimpleName("o"));
	
		CastExpression cast = ast.newCastExpression();
		
		FutureTypeWrapper ft = getFutureType();
		
		cast.setType(Types.typeFromBinding(ast, getFutureType().getTypeParameter(ast)));
		cast.setExpression(ast.newSimpleName("o"));
		
		LambdaExpression mapLambda = ast.newLambdaExpression();
		mapLambda.parameters().add(mapVar);
		mapLambda.setBody(cast);
		
		map.arguments().add(mapLambda);
		
		//.collect(Collectors.toList())
		MethodInvocation collect = ast.newMethodInvocation();
		collect.setName(ast.newSimpleName("collect"));
		collect.setExpression(map);
		
		MethodInvocation collectorsToList = ast.newMethodInvocation();
		collectorsToList.setName(ast.newSimpleName("toList"));
		collectorsToList.setExpression(ast.newSimpleName("Collectors"));
		
		collect.arguments().add(collectorsToList);
				
		zipLambda.setBody(collect);
		
		
		//Observable.zip
		MethodInvocation observableZip = ast.newMethodInvocation();
		observableZip.setName(ast.newSimpleName("zip"));
		observableZip.setExpression(ast.newSimpleName("Observable"));
		observableZip.arguments().add(unit.copyNode(getFuture()));
		observableZip.arguments().add(zipLambda);
		
		return observableZip;
	}
	
	public static boolean isFuturesSequence(Expression expr) {
		if (expr == null || !(expr instanceof MethodInvocation))
			return false;
		
		MethodInvocation method = (MethodInvocation) expr;
		
		return Objects.equals(method.getName().getIdentifier(), "sequence") && 
				method.getExpression() != null &&
				AkkaFutureASTUtils.isFutures(method.getExpression().resolveTypeBinding());
	}

}
