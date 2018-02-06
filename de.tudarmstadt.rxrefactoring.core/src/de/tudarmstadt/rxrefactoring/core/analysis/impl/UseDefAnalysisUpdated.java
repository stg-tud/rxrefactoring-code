package de.tudarmstadt.rxrefactoring.core.analysis.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.traversal.IDataFlowTraversal;
import de.tudarmstadt.rxrefactoring.core.utils.Types;

/**
 * Returns a Map of 'defs' and 'uses' instead of only def-multimaps
 * New 'Result' return type Map<String, Multimap<String, ASTNode>>, where String will be either 'defs' or 'uses'
 * @author Kumar
 *
 */

public class UseDefAnalysisUpdated extends DataFlowAnalysis<ASTNode, Map<String, Multimap<String, ASTNode>>>{

	private static final String DEFS = "defs";
	private static final String USES = "uses";
	
	
	protected UseDefAnalysisUpdated() {
		super(newDataFlowStrategy(), traversalForwards());

	}


	public static UseDefAnalysisUpdated create() {
		return new UseDefAnalysisUpdated();
	}
	
	
	private static IDataFlowStrategy<ASTNode, Map<String, Multimap<String, ASTNode>>> newDataFlowStrategy() {
		return new IDataFlowStrategy<ASTNode, Map<String, Multimap<String, ASTNode>>>() {
			
			//Changed to LinkedHashMap and LinkedHashSet for tracking traversal of graph
			@Override
			public Map<String, Multimap<String, ASTNode>> entryResult() {
				Map<String, Multimap<String, ASTNode>> result = new HashMap();
				result.put(DEFS, Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet()));
				result.put(USES, Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet()));
				return result;
			}

			@Override
			public Map<String, Multimap<String, ASTNode>> initResult() {				
				Map<String, Multimap<String, ASTNode>> result = new HashMap();
				result.put(DEFS, Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet()));
				result.put(USES, Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet()));
				return result;
			}

			@Override
			public Map<String, Multimap<String, ASTNode>> mergeAll(Collection<Map<String, Multimap<String, ASTNode>>> results) {
				
				Map<String, Multimap<String, ASTNode>> result = new HashMap();
				result.put(DEFS, Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet()));
				result.put(USES, Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet()));
				
				for(Map<String, Multimap<String, ASTNode>> tempMap: results) {
					result.get(DEFS).putAll(tempMap.get(DEFS));
					result.get(USES).putAll(tempMap.get(USES));
				}
				
				return result;
			}

			@Override
			public Map<String, Multimap<String, ASTNode>> transform(ASTNode vertex, Map<String, Multimap<String, ASTNode>> input) {
				Map<String, Multimap<String, ASTNode>> result = new HashMap();
				result.put(DEFS, Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet()));
				result.put(USES, Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet()));
								
				//Extract variable definitions and uses
				Multimap<String, ASTNode> tempResult = Multimaps.newSetMultimap(Maps.newLinkedHashMap(), () -> Sets.newLinkedHashSet());
				if (vertex instanceof Assignment) {	
					/*if(FutureTypeWrapper.isAkkaFuture(((Assignment) vertex).getLeftHandSide().resolveTypeBinding())) {
						tempResult.put(((Assignment) vertex).getLeftHandSide(), ((Assignment) vertex).getRightHandSide());						
					}*/						
					Expression leftExpr = ((Assignment) vertex).getLeftHandSide();
					Expression rightExpr = ((Assignment) vertex).getRightHandSide();
										
					if(leftExpr.resolveTypeBinding().getName().contains("Future") && leftExpr instanceof SimpleName) {
						String leftVarName = ((SimpleName)leftExpr).getIdentifier();
						if(rightExpr instanceof SimpleName) {
							String rightVarName = ((SimpleName)rightExpr).getIdentifier();
							
							//If definitions already present take the latest definition
							if(input.get(DEFS).asMap().containsKey(rightVarName)) {
								ArrayList<Collection<ASTNode>> al = new ArrayList(input.get(DEFS).get(rightVarName));
								ASTNode defValNode = (ASTNode) al.get(al.size() -1);
								tempResult.put(leftVarName, defValNode);							

							}
							else {
								tempResult.put(leftVarName, ((Assignment) vertex).getRightHandSide());
							}
						}
						else {
							tempResult.put(leftVarName, ((Assignment) vertex).getRightHandSide());
						}
						
					}
					result.putAll(input);
					result.get(DEFS).putAll(tempResult);
				}
				else if(vertex instanceof VariableDeclarationStatement) {
					VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment)((VariableDeclarationStatement) vertex).fragments().get(0);
					
					//Check future definitions only
					String  varType = varDeclFrag.resolveBinding().getType().getName();	
					if(varType.contains("Future")) {
						if(varDeclFrag.getInitializer() != null) {
							tempResult.put(varDeclFrag.getName().toString(), varDeclFrag.getInitializer());								
						}
						else {
							tempResult.put(varDeclFrag.getName().toString(), null);
						}
					}															
					result.putAll(input);
					result.get(DEFS).putAll(tempResult);
				}
				else if(vertex instanceof MethodInvocation) {
					if(((MethodInvocation)vertex).getExpression() != null) {
						String invokerObject = ((MethodInvocation)vertex).getExpression().toString();
						if(input.get(DEFS).asMap().containsKey(invokerObject)) {
							ArrayList<Collection<ASTNode>> al = new ArrayList(input.get(DEFS).get(invokerObject));
							String useVal = ((ASTNode)(al.get(al.size() -1))).toString();
							tempResult.put(useVal, ((MethodInvocation)vertex).getName());
						}
						else {
							tempResult.put(invokerObject, ((MethodInvocation)vertex).getName());
						}
					}
					result.putAll(input);
					result.get(USES).putAll(tempResult);
				}
				else {
					result.putAll(input);
				}
				
				return result;
			}

			
			
		};
	}

	


}