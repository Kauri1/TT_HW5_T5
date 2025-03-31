package ee.ut.math.tvt.salessystem.dao;
import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dataobjects.HistoryEntry;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.service.spi.ServiceException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HibernateSalesSystemDAO implements SalesSystemDAO{
    private final Logger log = LogManager.getLogger(HibernateSalesSystemDAO.class);
    private EntityManagerFactory emf;
    private EntityManager em;
    public HibernateSalesSystemDAO () {
        // if you get ConnectException / JDBCConnectionException then you
        // probably forgot to start the database before starting the application
        try {
            emf = Persistence.createEntityManagerFactory("POS");
            em = emf.createEntityManager();
        }
        catch (ServiceException e) {
            log.error(e.getMessage());
            log.info("Could not connect to the database. Please check whether your database task is running.");
            throw new SalesSystemException(e.getMessage());
            //System.exit(1);
        }
    }
    public void close () {
        em.close ();
        emf.close ();
    }

    @Override
    public List<HistoryEntry> findHistoryEntries() {
        return em.createQuery("select h from HistoryEntry h", HistoryEntry.class).getResultList();
    }

    @Override
    public List<StockItem> findStockItems() {
        return em.createQuery("select c from StockItem c", StockItem.class).getResultList();
    }

    @Override
    public List<StockItem> findItemsLeftInStock() {
        return em.createQuery("select c from StockItem c where c.quantity>0", StockItem.class).getResultList();
    }

    @Override
    public List<SoldItem> findSoldItems() {
        return em.createQuery("select s from SoldItem s", SoldItem.class).getResultList();
    }

    @Override
    public StockItem findStockItem(long id) {
        return em.find(StockItem.class, id);
    }

    @Override
    public StockItem findStockItem(String name) {
        //System.out.println(name);
        //return em.find(StockItem.class, name);
        try {
            return em.createQuery("select s from StockItem s where LOWER(s.name) = lower(:name)", StockItem.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e){
            return null;
        }

    }

    @Override
    public void saveStockItem(StockItem stockItem) {
        em.merge(stockItem);
    }

    @Override
    public void saveSoldItem(SoldItem item, Long lastHistoryEntryId) {
        Long stockItemId = item.getStockItem().getId();
        String name = item.getName();
        int quantity = item.getQuantity();
        double price = item.getPrice();
        LocalDateTime timeSold = item.getDateTimeSold();
        String timeSoldString = timeSold.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        String insertSoldItemQuery = "INSERT INTO SOLDITEMS (stockitem_id, name, quantity, price, time_sold, history_entry_id) " +
                "VALUES (" + stockItemId + ", '" + name + "', " + quantity + ", " + price + ", '" + timeSoldString + "', " + lastHistoryEntryId + ")";



        Query soldItemQuery = em.createNativeQuery(insertSoldItemQuery);
        soldItemQuery.executeUpdate();

    }

    @Override
    public void saveHistoryEntry(HistoryEntry historyEntry) {
        LocalDateTime timeSold = historyEntry.getDateTimeSold();
        String timeSoldString = timeSold.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        double totalPrice = historyEntry.getTotalPrice();

        String insertHistoryEntryQuery = "INSERT INTO HISTORYENTRIES (time_sold, total_price) " +
                "VALUES ('" + timeSoldString + "', " + totalPrice + ")";
        Query historyEntryQuery = em.createNativeQuery(insertHistoryEntryQuery);
        historyEntryQuery.executeUpdate();

    }

    public Long getLastHistoryEntryId() {
        String queryString = "SELECT MAX(h.id) FROM HISTORYENTRIES h";
        Query query = em.createNativeQuery(queryString);
        return ((Number) query.getSingleResult()).longValue();
    }



    @Override
    public void beginTransaction () {
        em.getTransaction (). begin ();
    }
    @Override
    public void rollbackTransaction () {
        em.getTransaction (). rollback ();
    }
    @Override
    public void commitTransaction () {
        em.getTransaction (). commit ();
    }



    @Override
    public void removeStockItem(StockItem item) {
        em.remove(item);
    }

    @Override
    public long lastID() {
        return 0;
    }

    @Override
    public List<HistoryEntry> getEntriesByDate(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        String queryString = "SELECT h FROM HistoryEntry h WHERE h.timeSold BETWEEN :startDateTime AND :endDateTime ORDER BY h.timeSold DESC";
        TypedQuery<HistoryEntry> query = em.createQuery(queryString, HistoryEntry.class);
        query.setParameter("startDateTime", startDateTime);
        query.setParameter("endDateTime", endDateTime);

        return query.getResultList();
    }

    @Override
    public List<HistoryEntry> getNentries(int n) {
        List<HistoryEntry> entries;

        String queryString = "SELECT h FROM HistoryEntry h ORDER BY h.timeSold DESC";
        TypedQuery<HistoryEntry> query = em.createQuery(queryString, HistoryEntry.class);

        if (n > 0) {
            query.setMaxResults(n);
        }

        // If n == -1, return all entries
        entries = query.getResultList();

        return entries;
    }
}
