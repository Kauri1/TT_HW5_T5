package ee.ut.math.tvt.salessystem.dataobjects;

import jakarta.persistence.*;

/**
 * Stock item.
 */
@Entity
@Table(name = "STOCKITEMS")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private double price;
    @Column(name = "description")
    private String description;
    @Column(name = "quantity")
    private int quantity;

    public StockItem() {
    }

    public StockItem(Long id, String name, String desc, double price, int quantity) {
        this.id = id;
        initialize(name, desc, price, quantity);
    }

    // This constructor is used, when database generates ids
    public StockItem(String name, String desc, double price, int quantity) {
        initialize(name, desc, price, quantity);
    }

    private void initialize(String name, String desc, double price, int quantity) {
        this.name = name;
        this.description = desc;
        this.price = price;
        if (price < 0){
            throw new IllegalArgumentException("Price has to be above or equal 0");
        }
        this.quantity = quantity;
        if (quantity < 0){
            throw new IllegalArgumentException("Quantity can't be nagative");
        }
    }


    public void increaseQuantity(int amount){
        int newQuantity = quantity + amount;
        if (newQuantity > -1){
            quantity = newQuantity;
        } else {
            throw new IllegalArgumentException("Quantity cannot go below 0");
        }
    }

    public void decreaseQuantity(int amount){
        quantity-=amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("StockItem{id=%d, name='%s'}", id, name);
    }
}
