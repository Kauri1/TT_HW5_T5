package ee.ut.math.tvt.salessystem;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.WarehouseManager;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ShoppingCartTest {
    private ShoppingCart shoppingCart;
    private InMemorySalesSystemDAO dao;

    @BeforeEach
    public void setUp(){
        dao = spy(new InMemorySalesSystemDAO());
        shoppingCart = new ShoppingCart(dao);
        dao.saveStockItem(new StockItem(0L, "item1", "desc", 20.4, 20));
        dao.saveStockItem(new StockItem(0L,"item2", "desc", 2, 10));
    }

    @Test
    public void testSubmittingShoppingCartBeginsAndCommitsTransaction(){
        StockItem item = dao.findStockItem("item1");
        System.out.println(dao.findStockItems());
        StockItem item2 = dao.findStockItem("item2");
        shoppingCart.addItem(item, 3);
        shoppingCart.addItem(item2, 5);

        shoppingCart.submitCurrentPurchase();

        // Verify the transaction methods were called in correct order
        InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginTransaction();
        inOrder.verify(dao).commitTransaction();

        // Verify each method was called exactly once (once at setup and once when submitting purchase)
        verify(dao, times(1)).beginTransaction();
        verify(dao, times(1)).commitTransaction();
    }

    @Test
    public void testAddingNewItem(){
        StockItem item = dao.findStockItem("item1");
        shoppingCart.addItem(item, 2);
        assertSame(shoppingCart.getAll().getFirst().getStockItem(), item);
    }

    @Test
    public void testIncreasingQuantity(){
        StockItem item = dao.findStockItem("item1");
        shoppingCart.addItem(item, 2);
        shoppingCart.addItem(item, 3);
        assertEquals(5, shoppingCart.getAll().getFirst().getQuantity());
    }

    @Test
    public void testReducingQuantity(){
        StockItem item = dao.findStockItem("item1");
        shoppingCart.addItem(item, 5);
        shoppingCart.addItem(item, -3);
        assertEquals(2, shoppingCart.getAll().getFirst().getQuantity());
    }

    @Test
    public void testCompletelyRemovingItemsFromShoppingcart(){
        StockItem item = dao.findStockItem("item1");
        shoppingCart.addItem(item, 5);
        shoppingCart.addItem(item, -5);
        assertEquals(0, shoppingCart.getAll().size());
        shoppingCart.addItem(item, 5);
        shoppingCart.addItem(item, -50000);
        assertEquals(0, shoppingCart.getAll().size());
    }

    @Test
    public void testBuyingOutAnItem(){
        StockItem item = dao.findStockItem("item1");
        shoppingCart.addItem(item, 20);
        shoppingCart.submitCurrentPurchase();
        assertEquals(0, dao.findStockItem("item1").getQuantity());
    }

    @Test
    public void testMaxQuantityExceeded(){
        StockItem item = dao.findStockItem("item1");
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(item, 500000));
    }

    @Test
    public void testQuantitySumTooLarge(){
        StockItem item = dao.findStockItem("item1");
        shoppingCart.addItem(item, 10);
        shoppingCart.addItem(item, 10);
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(item, 10));
    }

    @Test
    public void testAddingNewItemWithNegativeQuantity(){
        StockItem item = dao.findStockItem("item1");
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(item, -1));
    }

    @Test
    public void testAddingItemWithQuantity0(){
        StockItem item = dao.findStockItem("item1");
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(item, 0));
    }

    @Test
    public void testSubmittingCurrentOrderCreatesHistoryItem(){
        StockItem item = dao.findStockItem("item1");
        shoppingCart.addItem(item, 1);
        shoppingCart.submitCurrentPurchase();
        assertEquals(dao.findHistoryEntries().getFirst().getSoldItemList().getFirst().getStockItem(), item);
    }

    @Test
    public void testSubmittingCurrentOrderSavesCorrectTime(){
        StockItem item = dao.findStockItem("item1");
        shoppingCart.addItem(item, 1);
        shoppingCart.submitCurrentPurchase();
        //assertEquals(dao.findHistoryEntries().getFirst().getTimeSold(), , 10);
        assertTrue(Duration.between(LocalTime.now(), dao.findHistoryEntries().getFirst().getTimeSold()).getSeconds() < 10);
    }

    @Test
    public void testCancellingOrder(){
        StockItem item = dao.findStockItem("item1");
        int quantity1 = item.getQuantity();
        shoppingCart.addItem(item, 1);
        shoppingCart.cancelCurrentPurchase();
        assertEquals(0, shoppingCart.getAll().size());
        assertEquals(quantity1, dao.findStockItem("item1").getQuantity());

        item = dao.findStockItem("item2");
        shoppingCart.addItem(item, 5);
        shoppingCart.submitCurrentPurchase();
        assertEquals(5, dao.findStockItem("item2").getQuantity());
        assertEquals(20, dao.findStockItem("item1").getQuantity());
    }

    @Test
    public void testSubmittingOrderRemovesStockFromWarehouse(){
        StockItem item = dao.findStockItem("item2");
        shoppingCart.addItem(item, 5);
        shoppingCart.submitCurrentPurchase();
        assertEquals(5, dao.findStockItem("item2").getQuantity());
        assertEquals(20, dao.findStockItem("item1").getQuantity());
    }


}
