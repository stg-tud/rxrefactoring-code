package async;

import java.io.Serializable;

public interface Task<T> extends Serializable {

    T execute() throws Exception;
}
