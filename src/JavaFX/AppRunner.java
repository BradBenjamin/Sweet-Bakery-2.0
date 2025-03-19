package JavaFX;

import actions.IAction;
import domain.Cake;
import domain.Order;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class AppRunner extends Application  {

    private final ObservableList<Cake> cakes = FXCollections.observableArrayList();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    protected Stack<IAction> undoStack = new Stack<>();
    protected Stack<IAction> redoStack = new Stack<>();
    // Database connection details
    private static final String DB_URL = "jdbc:sqlite:identifier.sqlite";

    /** Load cakes from the database */
    private void loadCakes() {
        cakes.clear();
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM cakes")) {

            while (resultSet.next()) {
                cakes.add(new Cake(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        resultSet.getString("flavor"),
                        resultSet.getInt("size")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading cakes from database.");
        }
    }

    /** Load orders from the database */
    private void loadOrders() {
        orders.clear();
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM orders")) {

            while (resultSet.next()) {
                List<Integer> cakeIds = new ArrayList<>();
                String[] ids = resultSet.getString("cake_ids").split(",");
                for (String id : ids) cakeIds.add(Integer.parseInt(id.trim()));

                orders.add(new Order(
                        resultSet.getInt("id"),
                        resultSet.getString("customer_name"),
                        resultSet.getString("status"),
                        cakeIds
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading orders from database.");
        }
    }

    /** Show error message */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage stage) {
        TableView<Cake> cakeTableView = createCakeTableView();
        TableView<Order> orderTableView = createOrderTableView();
        TextField cakeIdInput = new TextField();
        cakeIdInput.setPromptText("Cake ID");
        TextField cakeNameInput = new TextField();
        cakeNameInput.setPromptText("Name");
        TextField cakePriceInput = new TextField();
        cakePriceInput.setPromptText("Price");
        TextField cakeFlavorInput = new TextField();
        cakeFlavorInput.setPromptText("Flavor");
        TextField cakeSizeInput = new TextField();
        cakeSizeInput.setPromptText("Size");

        Label cakeErrorLabel = new Label();

        // Input fields for cake generation
        TextField cakeGenerateCountInput = new TextField();
        cakeGenerateCountInput.setPromptText("Number of Cakes to Generate");

        // Buttons for Cakes
        Button addCakeButton = new Button("Add Cake");
        addCakeButton.setOnAction(e -> handleAddCake(cakeIdInput, cakeNameInput, cakePriceInput, cakeFlavorInput, cakeSizeInput, cakeErrorLabel));

        Button deleteCakeButton = new Button("Delete Cake");
        deleteCakeButton.setOnAction(e -> handleDeleteCake(cakeIdInput, cakeErrorLabel));

        Button filterFlavorButton = new Button("Filter by Flavor");
        filterFlavorButton.setOnAction(e -> filterCakesByFlavor(cakeFlavorInput.getText()));

        Button filterPriceButton = new Button("Filter by Price");
        filterPriceButton.setOnAction(e -> filterCakesByPrice(cakePriceInput.getText()));

        Button generateCakesButton = new Button("Generate Cakes");
        generateCakesButton.setOnAction(e -> {
            try {
                int count = Integer.parseInt(cakeGenerateCountInput.getText());
                generateAndAddCakes(count);
                loadCakes(); // Reload the cakes to reflect the new data
                cakeErrorLabel.setText("Successfully generated " + count + " new cakes.");
            } catch (NumberFormatException ex) {
                cakeErrorLabel.setText("Invalid input! Please enter a numeric value.");
            } catch (Exception ex) {
                cakeErrorLabel.setText("Error generating cakes: " + ex.getMessage());
            }
        });

        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e->undo());

        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e->undo());

        // Layout
        HBox cakeFields = new HBox(10, cakeIdInput, cakeNameInput, cakePriceInput, cakeFlavorInput, cakeSizeInput);
        HBox cakeButtons = new HBox(10, addCakeButton, deleteCakeButton, filterFlavorButton, filterPriceButton);

        // Layout for cake generation
        HBox cakeGenerateFields = new HBox(10, cakeGenerateCountInput, generateCakesButton);

        VBox layout = new VBox(10,
                new Label("Cakes:"), cakeFields, cakeButtons, cakeGenerateFields, cakeErrorLabel, cakeTableView,
                new Label("Orders:"), orderTableView
        );

        // Scene
        Scene scene = new Scene(layout, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Cake and Order Management");
        stage.show();

        // Load data
        loadCakes();
        loadOrders();
    }

    private TableView<Cake> createCakeTableView() {
        TableView<Cake> tableView = new TableView<>();
        TableColumn<Cake, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        TableColumn<Cake, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        TableColumn<Cake, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPrice()));
        TableColumn<Cake, String> flavorCol = new TableColumn<>("Flavor");
        flavorCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getFlavor()));
        tableView.getColumns().addAll(idCol, nameCol, priceCol, flavorCol);
        tableView.setItems(cakes);
        return tableView;
    }

    private TableView<Order> createOrderTableView() {
        TableView<Order> tableView = new TableView<>();

        // ID column for orders
        TableColumn<Order, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(o -> new ReadOnlyObjectWrapper<>(o.getValue().getId()));

        // Customer name column
        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(o -> new ReadOnlyObjectWrapper<>(o.getValue().getCustomerName()));

        // Status column
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(o -> new ReadOnlyObjectWrapper<>(o.getValue().getStatus()));

        // Cakes ordered column (showing the list of cake IDs)
        TableColumn<Order, String> cakesCol = new TableColumn<>("Cakes Ordered");
        cakesCol.setCellValueFactory(o -> {
            // Concatenate cake IDs into a comma-separated string
            List<Integer> cakeIds = o.getValue().getCakesOrdered();
            return new ReadOnlyObjectWrapper<>(String.join(", ", cakeIds.stream().map(String::valueOf).toList()));
        });

        // Add all columns to the table
        tableView.getColumns().addAll(idCol, customerCol, statusCol, cakesCol);
        tableView.setItems(orders);

        return tableView;
    }


    private void handleAddCake(TextField id, TextField name, TextField price, TextField flavor, TextField size, Label error) {
        try {
            int cakeId = Integer.parseInt(id.getText());
            String cakeName = name.getText();
            double cakePrice = Double.parseDouble(price.getText());
            String cakeFlavor = flavor.getText();
            int cakeSize = Integer.parseInt(size.getText());

            // Insert into database
            String insertSQL = "INSERT INTO cakes (id, name, price, flavor, size) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setInt(1, cakeId);
                pstmt.setString(2, cakeName);
                pstmt.setDouble(3, cakePrice);
                pstmt.setString(4, cakeFlavor);
                pstmt.setInt(5, cakeSize);
                pstmt.executeUpdate();}
            // Reload cakes from the database
            loadCakes();
            error.setText(""); // Clear any error message
        } catch (NumberFormatException e) {
            error.setText("Invalid input! Please enter valid numeric values for ID, Price, and Size.");
        } catch (SQLException e) {
            error.setText("Error saving cake: " + e.getMessage());
        }
    }

    private void handleDeleteCake(TextField id, Label error) {
        try {
            int cakeId = Integer.parseInt(id.getText());

            // Delete from database
            String deleteSQL = "DELETE FROM cakes WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                pstmt.setInt(1, cakeId);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted == 0) {
                    error.setText("Cake not found with ID: " + cakeId);
                } else {
                    // Reload cakes from the database
                    loadCakes();
                    error.setText(""); // Clear any error message
                }
            }
        } catch (NumberFormatException e) {
            error.setText("Invalid input! Cake ID must be numeric.");
        } catch (SQLException e) {
            error.setText("Error deleting cake: " + e.getMessage());
        }
    }

    private void handleAddOrder(TextField id, TextField customer, TextField status, TextField cakeIds, Label error) {
        try {
            int orderId = Integer.parseInt(id.getText());
            String customerName = customer.getText();
            String orderStatus = status.getText();

            // Parse and validate cake IDs
            String[] cakeIdArray = cakeIds.getText().split(",");
            if (cakeIdArray.length == 0) {
                error.setText("Error: Cake IDs cannot be empty.");
                return;
            }
            String cakeIdsFormatted = String.join(",", cakeIdArray);

            // Insert into database
            String insertSQL = "INSERT INTO orders (id, customer_name, status, cake_ids) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setInt(1, orderId);
                pstmt.setString(2, customerName);
                pstmt.setString(3, orderStatus);
                pstmt.setString(4, cakeIdsFormatted);
                pstmt.executeUpdate();
            }

            // Reload orders from the database
            loadOrders();
            error.setText("Order added successfully.");
        } catch (NumberFormatException e) {
            error.setText("Invalid input! Order ID and Cake IDs must be numeric.");
        } catch (SQLException e) {
            error.setText("Error saving order: " + e.getMessage());
        }
    }

    private void handleDeleteOrder(TextField id, Label error) {
        try {
            int orderId = Integer.parseInt(id.getText());

            // Delete from database
            String deleteSQL = "DELETE FROM orders WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                pstmt.setInt(1, orderId);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted == 0) {
                    error.setText("Order not found with ID: " + orderId);
                } else {
                    // Reload orders from the database
                    loadOrders();
                    error.setText(""); // Clear any error message
                }
            }
        } catch (NumberFormatException e) {
            error.setText("Invalid input! Order ID must be numeric.");
        } catch (SQLException e) {
            error.setText("Error deleting order: " + e.getMessage());
        }
    }

    private void filterCakesByFlavor(String flavor) {
        cakes.removeIf(cake -> !cake.getFlavor().equalsIgnoreCase(flavor));
    }

    private void filterCakesByPrice(String price) {
        double maxPrice = Double.parseDouble(price);
        cakes.removeIf(cake -> cake.getPrice() > maxPrice);
    }

    private void handleUpdateCake(TextField id, TextField name, TextField price, TextField flavor, TextField size, Label error) {
        try {
            int cakeId = Integer.parseInt(id.getText());
            String cakeName = name.getText();
            double cakePrice = Double.parseDouble(price.getText());
            String cakeFlavor = flavor.getText();
            int cakeSize = Integer.parseInt(size.getText());

            // Update in database
            String updateSQL = "UPDATE cakes SET name = ?, price = ?, flavor = ?, size = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, cakeName);
                pstmt.setDouble(2, cakePrice);
                pstmt.setString(3, cakeFlavor);
                pstmt.setInt(4, cakeSize);
                pstmt.setInt(5, cakeId);

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    loadCakes(); // Reload cakes
                    error.setText("Cake updated successfully.");
                } else {
                    error.setText("No cake found with ID: " + cakeId);
                }
            }
        } catch (NumberFormatException e) {
            error.setText("Invalid input! Please enter valid numeric values for ID, Price, and Size.");
        } catch (SQLException e) {
            error.setText("Error updating cake: " + e.getMessage());
        }
    }

    private void handleUpdateOrder(TextField id, TextField customer, TextField status, TextField cakeIds, Label error) {
        try {
            int orderId = Integer.parseInt(id.getText());
            String customerName = customer.getText();
            String orderStatus = status.getText();

            // Parse and format cake IDs
            String[] cakeIdArray = cakeIds.getText().split(",");
            String cakeIdsFormatted = String.join(",", cakeIdArray);

            // Update in database
            String updateSQL = "UPDATE orders SET customer_name = ?, status = ?, cake_ids = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, customerName);
                pstmt.setString(2, orderStatus);
                pstmt.setString(3, cakeIdsFormatted);
                pstmt.setInt(4, orderId);

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    loadOrders(); // Reload orders
                    error.setText("Order updated successfully.");
                } else {
                    error.setText("No order found with ID: " + orderId);
                }
            }
        } catch (NumberFormatException e) {
            error.setText("Invalid input! Order ID and Cake IDs must be numeric.");
        } catch (SQLException e) {
            error.setText("Error updating order: " + e.getMessage());
        }
    }

    private void generateAndAddCakes(int count) {
        String insertSQL = "INSERT INTO cakes (id, name, price, flavor, size) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            // Find the current maximum ID in the cakes table
            int maxId = 0;
            String queryMaxIdSQL = "SELECT MAX(id) AS max_id FROM cakes";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(queryMaxIdSQL)) {
                if (rs.next()) {
                    maxId = rs.getInt("max_id");
                }
            }

            // Start generating IDs from maxId + 1
            int currentId = maxId + 1;

            for (int i = 0; i < count; i++) {
                int id = currentId++; // Increment ID for each new cake
                String name = "Cake " + id;
                double price = Math.round((10 + Math.random() * 50) * 100.0) / 100.0; // Price between 10 and 60
                String[] flavors = {"Chocolate", "Vanilla", "Strawberry", "Lemon", "Caramel"};
                String flavor = flavors[(int) (Math.random() * flavors.length)];
                int size = 6 + (int) (Math.random() * 15); // Size between 6 and 20

                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setDouble(3, price);
                pstmt.setString(4, flavor);
                pstmt.setInt(5, size);
                pstmt.addBatch(); // Add to batch
            }
            pstmt.executeBatch(); // Execute the batch insert

        } catch (SQLException e) {
            throw new RuntimeException("Error generating cakes: " + e.getMessage(), e);
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            IAction action = undoStack.pop();
            action.executeUndo();
            redoStack.push(action);
        } else {
            System.out.println("No actions to undo.");
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            IAction action = redoStack.pop();
            action.executeRedo();
            undoStack.push(action);
        } else {
            System.out.println("No actions to redo.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
