package repository;

import domain.Order;
import filters.AbstractFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDatabaseRepository extends DatabaseRepository<Order, Integer> {

    public OrderDatabaseRepository(Connection connection) {
        super("orders", connection);
    }

    @Override
    protected Order mapResultSetToEntity(ResultSet rs) throws SQLException {
        List<Integer> cakeIds = new ArrayList<>();
        String cakeIdsString = rs.getString("cake_ids");
        if (cakeIdsString != null && !cakeIdsString.isEmpty()) {
            for (String id : cakeIdsString.split(",")) {
                cakeIds.add(Integer.parseInt(id.trim()));
            }
        }

        return new Order(
                rs.getInt("id"),
                rs.getString("customer_name"),
                rs.getString("status"),
                cakeIds
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO orders (id, customer_name, status, cake_ids) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE orders SET customer_name = ?, status = ?, cake_ids = ? WHERE id = ?";
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, Order entity) throws SQLException {
        stmt.setInt(1, entity.getId());
        stmt.setString(2, entity.getCustomerName());
        stmt.setString(3, entity.getStatus());
        // Convert the list of cake IDs to a comma-separated string
        String cakeIdsString = entity.getCakesOrdered().stream()
                .map(String::valueOf) // Convert each Integer to String
                .collect(Collectors.joining(",")); // Join with commas
        stmt.setString(4, cakeIdsString);
    }

    @Override
    public List<Order> findWithFilter(AbstractFilter<Order> filter) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders")) {
            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    Order order = mapResultSetToEntity(rs);
                    if (filter.matches(order)) {
                        orders.add(order);
                    }
                }
                return orders;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching orders with filter", e);
        }
    }
}
