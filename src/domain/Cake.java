package domain;

import java.io.Serializable;
import java.util.Objects;

public class Cake implements Identifiable<Integer>, Serializable {
    private Integer id;
    private String name;
    private double price;
    private String flavor;
    private int size;

    public Cake(Integer id, String name, double price, String flavor, int size) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.flavor = flavor;
        this.size = size;
    }


    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getFlavor() {
        return flavor;
    }

    public int getSize() {
        return size;
    }
    public void updatePrice(double percentage) {
        if ("Vanilla".equalsIgnoreCase(flavor)) {
            this.price += this.price * (percentage / 100);
        }
    }

    @Override
    public String toString() {
        return "Cake{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", flavor='" + flavor + '\'' +
                ", size=" + size +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cake cake = (Cake) o;
        return id == cake.id &&
                Double.compare(cake.price, price) == 0 &&
                size == cake.size &&
                Objects.equals(name, cake.name) &&
                Objects.equals(flavor, cake.flavor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, flavor, size);
    }
}