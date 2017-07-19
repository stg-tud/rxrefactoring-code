package de.tudarmstadt.rxrefactoring.core.collect;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

public class ASTCollector extends AbstractCollector {

		
	private final ASTParser parser;
	private boolean resolveBindings;
	private Map<ICompilationUnit, ASTNode> rootNodes;
	
	public ASTCollector(IJavaProject project, String name, boolean resolveBindings) {	
		super(project, name);
		this.resolveBindings = resolveBindings;
		parser  = ASTParser.newParser(AST.JLS8);		
		rootNodes = new HashMap<>();		
	}

	private ASTNode createAST() {
		//parser.setKind(ASTParser.K_STATEMENTS);
		parser.setResolveBindings(resolveBindings);
		return parser.createAST(null);
	}
	
	@Override
	public void processCompilationUnit(ICompilationUnit unit) {
		parser.setSource(unit);
		rootNodes.put(unit, createAST());
	}
	
	public Set<ICompilationUnit> getUnits() {
		return rootNodes.keySet();
	}
	
	public ASTNode getRootNode(ICompilationUnit unit) {
		return rootNodes.get(unit);
	}
	
	public Map<ICompilationUnit, ASTNode> getRootNodes() {
		return rootNodes;
	}
}
