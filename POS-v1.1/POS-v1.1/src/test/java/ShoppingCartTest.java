import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
//import ee.ut.math.tvt.salessystem.logic.WarehouseManager;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//import org.mockito.InOrder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ShoppingCartTest {
    private ShoppingCart shoppingCart;
    private InMemorySalesSystemDAO dao;

    @BeforeEach
    public void setUp(){
        dao = new InMemorySalesSystemDAO();
        shoppingCart = new ShoppingCart(dao);
        dao.saveStockItem(new StockItem(1L, "item1", "desc", 20.4, 20));
        dao.saveStockItem(new StockItem(2L,"item2", "desc", 2, 10));
    }


    @Test
    public void testAddingNewItem(){
        StockItem itm = dao.findStockItem(1);
        SoldItem item = new SoldItem(itm, (2));
        shoppingCart.addItem(item);
        assertSame(shoppingCart.getAll().get(0).getStockItem(), item);
    }

    @Test
    public void testIncreasingQuantity(){
        StockItem itm = dao.findStockItem(1);
        SoldItem item = new SoldItem(itm, (1));

        shoppingCart.addItem(item);
        shoppingCart.addItem(item);
        assertEquals(2, shoppingCart.getAll().get(0).getQuantity());
    }

    @Test
    public void testReducingQuantity(){
        StockItem itm = dao.findStockItem(1);
        SoldItem item = new SoldItem(itm, (5));
        SoldItem item2 = new SoldItem(itm, (-3));
        shoppingCart.addItem(item, 5);
        shoppingCart.addItem(item2, -3);
        assertEquals(2, shoppingCart.getAll().get(0).getQuantity());
    }

    @Test
    public void testCompletelyRemovingItemsFromShoppingcart(){
        StockItem itm = dao.findStockItem(1);
        SoldItem item = new SoldItem(itm, (5));
        SoldItem item2 = new SoldItem(itm, (-5));
        shoppingCart.addItem(item, 5);
        shoppingCart.addItem(item2, -5);
        assertEquals(0, shoppingCart.getAll().size());
        shoppingCart.addItem(new SoldItem(itm, (5)), 5);
        shoppingCart.addItem(new SoldItem(itm, (-50000)), -50000);
        assertEquals(0, shoppingCart.getAll().size());
    }

    @Test
    public void testBuyingOutAnItem(){
        StockItem itm = dao.findStockItem(1);
        shoppingCart.addItem(new SoldItem(itm, (20)), 20);
        shoppingCart.submitCurrentPurchase();
        assertEquals(0, dao.findStockItem(1).getQuantity());
    }


    @Test
    public void testCancellingOrder(){
        StockItem itm = dao.findStockItem(1);
        int quantity1 = itm.getQuantity();
        shoppingCart.addItem(new SoldItem(itm, (1)));
        shoppingCart.cancelCurrentPurchase();
        assertEquals(0, shoppingCart.getAll().size());
        assertEquals(quantity1, dao.findStockItem(1).getQuantity());

        itm = dao.findStockItem(2);
        shoppingCart.addItem(new SoldItem(itm, (5)), 5);
        shoppingCart.submitCurrentPurchase();
        assertEquals(5, dao.findStockItem(2).getQuantity());
        assertEquals(20, dao.findStockItem(1).getQuantity());
    }

    @Test
    public void testSubmittingOrderRemovesStockFromWarehouse(){
        StockItem itm = dao.findStockItem(2);
        shoppingCart.addItem(new SoldItem(itm, (5)), 5);
        shoppingCart.submitCurrentPurchase();
        assertEquals(5, dao.findStockItem(2).getQuantity());
        assertEquals(20, dao.findStockItem(1).getQuantity());
    }


}
