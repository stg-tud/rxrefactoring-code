package de.tudarmstadt.rxrefactoring.ext.swingworker.visitors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

//import rx.Subscriber;

import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;

import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;

import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;



/**
 * Description: Visitor to extract relevant information about SwingWorkers<br>
 * <ol>
 * <li>doInBackground Block</li>
 * <li>done Block</li>
 * <li>process Block</li>
 * <li>returned type</li>
 * <li>presence of get method and its arguments</li>
 * <li>progress update type</li>
 * </ol>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 * Adapted to new structure: 13/01/2018 by Camila Gonzalez
 */
public class RefactoringVisitor extends ASTVisitor
{
	private static final String DO_IN_BACKGROUND = "doInBackground";
	private static final String DONE = "done";
	private static final String PROCESS = "process";
	//private Class subscriberClass = Subscriber.class;
	//private final Map<String, Method> methodsOfSubscriber = new HashMap<String, Method>();
	private Block doInBackgroundBlock;
	private Block doneBlock;
	private Block processBlock;
	private Type resultType;
	private Type processType;
	private String processVariableName;
	private List<SuperMethodInvocation> superMethodInvocationsToRemove;
	private MethodDeclaration constructor;

	// for "stateful" classes
	private List<FieldDeclaration> fieldDeclarations;
	private List<MethodDeclaration> additionalMethodDeclarations;
	private List<MethodDeclaration> allMethodDeclarations;
	private List<MethodInvocation> allMethodInvocations;
	private List<TypeDeclaration> typeDeclarations;
	private List<String> suscriberMethodNames;
	
	private final Map<String, Method> methodsOfSubscriber = new HashMap<String, Method>();

	public RefactoringVisitor()
	{
		fieldDeclarations = new ArrayList<>();
		additionalMethodDeclarations = new ArrayList<>();
		allMethodDeclarations = new ArrayList<>();
		allMethodInvocations = new ArrayList<>();
		superMethodInvocationsToRemove = new ArrayList<>();
		typeDeclarations = new ArrayList<>();
		suscriberMethodNames = Arrays.asList("add", "isUnsubscribed", "onStart", "request", "setProducer", "unsubscribe");
		//for (Method method : subscriberClass.getDeclaredMethods()) {
			//methodsOfSubscriber.put(method.getName(), method);
			////return result.toArray(new Method[result.size()]);
		//}
	}

	@Override
	public boolean visit( ClassInstanceCreation node )
	{
		Type type = node.getType();
		boolean target = ASTUtils.isClassOf(node, SwingWorkerInfo.getBinaryName());
		if ( target && type instanceof ParameterizedType )
		{
			List argumentTypes = ( (ParameterizedType) type ).typeArguments();
			resultType = (Type) argumentTypes.get( 0 );
			processType = (Type) argumentTypes.get( 1 );
		}
		return true;
	}

	@Override
	public boolean visit( Block node )
	{
		ASTNode parent = node.getParent();
		if ( parent instanceof MethodDeclaration )
		{
			MethodDeclaration methodDeclaration = (MethodDeclaration) parent;
			String methodDeclarationName = methodDeclaration.getName().toString();
			if ( DO_IN_BACKGROUND.equals( methodDeclarationName ) )
			{
				doInBackgroundBlock = node;
			}
			else if ( DONE.equals( methodDeclarationName ) )
			{
				doneBlock = node;
			}
			else if ( PROCESS.equals( methodDeclarationName ) )
			{
				processBlock = node;
				processVariableName = ASTUtils.getVariableName( methodDeclaration, 0 );
			}
		}
		return true;
	}

	@Override
	public boolean visit( TypeDeclaration node )
	{
		typeDeclarations.add( node );
		return true;
	}

	@Override
	public boolean visit( FieldDeclaration node )
	{
		TypeDeclaration parent = ASTNodes.findParent(node, TypeDeclaration.class).get();
		if ( isRelevant( parent ) )
		{
			fieldDeclarations.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( MethodDeclaration node )
	{
		if ( constructor == null && node.isConstructor() )
		{
			constructor = node;
		}
		else
		{
			allMethodDeclarations.add( node );
		}

		TypeDeclaration parent = ASTNodes.findParent(node, TypeDeclaration.class).get();
		if ( isRelevant( parent ) )
		{
			String methodDeclarationName = node.getName().toString();
			if ( !DO_IN_BACKGROUND.equals( methodDeclarationName ) &&
					!DONE.equals( methodDeclarationName ) &&
					!PROCESS.equals( methodDeclarationName ) )
			{
				additionalMethodDeclarations.add( node );
			}
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		
		allMethodInvocations.add( node );
		return true;
	}
	
	@Override
	public boolean visit( SuperMethodInvocation node )
	{
		if ( ASTUtils.matchesTargetMethod( node, DONE, SwingWorkerInfo.getBinaryName() ) ||
				ASTUtils.matchesTargetMethod( node, PROCESS, SwingWorkerInfo.getBinaryName() ) )
		{
			superMethodInvocationsToRemove.add( node );
		}
		return true;
	}

	public Block getDoInBackgroundBlock()
	{
		return doInBackgroundBlock;
	}

	public Block getDoneBlock()
	{
		return doneBlock;
	}

	public Type getResultType()
	{
		return resultType;
	}

	public Type getProcessType()
	{
		return processType;
	}

	public Block getProcessBlock()
	{
		return processBlock;
	}

	public String getProcessVariableName()
	{
		return processVariableName;
	}

	public List<FieldDeclaration> getFieldDeclarations()
	{
		return fieldDeclarations;
	}

	public List<MethodDeclaration> getAdditionalMethodDeclarations()
	{
		return additionalMethodDeclarations;
	}

	public List<SuperMethodInvocation> getSuperMethodInvocationsToRemove()
	{
		return superMethodInvocationsToRemove;
	}

	public List<TypeDeclaration> getTypeDeclarations()
	{
		return typeDeclarations;
	}

	public List<String> getMethodsofsubscriber() {
		return suscriberMethodNames;
	}

	public MethodDeclaration getConstructor()
	{
		return constructor;
	}

	public List<MethodDeclaration> getAllMethodDeclarations()
	{
		return allMethodDeclarations;
	}
	
	public List<MethodInvocation> getAllMethodInvocations()
	{
		return allMethodInvocations;
	}
	
	public boolean hasAdditionalFieldsOrMethods()
	{
		return !fieldDeclarations.isEmpty() ||
				!additionalMethodDeclarations.isEmpty() ||
				!typeDeclarations.isEmpty();
	}

	// ### Private Methods ###

	private boolean isRelevant( TypeDeclaration parent )
	{
		boolean ignore = false;
		for ( TypeDeclaration typeDeclaration : typeDeclarations )
		{
			if ( typeDeclaration.equals( parent ) )
			{
				ignore = true;
				break;
			}
		}
		return !ignore;
	}
}
