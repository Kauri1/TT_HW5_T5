package ee.ut.math.tvt.salessystem.ui.controllers;

import com.sun.javafx.collections.ObservableListWrapper;
import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.WarehouseManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public class StockController implements Initializable {

    private final SalesSystemDAO dao;

    private final WarehouseManager wm;

    private static final Logger log = LogManager.getLogger(StockController.class);

    @FXML
    private Button addProductButton;

    @FXML
    private TableView<StockItem> warehouseTableView;

    @FXML
    private TextField barCodeField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;

    public StockController(SalesSystemDAO dao, WarehouseManager wm) {
        this.dao = dao;
        this.wm = wm;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshStockItems();
    }

    @FXML
    public void refreshButtonClicked() {
        log.info("Refreshing warehouse");
        refreshStockItems();
    }

    private void refreshStockItems() {
        warehouseTableView.setItems(FXCollections.observableList(dao.findStockItems()));
        warehouseTableView.refresh();
    }

    @FXML
    public void addProductButtonClicked() {
        log.info("Adding new product");
        addProduct();
    }

    private void addProduct() {

        //todo no logic here

        try {
            // add new procuct

            wm.addItem(
                    barCodeField.getText(),
                    nameField.getText(),
                    "desc",
                    priceField.getText(),
                    quantityField.getText()
            );
        } catch (Exception e){
            //JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Popup", JOptionPane.ERROR_MESSAGE);
            new Alert(Alert.AlertType.WARNING, e.getMessage()).show();
            log.info(e.getMessage());
        }

        refreshStockItems();
    }
}
