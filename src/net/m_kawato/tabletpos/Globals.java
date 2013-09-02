package net.m_kawato.tabletpos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Application;

public class Globals extends Application {
    public static final String PRODUCTS_FILENAME = "products.csv";
    public static final String SDCARD_DIRNAME = "TabletPOSApp";
    public static final String RECEIPT_FILENAME = "receipt.csv";

    public List<Product> products;
    public List<String> categories;
    public List<String> routes;
    public List<String> places;
    
    public List<Product> orderItems;
    public List<Map<String, Object>> orderItemList;
    public int selectedRoute;
    public int selectedPlace;
    public BigDecimal totalAmount;
    public BigDecimal creditAmount;

    public void initialize() {
        this.products = new ArrayList<Product>();
        this.categories = new ArrayList<String>();
        this.routes = new ArrayList<String>();
        this.places = new ArrayList<String>();
        this.orderItems = new ArrayList<Product>();
        this.orderItemList = new ArrayList<Map<String, Object>>();
        this.selectedRoute = 0;
        this.selectedPlace = 0;
        this.totalAmount = new BigDecimal(0);
        this.creditAmount = new BigDecimal(0);
    }
    
    public void addCategory(String category) {
        if (! this.categories.contains(category)) {
            this.categories.add(category);
        }
    }
}
