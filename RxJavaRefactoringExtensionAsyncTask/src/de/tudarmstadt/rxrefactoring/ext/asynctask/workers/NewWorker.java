package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractWorker;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriters;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders2.InnerClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.collect.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

public class NewWorker extends AbstractWorker<AsyncTaskCollector> {

	public NewWorker(AsyncTaskCollector collector) {
		super(collector);
	}

	@Override
	public WorkerStatus refactor() {
		
		//Step 1: Identify where AsyncTasks are declared.		
		Set<AsyncTaskWrapper> declarations = Sets.newHashSet();
		for (Entry<ICompilationUnit, ASTNode> entry : collector.getRootNodes().entrySet()) {
			ICompilationUnit unit = entry.getKey();
			ASTNode node = entry.getValue();
			declarations.addAll(findDeclarations(node, unit));			
		}
		
		//Step 2: find where the AsyncTask is created
		
		for (AsyncTaskWrapper asyncTask : declarations) {
			//Set of creations for one AsyncTask
			Set<ClassInstanceCreation> creations = Sets.newHashSet();
			
			for (Entry<ICompilationUnit, ASTNode> entry : collector.getRootNodes().entrySet()) {
				ICompilationUnit unit = entry.getKey();
				ASTNode node = entry.getValue();								
				creations.addAll(findClassCreations(node, asyncTask));			
			}
			
			//Step 3: Refactor the classes that declare the AsyncTasks
			{
				ICompilationUnit unit = asyncTask.getUnit();	
				
				UnitWriter writer = UnitWriters.getOrPut(unit, () -> new UnitWriter(unit, collector.getAST(unit)));
				
				if (asyncTask.isInnerClass()) {
					InnerClassBuilder builder = new InnerClassBuilder(asyncTask, writer);
					TypeDeclaration newNode = builder.create();
					writer.replace(asyncTask.getDeclaration(), newNode);
				} else if (asyncTask.isAnonymousClass()) {
					//TODO: Complete for anonymous classes
				} else {
					Log.error(getClass(), "asyncTask is not a known declaration");
					return WorkerStatus.ERROR;
				}
				
				execution.addUnitWriter(writer);
				
				
			}
			
			//Step 4: Refactor references to the AsyncTask
			
			
			
		}		
		
		return WorkerStatus.OK;
	}
	
	
	
	private Set<AsyncTaskWrapper> findDeclarations(ASTNode root, ICompilationUnit unit) {
			
		Set<AsyncTaskWrapper> declarations = Sets.newHashSet();
		
		class DeclarationVisitor extends ASTVisitor {
			public boolean visit(TypeDeclaration node) {
				if (ASTUtils.isTypeOf(node, "android.os.AsyncTask")) {
					declarations.add(new AsyncTaskWrapper(node, unit));
				}
				return true;
			}
			
			public boolean visit(AnonymousClassDeclaration node) {
				if (ASTUtils.isTypeOf(node, "android.os.AsyncTask")) {
					declarations.add(new AsyncTaskWrapper(node, unit));
				}
				return true;
			}
		}
		
		root.accept(new DeclarationVisitor());
		
		return declarations;	
	}
	
	private Set<ClassInstanceCreation> findClassCreations(ASTNode root, AsyncTaskWrapper asyncTask) {
		
		Set<ClassInstanceCreation> creations = Sets.newHashSet();
		
		class ClassCreationVisitor extends ASTVisitor {
			public boolean visit(ClassInstanceCreation node) {
				
				ITypeBinding nodeBinding = node.resolveTypeBinding();
				ITypeBinding asyncTaskBinding = asyncTask.resolveTypeBinding();
								
				if (typeBindingEquals(nodeBinding, asyncTaskBinding)) {
					creations.add(node);					
				}
				
				return true;
			}
		}
		
		root.accept(new ClassCreationVisitor());
		
		return creations;
	}
	
	private static boolean typeBindingEquals(ITypeBinding a, ITypeBinding b) {
		return a != null && a.isEqualTo(b);
	}
	
	

}
