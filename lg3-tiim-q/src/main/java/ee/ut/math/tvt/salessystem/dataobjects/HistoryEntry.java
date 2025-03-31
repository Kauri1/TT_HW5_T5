package ee.ut.math.tvt.salessystem.dataobjects;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "HISTORYENTRIES")
public class HistoryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "time_sold")
    private LocalDateTime timeSold;
    @OneToMany(mappedBy = "historyEntry")
    private List<SoldItem> soldItemList;
    @Column(name = "total_price")
    private double totalPrice;

    public HistoryEntry(LocalDateTime timeSold, List<SoldItem> soldItemList) {
        this.timeSold = timeSold;
        this.soldItemList = soldItemList;
    }

    public HistoryEntry() {

    }

    public LocalDateTime getDateTimeSold() {
        return timeSold;
    }

    public LocalDate getDateSold() {
        return timeSold.toLocalDate();
    }
    public LocalTime getTimeSold() {
        return timeSold.toLocalTime();
    }

    public List<SoldItem> getSoldItemList() {
        return soldItemList;
    }

    public double getTotalPrice() {
        totalPrice = soldItemList.stream().mapToDouble(SoldItem::getPriceXquantity).sum();
        return totalPrice;
    }

    public Long getId() {
        return id;
    }


    @Override
    public String toString() {
        return "HistoryEntry, sold: " + timeSold + ", " + soldItemList + ", total: " + getTotalPrice();
    }
}
