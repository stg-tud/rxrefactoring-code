package rxrefactoring;

import android.os.AsyncTask;

public class AnonymousClassCase3
{
    public void someMethod()
    {
        // Testing doInBackground only with empty onPostExecute declaration
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                int a = 1;
                int b = 2;
                int c = a + b;
                return c;
            }

            @Override
            protected void onPostExecute(Integer number) {
                // empty block
            }
        }.execute();
    }
}