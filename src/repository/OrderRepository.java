package repository;

import domain.Cake;
import domain.Order;
import filters.AbstractFilter;
import repository.FilteredFileRepository;

import java.util.List;

public class OrderRepository extends FilteredFileRepository<Order, Integer> {

    public OrderRepository() {
        super();
    }

    public OrderRepository(String filePath) {
        super(filePath);
    }


}