package repository;

import domain.Cake;
import domain.Identifiable;
import domain.Order;
import filters.AbstractFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilteredFileRepository<T extends Identifiable<ID> & Serializable, ID> implements GenericRepository<T, ID> {
    private List<T> storage = new ArrayList<>();
    private final String filePath;

    // Constructor for in-memory usage
    public FilteredFileRepository() {
        this.filePath = null;
    }
    // Constructor for file-based usage
    public FilteredFileRepository(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }
    private void loadFromFile() {
        if(filePath == null) {
            return;
        }
        if(filePath.endsWith(".bin")){
            readFromBinaryFile();// Load initial data if file exists
        }
        else if(filePath.endsWith(".txt")){
            readFromTextFile();
        } else
            throw new IllegalArgumentException("Invalid file format");
    }

    @Override
    public List<T> findAll() {
        if (filePath == null) {
            return new ArrayList<>(storage); // In-memory data
        } else {
            loadFromFile(); // Always refresh from file
            return new ArrayList<>(storage);
        }
    }

    // Save a new item
    @Override
    public void save(T entity) {
        loadFromFile();
        storage.add(entity);
        writeToFile();
    }

    // Update an existing item
    @Override
    public void update(T entity) {
        loadFromFile();
        storage = storage.stream()
                .filter(item -> !item.getId().equals(entity.getId())) // Remove existing
                .collect(Collectors.toList());
        storage.add(entity); // Add updated item
        writeToFile();
    }

    // Delete an item by ID
    @Override
    public Optional<T> delete(ID id) {
        loadFromFile();
        Optional<T> deleted_item=findAll().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
        storage = storage.stream()
                .filter(item -> !item.getId().equals(id)) // Remove matching ID
                .collect(Collectors.toList());
        writeToFile();
        return deleted_item;
    }

    // Find an item by ID
    @Override
    public Optional<T> findById(ID id) {
        return findAll().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    // Filter items using an AbstractFilter
    public List<T> findWithFilter(AbstractFilter<T> filter) {
        return filter.applyFilter(findAll());
    }
    private void readFromBinaryFile() {
        if (filePath == null) return; // Skip if no file specified
        File file = new File(filePath);
        if(file.length()==0){
            storage=new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            storage = (List<T>) ois.readObject();
        } catch (FileNotFoundException e) {
            storage = new ArrayList<>(); // Initialize empty storage if file doesn't exist
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    protected void readFromTextFile() {
        List<T> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                T item = parseItemFromString(line);
                if (item != null) {
                    items.add(item);
                }
            }
            storage = items; // Assign to storage
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeToFile() {
        if (filePath == null) return;
        if(filePath.endsWith(".bin")){
            writeToBinaryFile();
        }
        else if(filePath.endsWith(".txt")){
            writeToTextFile();
        }
    }
    private void writeToBinaryFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(storage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeToTextFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (T item : storage) {
                writer.write(item.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private T parseItemFromString(String line) {
        if (line.startsWith("Cake")) {
            try {
                // Use regex to parse Cake fields
                Pattern pattern = Pattern.compile("id=(\\d+), name='([^']+)', price=(\\d+(\\.\\d+)?), flavor='([^']+)', size=(\\d+)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    int id = Integer.parseInt(matcher.group(1));
                    String name = matcher.group(2);
                    double price = Double.parseDouble(matcher.group(3));
                    String flavor = matcher.group(5);
                    int size = Integer.parseInt(matcher.group(6));

                    return (T) new Cake(id, name, price, flavor, size);
                } else {
                    throw new IllegalArgumentException("Invalid Cake format: " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else if (line.startsWith("Order")) {
            try {
                // Use regex to parse Order fields
                Pattern pattern = Pattern.compile("id=(\\d+), customerName='(.*?)', status='([^']+)', cakesOrdered=\\[(.*?)]");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    int id = Integer.parseInt(matcher.group(1));
                    String customerName = matcher.group(2).replace("''", "'"); // Handle extra quotes
                    String status = matcher.group(3);
                    String cakesOrderedString = matcher.group(4);

                    // Parse cakes ordered
                    List<Integer> cakes = parseCakes(cakesOrderedString);

                    return (T) new Order(id, customerName, status, cakes);
                } else {
                    throw new IllegalArgumentException("Invalid Order format: " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported format: " + line);
        }
    }

    // Helper method to parse cake IDs from a string
    private List<Integer> parseCakes(String cakesOrderedString) {
        List<Integer> cakes = new ArrayList<>();
        if (!cakesOrderedString.isEmpty()) {
            String[] ids = cakesOrderedString.split(",\\s*");
            for (String id : ids) {
                cakes.add(Integer.parseInt(id));
            }
        }
        return cakes;
    }

}
