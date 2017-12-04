package rxjavarefactoring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import domain.SwingWorkerInfo;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.PluginUtils;
import visitors.DiscoveringVisitor;
import visitors.RxCollector;
import workers.*;

/**
 * Description: Implementation of API of the RxJavaRefactoringTool<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class Extension implements RxJavaRefactoringExtension<RxCollector>
{
	public static final String PLUGIN_ID = "de.tudarmstadt.stg.rxjava.refactoring.extension.swingworker";
	public static final String TEMPLATES_DIR_NAME = "templates";

	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoringSwingWorker";
	private static final String RESOURCES_DIR_NAME = "resources";
	private static final String COLLECTOR_NAME = "SwingWorker";

	private static Configuration freemakerCfg;

	@Override
	public String getId()
	{
		return COMMAND_ID;
	}

	@Override
	public RxCollector getASTNodesCollectorInstance( IProject project )
	{
		return new RxCollector( project, COLLECTOR_NAME );
	}

	@Override
	public void processUnit( ICompilationUnit unit, RxCollector rxCollector )
	{
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );

		// Initialize Visitor
		String className = SwingWorkerInfo.getBinaryName();
		DiscoveringVisitor discoveringVisitor = new DiscoveringVisitor( className );

		// Collect information using visitor
		cu.accept( discoveringVisitor );

		// put the collected information into the collector
		rxCollector.add( unit, discoveringVisitor.getTypeDeclarations() );
		rxCollector.add( unit, discoveringVisitor.getFieldDeclarations() );
		rxCollector.add( unit, discoveringVisitor.getAssignments() );
		rxCollector.add( unit, discoveringVisitor.getVarDeclStatements() );
		rxCollector.add( unit, discoveringVisitor.getSimpleNames() );
		rxCollector.add( unit, discoveringVisitor.getClassInstanceCreations() );
		rxCollector.add( unit, discoveringVisitor.getMethodInvocations() );
		rxCollector.add( unit, discoveringVisitor.getSingleVarDeclarations() );
		rxCollector.add( unit, discoveringVisitor.getMethodDeclarations() );
	}

	@Override
	public Set<AbstractRefactorWorker<RxCollector>> getRefactoringWorkers( RxCollector rxCollector )
	{
		setupFreemaker();
		Set<AbstractRefactorWorker<RxCollector>> workers = new HashSet<>();
		workers.add( new AssignmentWorker( rxCollector ) );
		workers.add( new FieldDeclarationWorker( rxCollector ) );
		workers.add( new MethodInvocationWorker( rxCollector ) );
		workers.add( new VariableDeclStatementWorker( rxCollector ) );
		workers.add( new SimpleNameWorker( rxCollector ) );
		workers.add( new SingleVariableDeclWorker( rxCollector ) );
		workers.add( new ClassInstanceCreationWorker( rxCollector ) );
		workers.add( new TypeDeclarationWorker( rxCollector ) );
		workers.add( new MethodDeclarationWorker( rxCollector ) );
		return workers;
	}

	@Override
	public String getJarFilesPath()
	{
		String pluginDir = PluginUtils.getPluginDir( PLUGIN_ID );
		Path jarFilsPath = Paths.get( pluginDir, RESOURCES_DIR_NAME ).toAbsolutePath();
		return jarFilsPath.toString();
	}

	public static Configuration getFreemakerCfg()
	{
		return freemakerCfg;
	}

	private void setupFreemaker()
	{
		String pluginDir = PluginUtils.getPluginDir( PLUGIN_ID );
		String templatesDirName = Paths.get( pluginDir, TEMPLATES_DIR_NAME ).toAbsolutePath().toString();
		freemakerCfg = new Configuration( Configuration.VERSION_2_3_25 );
		freemakerCfg.setDefaultEncoding( "UTF-8" );
		freemakerCfg.setTemplateExceptionHandler( TemplateExceptionHandler.RETHROW_HANDLER );
		freemakerCfg.setLogTemplateExceptions( false );
		try
		{
			freemakerCfg.setDirectoryForTemplateLoading( new File( templatesDirName ) );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( "Exception in setDirectoryForTemplateLoading", e );
		}
	}
}
