package rxjavarefactoring.framework.utils;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.osgi.framework.Bundle;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class PluginUtils
{
	public static String getPluginDir( String pluginId )
	{
		/* get bundle with the specified id */
		Bundle bundle = Platform.getBundle( pluginId );
		if ( bundle == null )
			throw new RuntimeException( "Could not resolve plugin: " + pluginId + "\r\n" +
					"Probably the plugin has not been correctly installed.\r\n" +
					"Running eclipse from shell with -clean option may rectify installation." );

		/* resolve Bundle::getEntry to local URL */
		URL pluginURL = null;
		try
		{
			pluginURL = Platform.resolve( bundle.getEntry( "/" ) );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( "Could not get installation directory of the plugin: " + pluginId );
		}
		String pluginInstallDir = pluginURL.getPath().trim();
		if ( pluginInstallDir.length() == 0 )
			throw new RuntimeException( "Could not get installation directory of the plugin: " + pluginId );

		/*
		 * since path returned by URL::getPath starts with a forward slash, that
		 * is not suitable to run commandlines on Windows-OS, but for Unix-based
		 * OSes it is needed. So strip one character for windows. There seems
		 * to be no other clean way of doing this.
		 */
		if ( Platform.getOS().compareTo( Platform.OS_WIN32 ) == 0 )
			pluginInstallDir = pluginInstallDir.substring( 1 );

		return pluginInstallDir;
	}

	public static String getCompilationUnitFullName(CompilationUnit cu)
	{
		return cu.getPackage().toString()
				.replaceAll( "package ", "" )
				.replaceAll( ";", "." + cu.getJavaElement().getElementName() )
				.replaceAll( "\n", "" )
				.replaceAll( "\\.java", "" );
	}
}
