package ee.ut.math.tvt.salessystem;

import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import ee.ut.math.tvt.salessystem.logic.WarehouseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;


public class WarehouseTest {

    //private SalesSystemDAO dao;
    private InMemorySalesSystemDAO dao;
    private WarehouseManager wm;
    @BeforeEach
    public void setUp() {
        // set up dao
        dao = spy(new InMemorySalesSystemDAO());
        wm = new WarehouseManager(dao);

        StockItem item = new StockItem(0L, "name", "desc", 10, 10);
        dao.saveStockItem(item);
    }

    @Test
    public void testAddingNewItemBeginsAndCommitsTransaction(){
        // Call the actual method that should trigger the transaction
        wm.addItem("", "newItem", "description", "10", "10");

        // Verify the transaction methods were called in correct order
        InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginTransaction();
        inOrder.verify(dao).commitTransaction();

        // Verify each method was called exactly once
        verify(dao, times(1)).beginTransaction();
        verify(dao, times(1)).commitTransaction();
    }

    @Test
    public void testIncreasingQuantityBeginsAndCommitsTransaction(){
        // Call the actual method that should trigger the transaction
        wm.addItem("", "name", "", "", "10");

        // Verify the transaction methods were called in correct order
        InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginTransaction();
        inOrder.verify(dao).commitTransaction();
    }

    @Test
    public void testChangingPriceBeginsAndCommitsTransaction(){
        // Call the actual method that should trigger the transaction
        wm.addItem("", "name", "", "30", "0");

        // Verify the transaction methods were called in correct order
        InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginTransaction();
        inOrder.verify(dao).commitTransaction();
    }

    @Test
    public void testAddingNewItem(){
        wm.addItem("", "newItem", "description", "10", "10");
        assertEquals("newItem", dao.findStockItem("newItem").getName());
    }

    @Test
    public void testAddingItemWithNegativeQuantity(){
        assertThrows(SalesSystemException.class, () ->
                wm.addItem("1", "newItem", "description", "10", "-1"));
        assertSame(null, dao.findStockItem("newItem"));
    }

    @Test
    public void testAddingExistingItem(){
        wm.addItem("", "name", "desc", "10", "3");
        assertEquals(13, dao.findStockItem("name").getQuantity());
    }

    @Test
    public void testAddingExistingItemViaId(){
        wm.addItem(dao.findStockItem("name").getId().toString(), "", "desc", "10", "3");
        assertEquals(13, dao.findStockItem("name").getQuantity());
    }

    @Test
    public void testAddingExistingItemViaName(){
        wm.addItem("", "name", "desc", "10", "3");
        assertEquals(13, dao.findStockItem("name").getQuantity());
    }

    @Test
    public void testSetPrice(){
        wm.addItem("", "name", "desc", "30", "0");
        assertEquals(30, dao.findStockItem("name").getPrice());
    }
}
