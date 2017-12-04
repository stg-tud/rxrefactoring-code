package rxrefactoring;

import javax.swing.*;

public class MethodInvocationSubclass
{

	public void main()
	{
		new MySwingWorker().execute();
	}

	class MySwingWorker extends SwingWorker
	{
		@Override
		protected Object doInBackground() throws Exception {
			return null;
		}

	}
}