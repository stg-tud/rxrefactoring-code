package async;

public interface AsyncResult<T> {

    T getResult();

    Throwable getException();
}
