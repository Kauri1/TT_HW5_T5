package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dataobjects.HistoryEntry;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class InMemorySalesSystemDAO implements SalesSystemDAO {

    private static final Logger log = LogManager.getLogger(InMemorySalesSystemDAO.class);

    private final List<StockItem> stockItemList;
    private final List<SoldItem> soldItemList;
    private List<HistoryEntry> historyEntries;

    public InMemorySalesSystemDAO() {
        List<StockItem> items = new ArrayList<>();
        items.add(new StockItem(1L, "Lays chips", "Potato chips", 11.0, 5));
        items.add(new StockItem(2L, "Chupa-chups", "Sweets", 8.0, 8));
        items.add(new StockItem(3L, "Frankfurters", "Beer sauseges", 15.0, 12));
        items.add(new StockItem(4L, "Free Beer", "Student's delight", 0.0, 100));
        items.add(new StockItem(5L, "Gingerbread", "All I want for Christmas", 3.3, 33));
        this.stockItemList = items;

        this.soldItemList = new ArrayList<>();

        //addDummies(); // for testing
    }

    @Override
    public List<StockItem> findStockItems() {
        return stockItemList;
    }

    @Override
    public List<StockItem> findItemsLeftInStock(){
        return stockItemList.stream().filter(item -> item.getQuantity()>0).collect(Collectors.toList());
    }

    @Override
    public StockItem findStockItem(long id) {
        for (StockItem item : stockItemList) {
            if (item.getId() == id)
                return item;
        }
        return null;
    }

    @Override
    public StockItem findStockItem(String name) {
        for (StockItem item : stockItemList) {
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return null;
    }

    public List<SoldItem> findSoldItems(){
        soldItemList.sort(Comparator.comparing(SoldItem::getDateTimeSold));
        return soldItemList.reversed();
    }

    @Override
    public long lastID(){
        return stockItemList.getLast().getId();
    }

    private ArrayList<HistoryEntry> fillHistoryEntries(){
        HashMap<LocalDateTime, HistoryEntry> filler = new HashMap<>();

        for (SoldItem soldItem : soldItemList) {
            LocalDateTime timeSold = soldItem.getDateTimeSold();

            if (filler.isEmpty() || !filler.containsKey(timeSold)){
                ArrayList<SoldItem> soldItems = new ArrayList<>();
                soldItems.add(soldItem);
                filler.put(timeSold, (new HistoryEntry(timeSold, soldItems)));
            }
            else {
                filler.get(timeSold).getSoldItemList().add(soldItem);
            }
        }
        return new ArrayList<>(filler.values().stream().sorted(Comparator.comparing(HistoryEntry::getDateTimeSold)).toList().reversed());
    }

    private void addDummies() {
        soldItemList.add(new SoldItem(new StockItem(99L, "Test item 1", "Test description", 99.9, 99), 99, LocalDateTime.MIN));
        soldItemList.add(new SoldItem(new StockItem(100L, "Test item 2", "Test description", 100.0, 9), 10, LocalDateTime.MAX));
        soldItemList.add(new SoldItem(new StockItem(101L, "Test item 3", "Test description", 101.1, 90), 1, LocalDateTime.MIN));
        this.historyEntries = fillHistoryEntries();
    }

    @Override
    public List<HistoryEntry> findHistoryEntries(){
        historyEntries = fillHistoryEntries(); // refreshes the field
        return historyEntries;
    }


    @Override
    public void saveSoldItem(SoldItem item, Long lastHistoryEntryId) {
        soldItemList.add(item);
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
        stockItemList.add(stockItem);
    }

    @Override
    public void removeStockItem(StockItem item){
        stockItemList.remove(item);
    }


    @Override
    public void beginTransaction() {
    }

    @Override
    public void rollbackTransaction() {
    }

    @Override
    public void commitTransaction() {
    }

    @Override
    public List<HistoryEntry> getNentries(int n){
        List<HistoryEntry> entries = findHistoryEntries();

        if (n==-1) { // n=-1 means that all entries are requested
            return entries;
        } else if (n<0) {   // shouldn't actually be needed as we only call the method with n=10 and n=-1, but just in case
            return new ArrayList<>();
        } else {
            return entries.subList(0,Math.min(n,entries.size()));
        }
    }

    @Override
    public List<HistoryEntry> getEntriesByDate(LocalDate start, LocalDate end) {
        return findHistoryEntries()
                .stream()
                .filter(e -> {
                    LocalDate dateSold = e.getDateSold();
                    return ((dateSold.isAfter(start) || dateSold.isEqual(start))
                            && (dateSold.isBefore(end) || dateSold.isEqual(end)));})
                .collect(Collectors.toList());
    }

    @Override
    public void saveHistoryEntry(HistoryEntry historyEntry) {

    }

    @Override
    public Long getLastHistoryEntryId() {
        return 0L;
    }
}
