package net.m_kawato.tabletpos;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class Product {
    private Context context;
    public int productId;
    public int salesId;
    public String productName;
    public String category;
    public BigDecimal unitPrice;
    public BigDecimal unitPriceBox;
    public int numPiecesInBox;
    public int quantity; 
    public int quantityBox;
    public boolean confirmed = false;

    public Product(Context context, int productId, String productName, String category, BigDecimal unitPrice, int quantity, BigDecimal unitPriceBox, int quantityBox, int numPiecesInBox) {
        this.context = context;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.unitPriceBox = unitPriceBox;
        this.quantityBox = quantityBox;
        this.numPiecesInBox = numPiecesInBox;
    }

    public Product(Context context, int productId, String productName, String category, BigDecimal unitPrice, BigDecimal unitPriceBox, int numPiecesInBox) {
        this(context, productId, productName, category, unitPrice, 0, unitPriceBox, 0, numPiecesInBox);
    }

    public BigDecimal getAmount() {
        BigDecimal amountPieces = this.unitPrice.multiply(new BigDecimal(this.quantity));
        BigDecimal amountBoxes = this.unitPriceBox.multiply(new BigDecimal(this.numPiecesInBox)).multiply(new BigDecimal(this.quantityBox));
        return amountPieces.add(amountBoxes);
    }

    public Map<String, Object> toMap() {
        String currency = context.getString(R.string.currency);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("image", context.getResources().getIdentifier("product" + this.productId, "drawable", context.getPackageName()));
        map.put("product_name", this.productName);
        map.put("unit_price", this.unitPrice.toString() + " " + currency);
        map.put("quantity", Integer.toString(this.quantity));
        map.put("unit_price_box", String.format("%s %s\n* %d pcs.", this.unitPriceBox.toString(), currency, this.numPiecesInBox));
        map.put("quantity_box", Integer.toString(this.quantityBox));
        map.put("amount", this.getAmount().toString() + " " + currency);
        return map;
    }
}

