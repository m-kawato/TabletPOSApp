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

    public List<Product> products = new ArrayList<Product>();
    public List<String> categories = new ArrayList<String>();
    public List<String> routes = new ArrayList<String>();
    public List<String> places = new ArrayList<String>();
    
    public List<Product> orderItems = new ArrayList<Product>();
    public List<Map<String, Object>> orderItemList = new ArrayList<Map<String, Object>>();
    public int selectedRoute = 0;
    public int selectedPlace = 0;
    public BigDecimal totalAmount = new BigDecimal(0);
    public BigDecimal creditAmount = new BigDecimal(0);
    
    public void addCategory(String category) {
        if (! this.categories.contains(category)) {
            this.categories.add(category);
        }
    }
}
