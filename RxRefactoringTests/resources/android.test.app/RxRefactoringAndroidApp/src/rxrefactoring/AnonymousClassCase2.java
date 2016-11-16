package rxrefactoring;

import android.os.AsyncTask;

public class AnonymousClassCase2
{
    public void someMethod()
    {
        // Testing doInBackground only
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                int a = 1;
                int b = 2;
                int c = a + b;
                return c;
            }
        }.execute();
    }
}