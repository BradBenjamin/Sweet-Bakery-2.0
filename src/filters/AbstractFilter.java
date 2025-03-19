package filters;

import domain.Identifiable;

import java.util.List;

public interface AbstractFilter<T extends Identifiable<?>> {
    List<T> applyFilter(List<T> items);
    boolean matches(T entity);
}