package service;

import actions.IAction;
import domain.Cake;
import domain.Order;
import exceptions.InvalidDataException;
import exceptions.NotFoundException;
import filters.AbstractFilter;
import filters.CakeFlavorFilter;
import filters.CakePriceFilter;
import repository.GenericRepository;

import java.util.*;
import java.util.stream.Collectors;

public class CakeService extends ActionService {

    private final GenericRepository<Cake, Integer> cakeRepository;

    public CakeService(GenericRepository<Cake, Integer> cakeRepository) {
        this.cakeRepository = cakeRepository;
    }

    public List<Cake> getAllCakes() {
        return cakeRepository.findAll();
    }

    public Optional<Cake> getCakeById(Integer id) {
        return cakeRepository.findById(id)
                .or(() -> {
                    System.err.println("Cake not found");
                    return Optional.empty();
                });
    }

    public void addCake(Cake cake) {
        try {
            if (cakeRepository.findById(cake.getId()).isPresent()) {
                throw new NotFoundException("There already exists a cake with this Identifier");
            }
            if (cake.getName() == null || cake.getFlavor() == null) {
                throw new InvalidDataException("Cake name and flavor must be valid strings");
            }
            cakeRepository.save(cake);

            // Log the action for undo/redo
            undoStack.push(new IAction() {
                @Override
                public void executeUndo() {
                    cakeRepository.delete(cake.getId());
                }

                @Override
                public void executeRedo() {
                    cakeRepository.save(cake);
                }
            });
            redoStack.clear();

        } catch (InvalidDataException | NotFoundException e) {
            System.err.println("Error adding cake: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    public void updateCake(Cake cake) {
        Optional<Cake> oldCake = cakeRepository.findById(cake.getId());
        cakeRepository.update(cake);

        // Log the action for undo/redo
        oldCake.ifPresent(previous -> {
            undoStack.push(new IAction() {
                @Override
                public void executeUndo() {
                    cakeRepository.update(previous);
                }

                @Override
                public void executeRedo() {
                    cakeRepository.update(cake);
                }
            });
            redoStack.clear();
        });
    }

    public void deleteCake(Integer id) {
        Optional<Cake> oldCake = cakeRepository.findById(id);
        if (oldCake.isEmpty()) {
            System.err.println("Cake not found");
            return;
        }

        cakeRepository.delete(id);

        // Log the action for undo/redo
        oldCake.ifPresent(deletedCake -> {
            undoStack.push(new IAction() {
                @Override
                public void executeUndo() {
                    cakeRepository.save(deletedCake);
                }

                @Override
                public void executeRedo() {
                    cakeRepository.delete(id);
                }
            });
            redoStack.clear();
        });
    }

    public List<Cake> getCakesByFilter(AbstractFilter<Cake> filter) {
        return cakeRepository.findWithFilter(filter);
    }

    public List<Cake> filterCakesByFlavour(String flavour) {
        CakeFlavorFilter filter = new CakeFlavorFilter(flavour);
        return cakeRepository.findWithFilter(filter);
    }

    public List<Cake> filterCakesByPrice(double min, double max) {
        CakePriceFilter filter = new CakePriceFilter(min, max);
        return cakeRepository.findWithFilter(filter);
    }

    public List<Cake> getCakesOrderedByCustomer(String customerName, GenericRepository<Order, Integer> orderRepository) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getCustomerName().equalsIgnoreCase(customerName))
                .toList();

        List<Integer> cakeIds = orders.stream()
                .flatMap(order -> order.getCakesOrdered().stream())
                .distinct()
                .toList();

        return cakeIds.stream()
                .map(cakeId -> cakeRepository.findById(cakeId).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    public double getAverageCakePrice() {
        List<Cake> cakes = cakeRepository.findAll();
        return cakes.stream().mapToDouble(Cake::getPrice).average().orElse(0.0);
    }

    public String getMostUsedFlavor() {
        List<Cake> cakes = cakeRepository.findAll();

        return cakes.stream()
                .collect(Collectors.groupingBy(Cake::getFlavor, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No cakes available");
    }

    public double getCakePrice(Integer cakeId) {
        return cakeRepository.findById(cakeId)
                .map(Cake::getPrice)
                .orElseThrow(() -> new IllegalArgumentException("Cake not found"));
    }
}
