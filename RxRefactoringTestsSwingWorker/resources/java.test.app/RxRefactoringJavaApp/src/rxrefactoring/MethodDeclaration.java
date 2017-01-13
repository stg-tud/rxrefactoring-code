package rxrefactoring;

import javax.swing.*;

public class MethodDeclaration
{
	public void doSomething()
	{
		getSwingWorker().execute();
	}

	private SwingWorker<String, Integer> getSwingWorker()
	{
		return new SwingWorker<String,Integer>(){

			@Override
			protected String doInBackground() throws Exception {
				return null;
			}
		};
	};
}