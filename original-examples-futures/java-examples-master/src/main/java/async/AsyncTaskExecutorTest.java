package async;

import java.util.concurrent.ExecutionException;

public class AsyncTaskExecutorTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        AsyncTaskExecutor taskExecutor = new AsyncTaskExecutor(); // not a general pattern
        Task<Long> task = new LongTask();
        AsyncFuture<Long> future = taskExecutor.execute(task);
        future.setListener(new AsyncFutureListener<Long>() {
            @Override
            public void onResult(AsyncResult<Long> result) {
                System.out.println("Result Received is " + result.getResult());
            }
        });

        Task<Data> dataTask = new GetTask();
        AsyncFuture<Data> datafuture = taskExecutor.execute(dataTask);
        datafuture.setListener(new AsyncFutureListener<Data>() {
            @Override
            public void onResult(AsyncResult<Data> result) {
                System.out.println("Result Received is " + result.getResult());
            }
        });
    }

    static class Data {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
