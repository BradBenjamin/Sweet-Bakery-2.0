package repository;

import domain.Identifiable;
import filters.AbstractFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemoryRepository<T extends Identifiable<ID>, ID> implements GenericRepository<T, ID> {
    private Map<ID, T> storage = new HashMap<>();

    @Override
    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void save(T entity) {
        storage.put(entity.getId(), entity);
    }

    @Override
    public void update(T entity) {
        storage.put(entity.getId(), entity);
    }

    @Override
    public Optional<T> delete(ID id) {
        T item = storage.get(id); // Get the item before removing it

        if (item != null) {
            storage.remove(id); // Delete the item
            return Optional.of(item); // Return the deleted item wrapped in Optional
        }

        return Optional.empty(); // Item was not found
    }

    @Override
    public List<T> findWithFilter(AbstractFilter<T> filter) {
        return List.of();
    }
}