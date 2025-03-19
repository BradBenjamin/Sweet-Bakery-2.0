/*package repository;

import domain.Identifiable;
import filters.AbstractFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BinaryFileRepository<T extends Identifiable<ID> & Serializable, ID> implements GenericRepository<T, ID> {
    private final String filePath;

    // Constructor that takes the file path as an argument
    public BinaryFileRepository(String filePath) {
        this.filePath = filePath;
    }

    // Retrieves all items from the binary file
    @Override
    public List<T> findAll() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<T>) ois.readObject(); // Deserialize list of items
        } catch (FileNotFoundException e) {
            return new ArrayList<>(); // Return empty list if file doesn't exist
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Finds an item by ID
    @Override
    public Optional<T> findById(ID id) {
        return findAll().stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    // Saves a new item to the binary file (appends to existing list)
    @Override
    public void save(T entity) {
        List<T> items = findAll();
        items.add(entity);
        writeToFile(items);
    }

    // Updates an existing item by rewriting the file with modified data
    @Override
    public void update(T entity) {
        List<T> items = findAll();
        items.removeIf(item -> item.getId().equals(entity.getId())); // Remove existing item
        items.add(entity); // Add updated item
        writeToFile(items);
    }

    // Deletes an item by ID and rewrites file without it
    @Override
    public void delete(ID id) {
        List<T> items = findAll();
        items.removeIf(item -> item.getId().equals(id)); // Remove item by ID
        writeToFile(items);
    }

    @Override
    public List<T> findWithFilter(AbstractFilter<T> filter) {
        return List.of();
    }

    // Writes the list to the file in binary format
    private void writeToFile(List<T> items) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(items); // Serialize list of items to binary file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}*/