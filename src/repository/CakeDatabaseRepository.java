package repository;

import domain.Cake;
import filters.AbstractFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class CakeDatabaseRepository extends DatabaseRepository<Cake, Integer> {

    private Connection connection;
    public CakeDatabaseRepository(Connection connection) {
        super("cakes", connection);
        this.connection = connection;
    }

    @Override
    protected Cake mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Cake(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("flavor"),
                rs.getInt("size")
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO cakes (id, name, price, flavor, size) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE cakes SET id=?, name = ?, price = ?, flavor = ?, size = ? WHERE id = ?";
    }


    @Override
    protected void setStatementParameters(PreparedStatement stmt, Cake entity) throws SQLException {
        stmt.setInt(1, entity.getId());
        stmt.setString(2, entity.getName());
        stmt.setDouble(3, entity.getPrice());
        stmt.setString(4, entity.getFlavor());
        stmt.setInt(5, entity.getSize());
        stmt.setInt(6, entity.getId());
    }

    @Override
    public List<Cake> findWithFilter(AbstractFilter<Cake> filter) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM cakes")) {
            try (ResultSet rs = stmt.executeQuery()) {
                return Stream.generate(() -> {
                            try {
                                return rs.next() ? mapResultSetToEntity(rs) : null;
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .takeWhile(cake -> cake != null)
                        .filter(filter::matches)
                        .toList(); // Collects filtered results into a list
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching cakes with filter", e);
        }
    }
    public List<Cake> generateCakes(int count) {
        List<Cake> cakes = new ArrayList<>();
        Random random = new Random();
        String[] flavors = {"vanilla", "chocolate", "strawberry", "lemon"};

        // Fetch the maximum existing ID from the database
        int maxExistingId = getMaxIdFromDatabase();

        // Start generating new cakes from the next available ID
        for (int i = 0; i < count; i++) {
            int newId = maxExistingId + i + 1;  // Ensure new IDs start from maxExistingId + 1
            String flavor = flavors[random.nextInt(flavors.length)];
            double price = 10 + (100 - 10) * random.nextDouble();  // Random price between 10 and 100
            int size = random.nextInt(3) + 1;  // Random size: 1, 2, or 3
            Cake cake = new Cake(newId, "Cake " + newId, price, flavor, size);

            // Save the new cake to the database
            this.save(cake);

            // Add the cake to the local list
            cakes.add(cake);
        }

        return cakes;
    }

    private int getMaxIdFromDatabase() {
        String sql = "SELECT MAX(id) AS max_id FROM cakes";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("max_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching maximum ID from database", e);
        }
        return 0;  // Return 0 if no records are found
    }

    public Connection getConnection() {
        return connection;
    }
}
