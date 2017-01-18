package visitors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import rxjavarefactoring.framework.refactoring.AbstractCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class ExtCollector extends AbstractCollector
{
    private IProject project;

    // TODO 4 - consider using maps to collect the information
    // Below List<ASTNode> was used because the template doesn't known
    // which nodes are required. You should be specifying the node more
    // concrete. I.e: TypeDeclaration or MethodInvocation etc.

    // If you need more than one type of nodes, then consider using
    // one map for each. This will facilitate the implementation of the
    // workers.
    private final Map<ICompilationUnit, List<ASTNode>> exampleMap;

    public ExtCollector(IProject project, String collectorName)
    {
        super(collectorName);
        this.project = project;
        exampleMap = new HashMap<>();
    }

    // TODO 5 - adjust method as you need.
    // This example was added to be able to start writing a template for the worker
    public Map<ICompilationUnit, List<ASTNode>> getExampleMap()
    {
        return exampleMap;
    }
}
