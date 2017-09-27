package de.tudarmstadt.rxrefactoring.core.ir;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;

public interface IReactiveOutput extends IReactiveValue {

	@NonNull
	NodeSupplier<? extends Expression> supplyOnNext(NodeSupplier<? extends Expression> expr);

	
}
