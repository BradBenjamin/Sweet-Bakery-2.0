package UI;

import domain.Cake;
import domain.Order;
import filters.CakeFlavorFilter;
import filters.CakePriceFilter;
import repository.*;
import service.CakeService;
//import service.OrderService;
import service.OrderService;
import settings.Settings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CakeUI {

    private CakeService cakeService;
    private OrderService orderService;
    private Scanner scanner;

    public CakeUI() throws SQLException {

        this.scanner = new Scanner(System.in);
        String cakeRepositoryType = Settings.getProperty("cakeRepositoryType");
        String orderRepositoryType = Settings.getProperty("orderRepositoryType");
        String cakesFile = Settings.getProperty("cakesFile");
        String ordersFile = Settings.getProperty("ordersFile");
        /*FilteredFileRepository<Cake, Integer> cakeRepository;
        FilteredFileRepository<Order, Integer> orderRepository;*/
        GenericRepository<Cake,Integer> cakeRepository;
        GenericRepository<Order,Integer> orderRepository;


        ///Generating the repositories based on the settings.properties file
        if ("text".equalsIgnoreCase(cakeRepositoryType) || "binary".equalsIgnoreCase(cakeRepositoryType)) {
            cakeRepository = new FilteredFileRepository<>(cakesFile); // File-based repository

        } else if ("memory".equalsIgnoreCase(cakeRepositoryType)) {
            cakeRepository = new FilteredFileRepository<>(); // In-memory repository
        }else if("database".equalsIgnoreCase(cakeRepositoryType)){

            Connection connection = DriverManager.getConnection("jdbc:sqlite:identifier.sqlite");
            cakeRepository=new CakeDatabaseRepository(connection);
        }
        else {
            throw new IllegalArgumentException("Unknown repository type specified in settings.properties");
        }
        this.cakeService = new CakeService(cakeRepository);

        if ("text".equalsIgnoreCase(cakeRepositoryType) || "binary".equalsIgnoreCase(cakeRepositoryType)) {
            orderRepository = new FilteredFileRepository<>(ordersFile); // File-based repository

        } else if ("memory".equalsIgnoreCase(cakeRepositoryType)) {
            orderRepository = new FilteredFileRepository<>(); // In-memory repository
        }else if("database".equalsIgnoreCase(cakeRepositoryType)){

            Connection connection = DriverManager.getConnection("jdbc:sqlite:identifier.sqlite");
            orderRepository=new OrderDatabaseRepository(connection);
        }
        else {
            throw new IllegalArgumentException("Unknown repository type specified in settings.properties");
        }
        this.orderService = new OrderService(orderRepository);
    }

    public void start() {

        while (true) {
            System.out.println("\nCake Management System");
            System.out.println("1. View all cakes");
            System.out.println("2. Add new cake");
            System.out.println("3. Update existing cake");
            System.out.println("4. Delete a cake");
            System.out.println("5. Filter cakes by flavor");
            System.out.println("6. Filter cakes by price range");
            System.out.println("7. View all orders");
            System.out.println("8. Add new order");
            System.out.println("9. Update existing order");
            System.out.println("10. Delete an order");
            System.out.println("11. Find Cake by ID");
            System.out.println("12.Feature 1: Get average cake price");
            System.out.println("13.Feature 2: What is the most used flavour?");
            System.out.println("14.Feature 3:Status for a given order");
            System.out.println("15.Feature 4:Get Cake Price By ID");
            System.out.println("17. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewAllCakes();
                case 2 -> addNewCake();
                case 3 -> updateCake();
                case 4 -> deleteCake();
                case 5 -> filterCakesByFlavor();
                case 6 -> filterCakesByPriceRange();
                case 7 -> viewAllOrders();
                case 8 -> addNewOrder();
                case 9 -> updateOrder();
                case 10 -> deleteOrder();
                case 11 -> findCakeByID();
                case 12 ->getAverageCakePrice();
                case 13 -> getMostUsedFlavor();
                case 14 -> getOrderStatus();
                case 15 -> getCakePriceById();
                case 17 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void findCakeByID(){
        System.out.println("Enter the ID of the cake you wanna find:");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.println(cakeService.getCakeById(id));
    }
    private void viewAllCakes() {
        System.out.println("\nList of Cakes:");
        cakeService.getAllCakes().forEach(System.out::println); // Using forEach with method reference
    }
    private void addNewCake() {
        System.out.println("\nEnter ID:");
        int id=scanner.nextInt();
        scanner.nextLine();
        System.out.println("\nEnter Name:");
        String name=scanner.nextLine();
        System.out.println("\nEnter Price:");
        int price=scanner.nextInt();
        scanner.nextLine();
        System.out.println("\nEnter Flavour:");
        String flavour=scanner.nextLine();
        System.out.println("\nEnter Size:");
        int size=scanner.nextInt();
        scanner.nextLine();
        cakeService.addCake(new Cake(id, name, price, flavour, size));
    }
    private void updateCake() {
        System.out.println("\nID of Cake to be updated:");
        int id=scanner.nextInt();
        scanner.nextLine();
        System.out.println("\nEnter Name:");
        String name=scanner.nextLine();
        System.out.println("\nEnter Price:");
        int price=scanner.nextInt();
        scanner.nextLine();
        System.out.println("\nEnter Flavour:");
        String flavour=scanner.nextLine();
        System.out.println("\nEnter Size:");
        int size=scanner.nextInt();
        scanner.nextLine();
        cakeService.updateCake(new Cake(id, name, price, flavour, price));
    }
    private void deleteCake() {
        System.out.println("\nID of Cake to be deleted:");
        int id=scanner.nextInt();
        scanner.nextLine();
        cakeService.deleteCake(id);
        orderService.deleteCakeFromAllOrders(id);
    }
    private void filterCakesByFlavor() {
        System.out.print("Enter flavor to filter by: ");
        String flavor = scanner.nextLine();
        cakeService.filterCakesByFlavour(flavor);
    }
    private void filterCakesByPriceRange() {
        System.out.print("Enter minimum price: ");
        double minPrice = scanner.nextDouble();
        System.out.print("Enter maximum price: ");
        double maxPrice = scanner.nextDouble();
        cakeService.filterCakesByPrice(minPrice, maxPrice);
    }
    private void viewAllOrders() {
        System.out.println("\nList of Orders:");
        orderService.getAllOrders().forEach(System.out::println);
    }
    private void addNewOrder() {
        List<Integer> list_of_cakes = new ArrayList<>();
        System.out.println("Enter ID:");
        int id=scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter Customer Name:");
        String customer_name=scanner.nextLine();
        System.out.println("Enter Status:");
        String status=scanner.nextLine();
        System.out.println("Enter Number of Cakes:");
        int nr_of_cakes=scanner.nextInt();
        scanner.nextLine();
        System.out.println("\nEnter, one by one, the ID's of the "+nr_of_cakes+ " cakes in the order:");
        for(int i=1;i<=nr_of_cakes;i++){
            int cake_id=scanner.nextInt();
            scanner.nextLine();
            list_of_cakes.add(cake_id);
        }
        orderService.addOrder(new Order(id, customer_name, status, list_of_cakes));
    }
    private void updateOrder() {
        List<Integer> list_of_cakes = new ArrayList<>();
        System.out.println("Enter ID:");
        int id=scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter Customer Name:");
        String customer_name=scanner.nextLine();
        System.out.println("Enter Status:");
        String status=scanner.nextLine();
        System.out.println("Enter Number of Cakes:");
        int nr_of_cakes=scanner.nextInt();
        scanner.nextLine();
        for(int i=1;i<=nr_of_cakes;i++){
            int cake_id=scanner.nextInt();
            scanner.nextLine();
            list_of_cakes.add(cake_id);
        }
        orderService.updateOrder(new Order(id, customer_name, status, list_of_cakes));
    }
    private void deleteOrder() {
        System.out.println("\nID of Order to be deleted:");
        int id=scanner.nextInt();
        scanner.nextLine();
        orderService.deleteOrder(id);
    }
    private void getOrderStatus() {
        System.out.print("Enter the order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine();
        String status = orderService.getOrderStatus(orderId);
        System.out.println("Order status: " + status);
    }
    private void getAverageCakePrice() {
        double averagePrice = cakeService.getAverageCakePrice();
        System.out.println("The average price of all cakes is: " + averagePrice);
    }
    private void getMostUsedFlavor() {
        String mostUsedFlavor = cakeService.getMostUsedFlavor();
        System.out.println("The most used flavor is: " + mostUsedFlavor);
    }
    private void getCakePriceById() {
        System.out.print("Enter the cake ID: ");
        int cakeId = scanner.nextInt();
        scanner.nextLine();
        try {
            double price = cakeService.getCakePrice(cakeId);
            System.out.println("The price of the cake with ID " + cakeId + " is: " + price);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
