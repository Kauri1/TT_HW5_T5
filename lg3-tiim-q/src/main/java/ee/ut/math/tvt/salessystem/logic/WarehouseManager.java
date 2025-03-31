package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class WarehouseManager {

    private final SalesSystemDAO dao;

    private static final Logger log = LogManager.getLogger(WarehouseManager.class);

    public WarehouseManager(SalesSystemDAO dao) {
        this.dao = dao;
    }


    public void addItem(String inId, String name, String desc, String inPrice, String inQuantity){
        long id = -1;
        if (!inId.isBlank())
            id = Long.parseLong(inId);

        double price = 0;
        if (!inPrice.isBlank())
                price = Double.parseDouble(inPrice);

        int quantity = 0;
        if (!inQuantity.isBlank())
                quantity = Integer.parseInt(inQuantity);

        StockItem item;

        StockItem itemByName = dao.findStockItem(name);
        StockItem itemById = dao.findStockItem(id);

        // if the name and id point to different objects
        if (itemByName != null && itemById != null && !itemByName.equals(itemById)){
            throw new SalesSystemException("Name and id don't match. Didn't add item.");
        }

        if (itemByName != null && itemById == null && !inId.isBlank()){
            throw new SalesSystemException("There is an object with this name, but not with id.");
        } else if (itemById != null && itemByName == null && !name.isBlank()) {
            if (itemById.getQuantity() == 0) throw new SalesSystemException("Please choose a newer id.");
            throw new SalesSystemException("There is an object with this id, but not with name");
        }

        if (itemById != null){
            // if item by id exists, use it.
            item = itemById;
        } else {
            item = itemByName;
        }

        if (item == null){
            // adding brand-new item
            // if both id and name don't exist, add a new item.
            addNewItem(name, desc, price, quantity);
        } else {
            // setting price and increasing quantity
            if (quantity == 0){
                // seting price
                // if quantity == 0, set price
                setPrice(item, price);
            } else {
                // increasing quantity
                increaseQuantity(item, quantity);
            }
        }

    }

    private void addNewItem(String name,String desc,double price,int quantity){
        if (quantity < 1){
            throw new SalesSystemException("Cannot add new item with quantity below 1");
        }

        if (name.isBlank()){
            throw new SalesSystemException("Cannot add new item with blank name");
        }

        StockItem newItem = new StockItem(name, desc, price, quantity);

        if (newItem.getId() == null)
            newItem.setId(dao.lastID()+1);

        dao.beginTransaction();
        try {
            dao.saveStockItem(newItem);
            log.info("Saved new stockItem: " + newItem.toString());
            dao.commitTransaction();
        } catch (Exception e){
            dao.rollbackTransaction();
            throw e;
        }


    }

    private void setPrice(StockItem item, double price){
        if (price < 0){
            throw new SalesSystemException("Price has to be above 0");
        }
        dao.beginTransaction();
        try {
            item.setPrice(price);
            log.info("Set price of " + item.getName() + " to " + item.getPrice());
            dao.commitTransaction();
        } catch (Exception e){
            dao.rollbackTransaction();
            throw e;
        }

    }

    private void increaseQuantity(StockItem item, int quantity){
        dao.beginTransaction();
        try {
            item.increaseQuantity(quantity);
//            if (item.getQuantity()==0){
//                dao.removeStockItem(item);
//                log.info("Removed " + item.getName());
//            }
            if (quantity > 0){
                log.info("Increased quantity of " + item.getName());
            } else if (quantity < 0){
                log.info("Decreased quantity of " + item.getName());
            }
            dao.commitTransaction();
        } catch (Exception e){
            log.info(e);
            dao.rollbackTransaction();
            throw e;
        }
    }
}
