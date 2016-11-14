package framework;

import java.nio.file.Paths;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.After;
import org.junit.Before;

/**
 * Description: Abstract test for android projects<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/13/2016
 */
public abstract class AbstractAndroidTest extends AbstractTest
{
    private IProject project;

    @Before
    public void openAndroidProject() throws Exception
    {
        String absolutPath = Paths.get(
                "resources",
                "android.test.app",
                "RxRefactoringTestingApp",
                ".project"
        ).toAbsolutePath().toString();

        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        project = workspaceRoot.getProject("RxRefactoringTestingApp");
        IProjectDescription description = project.getWorkspace().loadProjectDescription(new org.eclipse.core.runtime.Path(absolutPath));
        project.create(description, null);
        project.open(null);
    }

    @After
    public void closeAndroidProject() throws Exception
    {
        project.close(null);
    }
}
