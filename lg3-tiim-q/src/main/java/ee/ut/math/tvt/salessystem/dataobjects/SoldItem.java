package ee.ut.math.tvt.salessystem.dataobjects;


import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Already bought StockItem. SoldItem duplicates name and price for preserving history.
 */
@Entity
@Table(name = "SOLDITEMS")
public class SoldItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "STOCKITEM_ID", nullable = false)
    private StockItem stockItem;
    @Column(name = "name")
    private String name;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "price")
    private double price;
    @Column(name = "time_sold")
    private LocalDateTime timeSold;
    @ManyToOne
    @JoinColumn(name = "HISTORY_ENTRY_ID", nullable = false)
    private HistoryEntry historyEntry;

    public SoldItem() {
    }

    public SoldItem(StockItem stockItem, int quantity, LocalDateTime timeSold) {
        this.stockItem = stockItem;
        this.id = stockItem.getId();
        this.name = stockItem.getName();
        this.price = stockItem.getPrice();
        this.quantity = quantity;
        this.timeSold = timeSold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public LocalDateTime getDateTimeSold(){return timeSold;}

    public LocalDate getDateSold() {
        return timeSold.toLocalDate();
    }

    public LocalTime getTimeSold() {
        return timeSold.toLocalTime();
    }

    public void setTimeSold(LocalDateTime timeSold) {
        this.timeSold = timeSold;
    }

    public double getSum() {
        return price * ((double) quantity);
    }

    public StockItem getStockItem() {
        return stockItem;
    }

    public void setStockItem(StockItem stockItem) {
        this.stockItem = stockItem;
    }

    public double getPriceXquantity() {
        return price*quantity;
    }
    @Override
    public String toString() {
        return String.format("SoldItem{id=%d, name='%s'}", id, name);
    }
}
