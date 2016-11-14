package rx.refactoring.testingapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class AnonymousClassNoAssignment
{
    public void someMethod()
    {
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
                System.out.println(number);
            }
        }.execute();
    }
}