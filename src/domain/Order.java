package domain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Order implements Identifiable<Integer>, Serializable {

    private int id;
    private String customerName;
    private String status;
    private List<Integer> cakesOrdered;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Integer> getCakesOrdered() {
        return cakesOrdered;
    }

    public void setCakesOrdered(List<Integer> cakesOrdered) {
        this.cakesOrdered = cakesOrdered;
    }

    /**
     * Returns a comma-separated string of cake IDs in the order.
     *
     * @return a string of cake IDs, e.g., "1, 2, 3"
     */
    public String getCakesID() {
        return cakesOrdered.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", status='" + status + '\'' +
                ", cakesOrdered=" + cakesOrdered +
                '}';
    }

    public Order(int id, String customerName, String status, List<Integer> cakesOrdered) {
        this.id = id;
        this.customerName = customerName;
        this.status = status;
        this.cakesOrdered = cakesOrdered;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id &&
                Objects.equals(customerName, order.customerName) &&
                Objects.equals(status, order.status) &&
                Objects.equals(cakesOrdered, order.cakesOrdered);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerName, status, cakesOrdered);
    }
}
