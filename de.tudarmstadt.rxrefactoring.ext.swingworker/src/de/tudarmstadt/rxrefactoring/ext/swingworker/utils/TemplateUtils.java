package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.ext.swingworker.extensions.SwingWorkerExtension;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 18/01/2018
 */
public final class TemplateUtils {
	private static Object lock = new Object();

	private TemplateUtils() {
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
	public static String processTemplate(String templateName, Map<String, Object> data) {
		synchronized (lock) {
			String observableString = "";
			try {
				Template template = SwingWorkerExtension.getFreemakerCfg().getTemplate(templateName);
				StringWriter out = new StringWriter();
				template.process(data, out);
				observableString = out.toString();
			} catch (IOException | TemplateException e) {
				Log.error(TemplateUtils.class, "METHOD=createObservable - Failed", e);
				throw new IllegalArgumentException(
						"Template could not be processed. TEMPLATE=" + templateName + "DATA=" + data, e);
			}
			return observableString;
		}
	}

}
