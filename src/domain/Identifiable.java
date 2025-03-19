package domain;

public interface Identifiable<T> {
    T getId();
    void setId(T id);
}