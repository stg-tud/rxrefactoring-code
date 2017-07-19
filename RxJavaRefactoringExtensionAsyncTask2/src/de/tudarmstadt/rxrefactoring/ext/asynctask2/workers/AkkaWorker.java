package de.tudarmstadt.rxrefactoring.ext.asynctask2.workers;



import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.collect.ASTCollector;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.ASTWorker;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;

public class AkkaWorker extends ASTWorker {

	private static String[] imports = new String[]{ "rx.Observable" }; 
	
	public AkkaWorker(ASTCollector collector) {
		super(collector);
	}
	
	@Override
	protected Visitor createVisitor(UnitWriter writer) {
		return new AkkaVisitor(writer);
	}
	
	@Override
	protected String[] imports() {
		return imports;
	}
	
	private class AkkaVisitor extends Visitor {		
			
		public AkkaVisitor(UnitWriter writer) {
			super(writer);			
		}		
				
		@Override
		//If we encounter a variable declaration		
		public boolean visit(VariableDeclarationStatement node) {
			//Check whether the variable type is a future
			if (ASTUtils.matchType("^scala\\.concurrent\\.Future(<.*>)?$", node.getType())) {
				Log.info(getClass(), "Refactor variable declaration...");
				//If so, then change the type to Observable and add the correct import
				ParameterizedType type = (ParameterizedType) node.getType();		
				writer.replace(type.getType(), ast.newSimpleName("Observable"));
				setChanged();
			}				
			return true;
		}
		
		@Override
		public boolean visit(VariableDeclarationFragment node) {
			//TODO: Store the nodes name for further refactorings.
			//node.accept(this);
			return true;
		}
		
		@Override
		//TODO: Only visit this when we refactored the variable.
		public void endVisit(MethodInvocation node) {				
			
			IMethodBinding mb = node.resolveMethodBinding();			
			if (mb != null) {
				if (ASTUtils.matchMethod(mb, "^akka\\.dispatch\\.Futures$", "future", "^scala\\.concurrent\\.Future(<.*>)?$", "^java\\.util\\.concurrent\\.Callable(<.*>)?$", "^scala\\.concurrent\\.ExecutionContext$")) {
					Log.info(getClass(), "Refactor " + node.getName());
					writer.replace(node.getExpression(), ast.newSimpleName("Observable"));
		            writer.replace(node.getName(), ast.newSimpleName("fromCallable"));			            
		            
		            ListRewrite lr = writer.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY);
		            lr.remove((ASTNode) node.arguments().get(1), null);    
		            setChanged();
				}
			} else {
				Log.info(getClass(), "No binding for: " + node.getName());
			}		
			
		}
	}

	

	
	
}
