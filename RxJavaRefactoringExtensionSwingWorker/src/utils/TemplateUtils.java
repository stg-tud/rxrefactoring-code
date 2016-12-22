package utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import rxjavarefactoring.Extension;
import rxjavarefactoring.framework.utils.RxLogger;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public final class TemplateUtils
{
	private TemplateUtils()
	{
		// This class should not be instantiated
	}

	/**
	 * Processes a template with the given data
	 * 
	 * @param templateName
	 *            template name
	 * @param data
	 *            data to be place into the template
	 * @return the template after being processed
	 */
	public static String processTemplate( String templateName, Map<String, Object> data )
	{
		String observableString = "";
		try
		{
			Template template = Extension.getFreemakerCfg().getTemplate( templateName );
			StringWriter out = new StringWriter();
			template.process( data, out );
			observableString = out.toString();
		}
		catch ( IOException | TemplateException e )
		{
			RxLogger.error( TemplateUtils.class, "METHOD=createObservable - Failed", e );
			throw new IllegalArgumentException( "Template could not be processed. TEMPLATE=" + templateName + "DATA=" + data, e );
		}
		return observableString;
	}

}
