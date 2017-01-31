package visitors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
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

    private final Map<ICompilationUnit,List<EnhancedForStatement>> forBlocks;

    public ExtCollector(IProject project, String collectorName)
    {
        super(collectorName);
        this.project = project;
        forBlocks= new HashMap<>();
    }

    public Map<ICompilationUnit, List<EnhancedForStatement>> getForBlocks() {
        return forBlocks;
    }

    public void addForBlock( ICompilationUnit cu, List<EnhancedForStatement> forBlocksList )
    {
        addToMap( cu, forBlocksList, forBlocks );
    }

    @Override
    public String getInfo()
    {
        return "\n******************************************************************\n" +
                getDetails() +
                "\n******************************************************************";
    }

    @Override
    public String getError()
    {
        return "\n******************************************************************\n" +
                " [ ERROR during refactoring ]\n" +
                getDetails() +
                "\n******************************************************************";
    }

    public String getDetails()
    {
        return "Project=" + project.getName() + "\n" +
                "ForBlocks=" + forBlocks.values().size();
    }
}
