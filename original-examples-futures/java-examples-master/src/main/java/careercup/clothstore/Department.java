package careercup.clothstore;

public interface Department<T extends Article> {

    void add(T article);
    void remove(T article);
}
