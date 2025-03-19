package repository;

import domain.Identifiable;
import repository.GenericRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DatabaseRepository<T extends Identifiable<ID>, ID> implements GenericRepository<T, ID> {

    protected final String tableName;
    protected final Connection connection;

    public DatabaseRepository(String tableName, Connection connection) {
        this.tableName = tableName;
        this.connection = connection;
    }

    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    @Override
    public List<T> findAll() {
        List<T> results = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public Optional<T> findById(ID id) {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(T entity) {
        String sql = getInsertSQL();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setStatementParameters(stmt, entity);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted == 0) {
                throw new RuntimeException("Insert failed. No rows affected.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error during insert operation: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(T entity) {
        String sql = getUpdateSQL();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setStatementParameters(stmt, entity);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Update failed. The entity may not exist.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error during update operation: " + e.getMessage(), e);
        }
    }



    protected abstract String getInsertSQL();

    protected abstract String getUpdateSQL();

    protected abstract void setStatementParameters(PreparedStatement stmt, T entity) throws SQLException;

    @Override
    public Optional<T> delete(ID id) {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                // If the item exists, return it and delete
                if (rs.next()) {
                    // Map the ResultSet to an entity (you will need to implement this logic)
                    T item = mapResultSetToEntity(rs); // Assuming you will define this method

                    // Perform the delete operation
                    String deleteQuery = "DELETE FROM " + tableName + " WHERE id = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                        deleteStmt.setObject(1, id);
                        deleteStmt.executeUpdate(); // Perform the delete operation
                    }

                    return Optional.of(item);  // Return the deleted item wrapped in Optional
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exception as needed
        }

        return Optional.empty();  // If no item found or error occurred, return empty
    }
}
