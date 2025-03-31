package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.HistoryEntry;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "History" in the menu).
 */
public class HistoryController implements Initializable {
    private static final Logger log = LogManager.getLogger(HistoryController.class);
    private final SalesSystemDAO dao;
    @FXML
    private Button showBetweenDates;
    @FXML
    private Button show10;
    @FXML
    private Button showAll;
    @FXML
    private TableView<HistoryEntry> historyTableView;
    @FXML
    private TableView<SoldItem> purchaseHistoryTableView;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;


    public HistoryController(SalesSystemDAO dao){
        this.dao = dao;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historyTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                purchaseHistoryTableView.setItems(FXCollections.observableList(newSelection.getSoldItemList()));
            }
        });
    }

    @FXML
    protected void showBetweenDatesClicked() {
        purchaseHistoryTableView.setItems(null);

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start==null || end==null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please choose the dates");
            alert.show();
            log.error("No dates were chosen. Couldn't display entries by dates.");
        } else if (start.isAfter(end)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Start date can't be bigger than end date");
            alert.show();
            log.error("Start date was bigger than end date. Couldn't display entries by dates.");
        }
        else {
            ObservableList<HistoryEntry> historyEntries = FXCollections.observableList(dao.getEntriesByDate(start,end));

            historyTableView.setItems(historyEntries);

            if (historyEntries.isEmpty()) log.info("No purchases between " + start + " and " + end + " to display currently");
            else {
                log.info("Showing " + historyEntries.size() + " purchases between " + start + " and " + end);
                log.debug("Total entries: " + historyEntries.size());
            }
        }
    }

    @FXML
    protected void show10Clicked() {
        showNentries(10);
    }
    @FXML
    protected void showAllClicked() {
        showNentries(-1);
    }

    private void showNentries (int n){
        purchaseHistoryTableView.setItems(null);

        ObservableList<HistoryEntry> historyEntries = FXCollections.observableList(dao.getNentries(n));

        historyTableView.setItems(historyEntries);

        if (historyEntries.isEmpty()) log.info("No sold items to display currently");
        else {
            log.info("Showing " + historyEntries.size() + " purchases");
            log.debug("Total entries: " + historyEntries.size());
        }
    }
}
