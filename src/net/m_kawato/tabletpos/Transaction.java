package net.m_kawato.tabletpos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;

public class Transaction {
    private Context context;
    //public long transactionId;
    public String routeCode;
    public String placeCode;
    public BigDecimal creditAmount;
    public List<OrderItem> orderItems = new ArrayList<OrderItem>();
    public String timestamp;
    public String loadingSheetNumber;

    public Transaction(Context context, String routeCode, String placeCode,
            BigDecimal creditAmount, String loadingSheetNumber) {
        this.context = context;
        this.routeCode = routeCode;
        this.placeCode = placeCode;
        this.creditAmount = creditAmount;
        this.creditAmount = new BigDecimal(0);
        this.loadingSheetNumber = loadingSheetNumber;
    }

    public boolean contains(Product p) {
        for(OrderItem orderItem: this.orderItems) {
            if (orderItem.product.productId == p.productId) {
                return true;
            }
        }
        return false;
    }
    
    public void clearOrderItems() {
        this.orderItems.clear();
    }

    public void addOrderItem(OrderItem orderItem) {
        if (! this.orderItems.contains(orderItem)) {
            this.orderItems.add(orderItem);
        }
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = new BigDecimal(0);
        for(OrderItem orderItem: orderItems) {
            totalAmount = totalAmount.add(orderItem.getAmount());
        }
        return totalAmount;
    }

    public String getFormattedTotalAmount() {
        return getTotalAmount().toString() + " " + context.getString(R.string.currency);
    }

    public BigDecimal getCashAmount() {
        return getTotalAmount().subtract(this.creditAmount);
    }

    public String getFormattedCashAmount() {
        return getCashAmount().toString() + " " + context.getString(R.string.currency);
    }

    public BigDecimal getCreditAmount() {
        return this.creditAmount;
    }

    public String getFormattedCreditAmount() {
        return this.creditAmount.toString() + " " + context.getString(R.string.currency);
        
    }

    public void setTimeStamp() {
        this.timestamp = DateFormat.format("dd-MM-yyyy kk:mm", Calendar.getInstance()).toString();
    }

    // Remove order item where quantity == 0
    public void removeZeroQuantity() {
        List<OrderItem> newOrderItems = new ArrayList<OrderItem>();
        for (OrderItem orderItem: this.orderItems) {
            if (orderItem.quantity > 0) {
                newOrderItems.add(orderItem);
            }
        }
        this.orderItems = newOrderItems;
    }
    
    // Sort order items by product category, product name
    public void sortOrderItems() {
        Collections.sort(this.orderItems, new Comparator<OrderItem>() {
            @Override
            public int compare(OrderItem item_a, OrderItem item_b) {
                Product a = item_a.product;
                Product b = item_b.product;
                if (a.category.compareTo(b.category) != 0) {
                    return a.category.compareTo(b.category);
                } else {
                    return a.productName.compareTo(b.productName);
                }
            }
        });
    }
}
