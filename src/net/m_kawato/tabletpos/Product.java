package net.m_kawato.tabletpos;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class Product {
    private Context context;
    public int productId;
    public String productName;
    public String category;
    public BigDecimal unitPrice;
    public BigDecimal unitPriceBox;
    public int numPiecesInBox;
    public OrderItem orderItem; // refers to the order item for this product
    public boolean loaded = false; // true if the product is checked in Loading activity
    
    public Product(Context context, int productId, String productName, String category, BigDecimal unitPrice, BigDecimal unitPriceBox, int numPiecesInBox) {
        this.context = context;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.unitPriceBox = unitPriceBox;
        this.numPiecesInBox = numPiecesInBox;
    }

    public String getFormattedUnitPrice() {
        return this.unitPrice.toString() + " " + context.getString(R.string.currency);
    }

    public String getFormattedUnitPriceBox() {
        return String.format("%s %s\n* %d pcs.",
                this.unitPriceBox.toString(),
                context.getString(R.string.currency),
                this.numPiecesInBox);
    }

    public Map<String, Object> getDefaultMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("image", context.getResources().getIdentifier("product" + this.productId, "drawable", context.getPackageName()));
        map.put("product_name", this.productName);
        map.put("quantity", "");
        map.put("unit_price", this.getFormattedUnitPrice());
        map.put("unit_price_box", this.getFormattedUnitPriceBox());
        map.put("amount", "");
        return map;
    }
}

