package repository;

import domain.Identifiable;
import filters.AbstractFilter;

import java.util.List;
import java.util.Optional;

public interface GenericRepository<T extends Identifiable<ID>, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    void save(T entity);
    void update(T entity);
    Optional<T> delete(ID id);
    public List<T> findWithFilter(AbstractFilter<T> filter);
}