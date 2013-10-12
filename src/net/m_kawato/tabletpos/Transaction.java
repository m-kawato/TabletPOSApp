package net.m_kawato.tabletpos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    public long transactionId;
    public String routeCode;
    public String placeCode;
    public BigDecimal creditAmount;
    public List<OrderItem> orderItems = new ArrayList<OrderItem>();

    public Transaction(long transactionId, String routeCode, String placeCode, BigDecimal creditAmount) {
        this.transactionId = transactionId;
        this.routeCode = routeCode;
        this.placeCode = placeCode;
        this.creditAmount = creditAmount;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = new BigDecimal(0);
        for(OrderItem orderItem: orderItems) {
            totalAmount = totalAmount.add(orderItem.getAmount());
        }
        return totalAmount;
    }

    public BigDecimal getCashAmount() {
        return getTotalAmount().subtract(this.creditAmount);
    }

    public BigDecimal getCreditAmount() {
        return this.creditAmount;
    }
}
