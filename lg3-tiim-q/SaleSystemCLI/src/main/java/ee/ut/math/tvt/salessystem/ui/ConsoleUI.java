package ee.ut.math.tvt.salessystem.ui;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.HistoryEntry;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import ee.ut.math.tvt.salessystem.logic.WarehouseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * A simple CLI (limited functionality).
 */
public class ConsoleUI {
    private static final Logger log = LogManager.getLogger(ConsoleUI.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart cart;

    private final WarehouseManager wm;

    public ConsoleUI(SalesSystemDAO dao, WarehouseManager wm) {
        this.dao = dao;
        cart = new ShoppingCart(dao);
        this.wm = wm;
    }

    public static void main(String[] args) throws Exception {
        try {
            //SalesSystemDAO dao = new HibernateSalesSystemDAO();//new InMemorySalesSystemDAO();
            SalesSystemDAO dao = new InMemorySalesSystemDAO();
            WarehouseManager wm = new WarehouseManager(dao);
            ConsoleUI console = new ConsoleUI(dao, wm);
            console.run();
        }
        catch (SalesSystemException e){
            System.out.println("\u001B[31m" +"\nCould not reach the database. Please run the database task (hsql) and try again."+"\033[0m");
            Thread.sleep(5000); // waiting so that the user notices the message
            System.exit(0);
        }
    }

    /**
     * Run the sales system CLI.
     */
    public void run() throws IOException {
        System.out.println("===========================");
        System.out.println("=       Sales System      =");
        System.out.println("===========================");
        printUsage();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            processCommand(in.readLine().trim().toLowerCase());
            System.out.println("Done. ");
        }
    }

    private void showStock() {
        List<StockItem> stockItems = dao.findStockItems();
        System.out.println("-------------------------");
        for (StockItem si : stockItems) {
            System.out.println(si.getId() + " " + si.getName() + " " + si.getPrice() + "Euro (" + si.getQuantity() + " items)");
        }
        if (stockItems.size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("-------------------------");
    }

    private void showCart() {
        System.out.println("-------------------------");
        for (SoldItem si : cart.getAll()) {
            System.out.println(si.getName() + " " + si.getPriceXquantity() + " € (" + si.getQuantity() + " price: " + si.getPrice() + " €)");
        }
        if (cart.getAll().size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("-------------------------");
    }

    private void showTeam() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("../src/main/resources/application.properties")) {

            properties.load(fileInputStream);

            String name = properties.getProperty("teamtab.teamname");
            String leader = properties.getProperty("teamtab.leader");
            String mail = properties.getProperty("teamtab.leademail");
            String members = properties.getProperty("teamtab.members");

            System.out.println("Team name: " + name);
            System.out.println("Team leader: " + leader);
            System.out.println("Team leader email: " + mail);
            System.out.println("Team members: ");
            System.out.println(members);
            System.out.println();
            showCake();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void showCake() {
        String cakeArt = "\033[35m" +  // Purple for cake layers
                "    iiiiiiiiiii      \n" +
                "\033[33m" +  // Yellow for the candles
                "   |:T:I:I:M:Q:|    \n" +
                "\033[35m" +  // Purple for the cake layer
                " __|___________|__  \n" +
                "\033[34m" +  // Blue for the frosting
                " |^^^^^^^^^^^^^^^^^| \n" +
                "\033[35m" +  // Purple for the cake text
                " |:c:a:k:e:c:a:k:e:| \n" +
                " |                 |  \n" +
                "\033[34m" +  // Blue for the bottom layer
                " ~~~~~~~~~~~~~~~~~~~  \n" +
                "\033[0m";  // Reset color

        System.out.println(cakeArt);
    }

    private void purchaseHistoryMenu() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;

        System.out.println("===========================");
        System.out.println("=          History        =");
        System.out.println("===========================");

        printHistoryUsage();

        while (!exit) {
            System.out.print("> ");
            String command = reader.readLine().trim().toLowerCase();
            String[] c = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            switch (c[0]) {
                case "home":
                    // Go back to the main menu
                    printUsage();
                    exit = true;
                    break;
                case "h":
                    printHistoryUsage();
                    break;
                // SAME LOGIC FOR S10, SA
                case "s10":
                case "sa":
                    if (dao.findSoldItems().isEmpty()) {
                        System.out.println("\tNo sold items found");
                        break;
                    }

                    int n = command.equals("s10") ? 10 : -1;
                    List<HistoryEntry> entries = dao.getNentries(n);
                    displayAndSelectHistoryEntry(entries);
                    break;

                case "sb":
                    if (c.length == 3) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                            String stripped1 = c[1].replaceAll("^\"|\"$", "");
                            String stripped2 = c[2].replaceAll("^\"|\"$", "");

                            LocalDate startDate = LocalDate.parse(stripped1, formatter);
                            LocalDate endDate = LocalDate.parse(stripped2, formatter);

                            if (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                                List<HistoryEntry> purchasesBetweenDates = dao.getEntriesByDate(startDate, endDate);

                                if (!purchasesBetweenDates.isEmpty()) {
                                    displayAndSelectHistoryEntry(purchasesBetweenDates);
                                } else {
                                    System.out.println("No purchases found in the given date range.");
                                }
                            } else {
                                System.out.println("Invalid date range. The start date must be before or the same as the end date.");

                            }
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format. Please use \"dd/MM/yyyy\".");
                        }
                    } else {
                        System.out.println("Please provide two dates in the format: sb \"dd/MM/yyyy\" \"dd/MM/yyyy\"");
                    }
                    break;

                default:
                    System.out.println("Invalid command. Try again.");

            }
        }
    }

    private void warehouseMenu() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;

        System.out.println("===========================");
        System.out.println("=       Warehouse         =");
        System.out.println("===========================");

        printWarehouseUsage();

        while (!exit) {
            System.out.print("> ");
            String command = reader.readLine().trim().toLowerCase();
            String[] c = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            switch (c[0]) {
                case "home":
                    printUsage();
                    exit = true;
                    break;
                case "h":
                    printWarehouseUsage();
                    break;
                case "w":
                    showStock();
                    break;
                case "a":
                    if (c.length == 6) {
                        try {
                            String ID = c[1];
                            String name = c[2].replaceAll("^\"|\"$", "");
                            String price = c[3];
                            String description = c[4];
                            String quantity = c[5];
                            //StockItem newItem = new StockItem(ID, name, description, price, quantity);
                            wm.addItem(ID, name, description, price, quantity);

                            System.out.println("Item added to warehouse: " + name + " (" + description + ")");
                        } catch (SalesSystemException | NoSuchElementException | NumberFormatException e) {
                            System.out.println("Error: " + e.getMessage());
                        } catch (Exception e) {
                            System.out.println("An unexpected error occurred. Please try again.");
                        }

                    } else {
                        System.out.println("Invalid command format. Usage: a IDX NAME PRICE \"DESC\" NR");
                    }
                    break;
                default:
                    System.out.println("Invalid command. Try again.");
            }
        }
    }

    private void displayAndSelectHistoryEntry(List<HistoryEntry> entries) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        int i = 1;
        for (HistoryEntry entry : entries) {
            System.out.println(i + ". Date: " + entry.getDateSold() + ", at: " + entry.getTimeSold());
            i++;
        }

        boolean validChoice = false;
        while (!validChoice) {
            System.out.print("Enter the number of the purchase to view its contents: ");
            try {
                int choice = Integer.parseInt(reader.readLine().trim());

                if (choice > 0 && choice <= entries.size()) {
                    HistoryEntry selectedEntry = entries.get(choice - 1);

                    System.out.println("Items in the selected purchase:");
                    for (SoldItem item : selectedEntry.getSoldItemList()) {
                        System.out.println(" - " + item.getName() + ", Quantity: " + item.getQuantity() + ", Price: " + item.getPrice());
                    }
                    validChoice = true;
                } else {
                    System.out.println("Invalid choice. Please select a valid purchase number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (IOException e) {
                log.error("Error reading input", e);
                break;
            }
        }
    }

    private void printUsage() {
        // ANSI color codes
        String RESET = "\u001B[0m";
        String GREEN = "\u001B[32m";

        System.out.println("-------------------------");
        System.out.println("Usage:");
        System.out.println(GREEN + "h" + RESET + "\t\tShow this help :)");
        System.out.println(GREEN + "w" + RESET + "\t\tShow warehouse menu");
        System.out.println(GREEN + "c" + RESET + "\t\tShow cart contents");
        System.out.println(GREEN + "a IDX NR" + RESET + "\tAdd NR of stock item with index IDX to the cart");
        System.out.println(GREEN + "p" + RESET + "\t\tPurchase the shopping cart");
        System.out.println(GREEN + "r" + RESET + "\t\tReset the shopping cart");
        System.out.println(GREEN + "hst" + RESET + "\t\tShow purchase history menu");
        System.out.println(GREEN + "t" + RESET + "\t\tShow the team tab");
        System.out.println(GREEN + "q" + RESET + "\t\tQuit the app");
        System.out.println("-------------------------");
    }

    private void printHistoryUsage() {
        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";
        System.out.println("-------------------------");
        System.out.println("Usage:");
        System.out.println(GREEN + "h" + RESET + "\t\tShow this help :)");
        System.out.println(GREEN + "home" + RESET + "\t\tReturn to main menu");
        System.out.println(GREEN + "sa" + RESET + "\tShow all purchase history");
        System.out.println(GREEN + "s10" + RESET + "\tShow last 10 purchases");
        System.out.println(GREEN + "sb \"dd/MM/yyyy\" \"dd/MM/yyyy\""+ RESET + "\tShow purchases between \"date1\" and \"date2\"");
        System.out.println("-------------------------");
    }

    private void printWarehouseUsage() {
        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";
        System.out.println("-------------------------");
        System.out.println("Usage:");
        System.out.println(GREEN + "h" + RESET + "\t\tShow this help :)");
        System.out.println(GREEN + "home" + RESET + "\t\tReturn to main menu");
        System.out.println(GREEN + "w" + RESET + "\tShow warehouse contents");
        System.out.println(GREEN + "a IDX NAME PRICE \"DESC\" NR" + RESET +
                "\tAdd item to the warehouse with given ID, Name, Price, \"Description\", and Quantity");
        System.out.println("-------------------------");
    }

    private void processCommand(String command) throws IOException {
        String[] c = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        if (c[0].equals("h"))
            printUsage();
        else if (c[0].equals("q"))
            System.exit(0);
        else if (c[0].equals("t"))
            showTeam();
        else if (c[0].equals("w"))
            warehouseMenu();
        else if (c[0].equals("c"))
            showCart();
        else if (c[0].equals("p"))
            cart.submitCurrentPurchase();
        else if (c[0].equals("r"))
            cart.cancelCurrentPurchase();
        else if (c[0].equals("hst"))
            purchaseHistoryMenu();
        else if (c[0].equals("a") && c.length == 3) {
            try {
                long idx = Long.parseLong(c[1]);
                int amount = Integer.parseInt(c[2]);
                StockItem item = dao.findStockItem(idx);
                if (item != null) {
                    cart.addItem(item, amount);
                } else {
                    System.out.println("no stock item with id " + idx);
                }
            } catch (SalesSystemException | NoSuchElementException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            System.out.println("unknown command");
        }
    }

}

