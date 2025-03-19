package repository;

import domain.Identifiable;
import filters.AbstractFilter;

import java.util.*;
import java.util.stream.Collectors;

public class GenericRepositoryImpl<T extends Identifiable<ID>, ID> implements GenericRepository<T, ID> {

    private final Map<ID, T> dataStore = new HashMap<>();

    @Override
    public List<T> findAll() {
        return new ArrayList<>(dataStore.values());
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(dataStore.get(id));
    }

    @Override
    public void save(T entity) {
        dataStore.put(entity.getId(), entity);
    }

    @Override
    public void update(T entity) {
        if (!dataStore.containsKey(entity.getId())) {
            throw new IllegalArgumentException("Entity with ID " + entity.getId() + " does not exist.");
        }
        dataStore.put(entity.getId(), entity);
    }

    @Override
    public Optional<T> delete(ID id) {
        return Optional.ofNullable(dataStore.remove(id));
    }

    @Override
    public List<T> findWithFilter(AbstractFilter<T> filter) {
        return dataStore.values().stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
    }

    // Additional helper method for the ActionRemove class
    public void add(T entity) {
        save(entity);
    }

    public void delete(T entity) {
        dataStore.remove(entity.getId());
    }
}
