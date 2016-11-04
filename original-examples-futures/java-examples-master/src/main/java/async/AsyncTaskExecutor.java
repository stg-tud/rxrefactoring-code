package async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Async Task Executor, executes task asynchronously using a thread pool and
 * allows option to attach a {@link AsyncFutureListener} with
 * {@link AsyncFuture} object.
 * 
 * This tries to overcome the limitation the concurrent Api {@link Future} that
 * does not support the an async listener approach.
 * 
 * @author Rahul Jain
 * 
 */
public class AsyncTaskExecutor {

    private ExecutorService executor;

    public AsyncTaskExecutor() {
        this(1);
    }

    public AsyncTaskExecutor(int nThreads) {
        executor = Executors.newFixedThreadPool(nThreads);
    }

    public <T> AsyncFuture<T> execute(final Task<T> task) {
        CallableTask<T> r = new CallableTask<T>(task);
        return new AsyncFutureImpl<T>(executor.submit(r));
    }

    class CallableTask<T> implements Callable<T> {

        private T result;
        private Task<T> task;

        public CallableTask(Task<T> task) {
            this.task = task;
        }

        public T call() {
            try {
                result = task.execute();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    class AsyncFutureImpl<T> implements AsyncFuture<T> {

        private Future<T> future;

        public AsyncFutureImpl(Future<T> future) {
            this.future = future;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return future.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(timeout, unit);
        }

        @Override
        public void setListener(AsyncFutureListener<T> listener) {
            T result = null;
            Exception ex = null;
            try {
                result = get();
            } catch (InterruptedException e) {
                ex = e;
            } catch (ExecutionException e) {
                ex = e;
            }
            final AsyncResult<T> asyncResult = new AsyncResultImpl<T>(result, ex);
            listener.onResult(asyncResult);
        }
    }

    class AsyncResultImpl<T> implements AsyncResult<T> {

        private T result;
        private Throwable ex;

        public AsyncResultImpl(T result, Throwable ex) {
            this.result = result;
            this.ex = ex;
        }

        @Override
        public T getResult() {
            return result;
        }

        @Override
        public Throwable getException() {
            return ex;
        }

    }

}
