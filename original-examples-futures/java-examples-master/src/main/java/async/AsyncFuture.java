package async;

import java.util.concurrent.Future;

public interface AsyncFuture<V> extends Future<V> {

    void setListener(AsyncFutureListener<V> listener);
}
