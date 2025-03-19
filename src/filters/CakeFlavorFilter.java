package filters;

import domain.Cake;

import java.util.List;
import java.util.stream.Collectors;

public class CakeFlavorFilter implements AbstractFilter<Cake> {
    private String flavor;

    public CakeFlavorFilter(String flavor) {
        this.flavor = flavor;
    }

    @Override
    public List<Cake> applyFilter(List<Cake> cakes) {
        return cakes.stream().filter(cake -> cake.getFlavor().equals(flavor)).collect(Collectors.toList());
    }
    @Override
    public boolean matches(Cake cake) {
        return cake.getFlavor().equalsIgnoreCase(flavor);
    }
}
