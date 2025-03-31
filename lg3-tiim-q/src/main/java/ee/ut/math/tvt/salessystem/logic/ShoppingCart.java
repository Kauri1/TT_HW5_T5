package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.HistoryEntry;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.service.spi.ServiceException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private static final Logger log = LogManager.getLogger(ShoppingCart.class);
    private final SalesSystemDAO dao;
    private EntityManagerFactory emf;
    private EntityManager em;
    private final List<SoldItem> items = new ArrayList<>();

    public ShoppingCart(SalesSystemDAO dao) {
        this.dao = dao;
    }

    /**
     * Add new SoldItem to table.
     */
    public void addItem(StockItem item, int amount) {

        for (SoldItem soldItem : items) {
            if (soldItem.getId().equals(item.getId())) {
                int newQuantity = soldItem.getQuantity() + amount;
                if (item.getQuantity() < newQuantity) {
                    throw new SalesSystemException("Max quantity exceeded. Available quantity: " + (item.getQuantity()-soldItem.getQuantity()) + ", Requested: " + amount);
                }
                soldItem.setQuantity(newQuantity);
                if (soldItem.getQuantity() < 1){
                    items.remove(soldItem);
                }
                return;
            }
        }
        if (item.getQuantity() - amount < 0){
            throw new SalesSystemException("Max quantity exceeded. Available quantity: " + item.getQuantity() + ", Requested: " + amount);
        }

        if (amount == 0){
            throw new SalesSystemException("Cant add item with quantity 0");
        }

        if (amount < 0){
            throw new SalesSystemException("Cant add item with negative quantity");
        }

        // Should not need the Math.min, because throws error
        items.add(new SoldItem(item, Math.min(amount, item.getQuantity()), LocalDateTime.MIN));

        log.debug("Added " + item.getName() + ", amount: " + item.getQuantity());
    }

    public List<SoldItem> getAll() {
        return items;
    }

    public void cancelCurrentPurchase() {
        items.clear();
    }

    public void submitCurrentPurchase() {
        // note the use of transactions. InMemorySalesSystemDAO ignores transactions
        // but when you start using hibernate in lab5, then it will become relevant.
        // what is a transaction? https://stackoverflow.com/q/974596
        dao.beginTransaction();


        LocalDateTime timeOfPurchase = LocalDateTime.now();
        HistoryEntry newHistoryEntry = new HistoryEntry(timeOfPurchase, items);

        try {
            dao.saveHistoryEntry(newHistoryEntry);
            Long lastHistoryEntryId = dao.getLastHistoryEntryId();

            for (SoldItem item : items) {
                item.setTimeSold(timeOfPurchase);
                dao.saveSoldItem(item, lastHistoryEntryId);
                item.getStockItem().decreaseQuantity(item.getQuantity());
//                if (item.getStockItem().getQuantity() == 0){
//                    dao.removeStockItem(item.getStockItem());
//                }
            }
            dao.commitTransaction();
            items.clear();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw e;
        }
    }
}
