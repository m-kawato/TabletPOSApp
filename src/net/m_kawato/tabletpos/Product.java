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
    public int quantity; 
    public BigDecimal unitPrice;
    public BigDecimal unitPriceBox;
    public int numPiecesInBox;
    public boolean confirmed = false;

    public Product(Context context, int productId, String productName, String category, int quantity, BigDecimal unitPrice, BigDecimal unitPriceBox, int numPiecesInBox) {
        this.context = context;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitPriceBox = unitPriceBox;
        this.numPiecesInBox = numPiecesInBox;
    }

    public Product(Context context, int productId, String productName, String category, BigDecimal unitPrice, BigDecimal unitPriceBox, int numPiecesInBox) {
        this(context, productId, productName, category, 0, unitPrice, unitPriceBox, numPiecesInBox);
    }

    public BigDecimal getAmount() {
        int numBoxes = this.quantity / this.numPiecesInBox;
        int numPieces = this.quantity - numBoxes * this.numPiecesInBox;
        BigDecimal amountPieces = this.unitPrice.multiply(new BigDecimal(numPieces));
        BigDecimal amountBoxes = this.unitPriceBox.multiply(new BigDecimal(numBoxes * this.numPiecesInBox));
        return amountPieces.add(amountBoxes);
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
    
    public String getFormattedAmount() {
        return this.getAmount().toString() + " " + context.getString(R.string.currency);
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("image", context.getResources().getIdentifier("product" + this.productId, "drawable", context.getPackageName()));
        map.put("product_name", this.productName);
        map.put("quantity", Integer.toString(this.quantity));
        map.put("unit_price", getFormattedUnitPrice());
        map.put("unit_price_box", getFormattedUnitPriceBox());
        map.put("amount", getFormattedAmount());
        return map;
    }
}

