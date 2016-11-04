package async;

public interface AsyncFutureListener<T> {

    void onResult(AsyncResult<T> result);
}
