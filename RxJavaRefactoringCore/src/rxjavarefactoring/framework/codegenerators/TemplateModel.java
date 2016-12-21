package rxjavarefactoring.framework.codegenerators;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import rxjavarefactoring.framework.utils.PluginUtils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class TemplateModel
{
	private static final String FILE_ENCODING = "UTF-8";
	private Map<String, String> values;
	private String template;

	public TemplateModel( String pluginId, String templateDir, String templateName )
	{
		values = new HashMap<>();
		String pluginDir = PluginUtils.getPluginDir( pluginId );
		Path templatePath = Paths.get( pluginDir, templateDir, templateName ).toAbsolutePath();
		try
		{
			template = FileUtils.readFileToString( templatePath.toFile(), FILE_ENCODING );
		}
		catch ( IOException e )
		{
			throw new IllegalArgumentException( "No template found in " + templatePath.toAbsolutePath().toString(), e );
		}

	}

	public void put( String key, String value )
	{
		values.put( key, value );
	}

	public String getProcessedTemplate()
	{

		final StringBuffer sb = new StringBuffer();
		final Pattern pattern = Pattern.compile( "\\$\\{(.*?)\\}", Pattern.DOTALL );
		final Matcher matcher = pattern.matcher( template );
		while ( matcher.find() )
		{
			final String key = matcher.group( 1 );
			final String replacement = values.get( key );
			if ( replacement == null )
			{
				throw new IllegalArgumentException( "Template contains unmapped key: " + key );
			}
			matcher.appendReplacement( sb, replacement );
		}
		matcher.appendTail( sb );
		return sb.toString();

	}
}
