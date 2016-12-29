package rxrefactoring;

import javax.swing.*;

public class FieldDeclaration
{
	private SwingWorker<String, Integer> swingWorker;
	
	private SwingWorker<String, String> anotherWorker = new SwingWorker<String, String>(){
		@Override
		protected String doInBackground() throws Exception 
		{
			return "AsyncResult";
		}
	};

	private static SwingWorker aThirdSwingWorker;
}