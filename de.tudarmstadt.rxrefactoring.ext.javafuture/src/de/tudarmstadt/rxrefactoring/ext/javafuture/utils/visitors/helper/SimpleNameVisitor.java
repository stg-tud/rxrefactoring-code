package de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.utils.Types;



/**
 * Searches for all simple names that resolve to type 'binaryName'
 *
 */
public class SimpleNameVisitor extends ASTVisitor {
	private String binaryName;
	private List<SimpleName> simpleNames;

	public SimpleNameVisitor(String binaryName) {
		this.binaryName = binaryName;
		simpleNames = new ArrayList<>();
	}

	@Override
	public boolean visit(SimpleName node) {
		if (Types.isExactTypeOf(node.resolveTypeBinding(), binaryName)) {
			simpleNames.add(node);
		}
		
		return true;
	}

	public List<SimpleName> getSimpleNames() {
		return simpleNames;
	}
}
