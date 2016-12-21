package rxjavarefactoring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import domain.SwingWorkerInfo;
import rxjavarefactoring.analyzers.DeclarationVisitor;
import rxjavarefactoring.analyzers.MethodInvocationVisitor;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.PluginUtils;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.processor.ASTNodesCollector;
import workers.AssigmentsWorker;

public class Extension implements RxJavaRefactoringExtension<ASTNodesCollector>
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
	public ASTNodesCollector getASTNodesCollectorInstance()
	{
		return new ASTNodesCollector( COLLECTOR_NAME );
	}

	@Override
	public void processUnit( ICompilationUnit unit, ASTNodesCollector collector )
	{
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );

		// Initialize Visitors
		String className = SwingWorkerInfo.getBinaryName();
		DeclarationVisitor declarationVisitor = new DeclarationVisitor( className );
		MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor(
				className,
				SwingWorkerInfo.getPublicMethodsMap() );

		// Collect information using visitors
		cu.accept( declarationVisitor );
		cu.accept( methodInvocationVisitor );

		String fullName = PluginUtils.getCompilationUnitFullName( cu );
		if ( declarationVisitor.isTargetClassFound() )
		{
			RxLogger.info( this, "METHOD=processUnit - " + className + " found in class: " + fullName );
		}

		if ( methodInvocationVisitor.isUsagesFound() )
		{
			RxLogger.info( this, "METHOD=processUnit - Method Invocation of " + className + " found in class: " + fullName );
		}

		// Cache the collected information from visitors in one collector
		collector.addSubclasses( unit, declarationVisitor.getSubclasses() );
		collector.addAnonymClassDecl( unit, declarationVisitor.getAnonymousClasses() );
		collector.addVariableDeclarations( unit, declarationVisitor.getVariableDeclarations() );
		collector.addAssignments( unit, declarationVisitor.getAssignments() );
		collector.addRelevantUsages( unit, methodInvocationVisitor.getUsages() );
	}

	@Override
	public Set<AbstractRefactorWorker<ASTNodesCollector>> getRefactoringWorkers( ASTNodesCollector collector )
	{
		setupFreemaker();
		Set<AbstractRefactorWorker<ASTNodesCollector>> workers = new HashSet<>();
		workers.add( new AssigmentsWorker( collector ) );
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
		String templatesDirName = Paths.get(pluginDir, TEMPLATES_DIR_NAME).toAbsolutePath().toString();
		freemakerCfg = new Configuration(Configuration.VERSION_2_3_25);
		freemakerCfg.setDefaultEncoding("UTF-8");
		freemakerCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemakerCfg.setLogTemplateExceptions(false);
		try
		{
			freemakerCfg.setDirectoryForTemplateLoading(new File(templatesDirName));
		}
		catch ( IOException e )
		{
			throw new RuntimeException("Exception in setDirectoryForTemplateLoading", e);
		}
	}
}
