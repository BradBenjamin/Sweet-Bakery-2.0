package service;

import actions.IAction;
import domain.Order;
import repository.GenericRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderService extends ActionService {

    private final GenericRepository<Order, Integer> orderRepository;

    public OrderService(GenericRepository<Order, Integer> orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    public void addOrder(Order order) {
        orderRepository.save(order);

        // Log the action for undo/redo
        undoStack.push(new IAction() {
            @Override
            public void executeUndo() {
                orderRepository.delete(order.getId());
            }

            @Override
            public void executeRedo() {
                orderRepository.save(order);
            }
        });
        redoStack.clear();
    }

    public void updateOrder(Order order) {
        Optional<Order> oldOrder = orderRepository.findById(order.getId());
        orderRepository.update(order);

        oldOrder.ifPresent(previous -> {
            undoStack.push(new IAction() {
                @Override
                public void executeUndo() {
                    orderRepository.update(previous);
                }

                @Override
                public void executeRedo() {
                    orderRepository.update(order);
                }
            });
            redoStack.clear();
        });
    }

    public void deleteOrder(Integer id) {
        Optional<Order> oldOrder = orderRepository.findById(id);
        orderRepository.delete(id);

        oldOrder.ifPresent(deletedOrder -> {
            undoStack.push(new IAction() {
                @Override
                public void executeUndo() {
                    orderRepository.save(deletedOrder);
                }

                @Override
                public void executeRedo() {
                    orderRepository.delete(id);
                }
            });
            redoStack.clear();
        });
    }

    public void deleteCakeFromAllOrders(Integer cakeId) {
        for (Order order : orderRepository.findAll()) {
            List<Integer> cakesList = order.getCakesOrdered();
            if (!cakesList.contains(cakeId)) continue;

            List<Integer> previousList = new ArrayList<>(cakesList);
            cakesList.removeIf(n -> n.equals(cakeId));
            order.setCakesOrdered(cakesList);
            orderRepository.update(order);

            undoStack.push(new IAction() {
                @Override
                public void executeUndo() {
                    order.setCakesOrdered(previousList);
                    orderRepository.update(order);
                }

                @Override
                public void executeRedo() {
                    order.setCakesOrdered(cakesList);
                    orderRepository.update(order);
                }
            });
            redoStack.clear();
        }
    }

    public String getOrderStatus(Integer orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getStatus)
                .orElse("Order not found");
    }
}
