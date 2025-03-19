package filters;

import domain.Cake;

import java.util.List;
import java.util.stream.Collectors;

public class CakePriceFilter implements AbstractFilter<Cake> {
    private double minPrice;
    private double maxPrice;

    public CakePriceFilter(double minPrice, double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    @Override
    public List<Cake> applyFilter(List<Cake> cakes) {
        return cakes.stream().filter(cake -> cake.getPrice() >= minPrice && cake.getPrice() <= maxPrice).collect(Collectors.toList());
    }

    @Override
    public boolean matches(Cake entity) {
        return false;
    }
}