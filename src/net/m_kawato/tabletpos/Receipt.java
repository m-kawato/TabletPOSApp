package net.m_kawato.tabletpos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Receipt extends Activity implements View.OnClickListener, DialogInterface.OnClickListener {
    private static final String TAG = "Receipt";
    private Globals globals;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        this.globals = (Globals) this.getApplication();

        // Loading sheet number
        TextView loadingSheetNumberView = (TextView) findViewById(R.id.loading_sheet_number);
        loadingSheetNumberView.setText(globals.loadingSheetNumber);
        
        // date, route and place
        Log.d(TAG, String.format("onCreate: selectedRoute=%s, routes.size=%s",
                globals.selectedRoute, globals.routes.size()));
        TextView dateView = (TextView) findViewById(R.id.date);
        String dateText = DateFormat.format("dd-MM-yyyy", Calendar.getInstance()).toString();
        dateView.setText(dateText);

        TextView routeView = (TextView) findViewById(R.id.route);
        String routeCode = globals.routes.get(globals.selectedRoute);
        String routeName = globals.routeName.get(routeCode);
        routeView.setText(String.format("%s (%s)", routeName, routeCode));
        
        TextView placeView = (TextView) findViewById(R.id.place);
        String placeCode = globals.places.get(routeCode).get(globals.selectedPlace);
        String placeName = globals.placeName.get(placeCode);
        placeView.setText(String.format("%s (%s)", placeName, placeCode));

        // Table of current order list
        buildOrderList();
        
        // total amount, credit amount and cash amount
        TextView totalAmountView = (TextView) findViewById(R.id.total_amount);
        totalAmountView.setText(globals.transaction.getFormattedTotalAmount());

        TextView creditAmountView = (TextView) findViewById(R.id.credit_amount);
        creditAmountView.setText(globals.transaction.getFormattedCreditAmount());

        TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
        cashAmountView.setText(globals.transaction.getFormattedCashAmount());
        
        // Navigation buttons (Confirm, Top Menu, New Order)
        Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        Button btnTopMenu = (Button) findViewById(R.id.btn_topmenu);
        btnTopMenu.setOnClickListener(this);

        Button btnNewOrder = (Button) findViewById(R.id.btn_complete);
        btnNewOrder.setOnClickListener(this);
        
        // Table of Today's past orders
        buildPastOrderList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.receipt, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
        case R.id.btn_confirm:
            i = new Intent(this, Confirm.class);
            startActivity(i);
            break;
        case R.id.btn_topmenu:
            i = new Intent(this, TopMenu.class);
            startActivity(i);
            break;
        case R.id.btn_complete:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Complete");
            builder.setMessage("Save the receipt and enter a new order.");
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            builder.show();
            break;
        }        
    }

    // Event hander for dialog input
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case android.content.DialogInterface.BUTTON1:
            Log.d(TAG, "onClick: BUTTON1");

            // modify stock for items contained in the receipt
            modifyStock();

            // export receipt to file and DB
            exportReceiptFile();
            //exportReceiptDb();

            // reset order status and return to Order page
            globals.initialize();
            //globals.incrTransactionId();
            Intent i = new Intent(this, Order.class);
            startActivity(i);
            break;
        case android.content.DialogInterface.BUTTON2:
            break;
        }
        
    }

    // Export receipt data to sdcard
    private void exportReceiptFile() {
        Log.d(TAG, "exportReceiptFile");

        String timestamp = DateFormat.format("dd-MM-yyyy kk:mm", Calendar.getInstance()).toString();
        Log.d(TAG, String.format("exportReceipt: filename=%s, timestamp=%s",
                globals.getReceiptFile().getPath(), timestamp));

        String routeCode = globals.routes.get(globals.selectedRoute);
        String routeName = globals.routeName.get(routeCode);
        String placeCode = globals.places.get(routeCode).get(globals.selectedPlace);
        String placeName = globals.placeName.get(placeCode);
        
        PrintWriter writer = null;
        try {
            File dir = globals.getSdcardDir();
            if (! dir.exists()) {
                dir.mkdirs();  
            }
            File outfile = globals.getReceiptFile();
            String header = null;
            if (! outfile.exists()) {
                header = "Sale Date,Route Name,Route Code,RRP Name,RRP Code,Credit Amount,Product Name,Product Code,Quantity,Loading Sheet Number";
            }
            FileOutputStream fout = new FileOutputStream(outfile, true);
            writer = new PrintWriter(fout);
            if (header != null) {
                writer.println(header);
            }

            boolean firstItem = true;
            for (OrderItem orderItem: globals.transaction.orderItems) {
                String creditAmountText = null;
                if (firstItem) {
                    creditAmountText = globals.transaction.creditAmount.toString();
                    firstItem = false;
                } else {
                    creditAmountText = "0";
                }
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%d,%d,%s",
                        timestamp, routeName, routeCode, placeName, placeCode,
                        creditAmountText, orderItem.product.productName, orderItem.product.productId, orderItem.quantity,
                        globals.loadingSheetNumber));
            }
    
        } catch (IOException e) {
            Log.e(TAG, "failed to write receipt to sdcard");
            e.printStackTrace();
        } finally {
            writer.close();
        }  
    }

    // Build current order list
    private void buildOrderList() {
        LinearLayout placeHolder = (LinearLayout) findViewById(R.id.current_order);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        boolean firstItem = true;
        for (OrderItem orderItem: globals.transaction.orderItems) {
            if (firstItem) {
                firstItem = false;
            } else {
                View itemBorderLine = inflater.inflate(R.layout.item_separator, new FrameLayout(this));
                placeHolder.addView(itemBorderLine);
            }

            LinearLayout receiptItem = (LinearLayout) inflater.inflate(R.layout.receipt_item, null);
            placeHolder.addView(receiptItem);

            TextView productNameView = (TextView) receiptItem.findViewById(R.id.product_name);
            productNameView.setText(orderItem.product.productName);

            TextView unitPriceView = (TextView) receiptItem.findViewById(R.id.unit_price);
            unitPriceView.setText(orderItem.product.getFormattedUnitPrice());

            TextView unitPriceBoxView = (TextView) receiptItem.findViewById(R.id.unit_price_box);
            unitPriceBoxView.setText(orderItem.product.getFormattedUnitPriceBox());

            TextView quantityView = (TextView) receiptItem.findViewById(R.id.quantity);
            quantityView.setText(Integer.toString(orderItem.quantity));

            TextView amountView = (TextView) receiptItem.findViewById(R.id.amount);
            amountView.setText(orderItem.getFormattedAmount());

       }
    }

    // Build past order list
    private void buildPastOrderList() {
        // Read past transaction data from receipt file
        List<String> lines = new ArrayList<String>();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(globals.getReceiptFile());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String line;
            while((line = reader.readLine()) != null){     
                lines.add(line);    
            } 
            fin.close();
        } catch (IOException e) {
            Log.e(TAG, "failed to read receipt from sdcard");
            e.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                }
            }
        }  

        Map<String, Transaction> transactionMap = new HashMap<String, Transaction>();
        List<Transaction> transactionList = new ArrayList<Transaction>();

        for(int i = 0; i < lines.size(); i++) {
            String[] lineSplit = lines.get(i).split("\\s*,\\s*");
            if (lineSplit.length != 10) {
                Log.d(TAG, String.format("line skipped: %s, length=%d", lines.get(i), lineSplit.length));
                continue;
            }
            try {
                String timestamp = lineSplit[0];
                String routeCode = lineSplit[2];
                String placeCode = lineSplit[4];
                int productId = Integer.parseInt(lineSplit[7]);
                int quantity = Integer.parseInt(lineSplit[8]);
                BigDecimal creditAmount = new BigDecimal(lineSplit[5]);
                Log.d(TAG, String.format("buildPastOrderList: timestamp=%s,routeCode=%s,placeCode=%s,productId=%d,quantity=%d,creditAmount=%s",
                        timestamp, routeCode, placeCode, productId, quantity, creditAmount.toString()));
                OrderItem orderItem = new OrderItem(this, globals.getProduct(productId), quantity);
                Transaction transaction = transactionMap.get(String.format("%s,%s,%s", timestamp, routeCode, placeCode));
                if (transaction == null) {
                    transaction = new Transaction(this, routeCode, placeCode, creditAmount);
                    transactionMap.put(String.format("%s,%s,%s", timestamp, routeCode, placeCode), transaction);
                    transactionList.add(transaction);
                }
                transaction.creditAmount = transaction.creditAmount.add(creditAmount);
                transaction.addOrderItem(orderItem);
            } catch (NumberFormatException e) {
                Log.d(TAG, "line skipped: " + lines.get(i));
                continue;
            }            
        }
        Collections.reverse(transactionList);
        
        // Sort order items for each transaction by product name
        for(Transaction t: transactionList) {
            Collections.sort(t.orderItems, new Comparator<OrderItem>() {
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
        
        // Build view for past orders
        LinearLayout placeHolder = (LinearLayout) findViewById(R.id.past_orders);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(Transaction transaction: transactionList) {
            // header (route and place)
            LinearLayout receiptHeader = (LinearLayout) inflater.inflate(R.layout.receipt_header, null);
            placeHolder.addView(receiptHeader);

            String routeName = globals.routeName.get(transaction.routeCode);
            TextView routeView = (TextView) receiptHeader.findViewById(R.id.route);
            routeView.setText(String.format("%s (%s)", routeName, transaction.routeCode));

            String placeName = globals.placeName.get(transaction.placeCode);
            TextView placeView = (TextView) receiptHeader.findViewById(R.id.place);
            placeView.setText(String.format("%s (%s)", placeName, transaction.placeCode));

            // order list
            boolean firstItem = true;
            for (OrderItem orderItem: transaction.orderItems) {
                Product p = orderItem.product;
                if (firstItem) {
                    firstItem = false;
                } else {
                    View itemBorderLine = inflater.inflate(R.layout.item_separator, new FrameLayout(this));
                    placeHolder.addView(itemBorderLine);
                }

                LinearLayout receiptItem = (LinearLayout) inflater.inflate(R.layout.receipt_item, null);
                placeHolder.addView(receiptItem);

                TextView productNameView = (TextView) receiptItem.findViewById(R.id.product_name);
                productNameView.setText(p.productName);

                TextView unitPriceView = (TextView) receiptItem.findViewById(R.id.unit_price);
                unitPriceView.setText(p.getFormattedUnitPrice());

                TextView unitPriceBoxView = (TextView) receiptItem.findViewById(R.id.unit_price_box);
                unitPriceBoxView.setText(p.getFormattedUnitPriceBox());

                TextView quantityView = (TextView) receiptItem.findViewById(R.id.quantity);
                quantityView.setText(Integer.toString(orderItem.quantity));

                TextView amountView = (TextView) receiptItem.findViewById(R.id.amount);
                amountView.setText(orderItem.getFormattedAmount());
            }

            // footer (total amount, credit amount and cash amount)
           
            LinearLayout receiptFooter = (LinearLayout) inflater.inflate(R.layout.receipt_footer, null);
            placeHolder.addView(receiptFooter);

            TextView totalAmountView = (TextView) receiptFooter.findViewById(R.id.total_amount);
            totalAmountView.setText(transaction.getFormattedTotalAmount());

            TextView creditAmountView = (TextView) receiptFooter.findViewById(R.id.credit_amount);
            creditAmountView.setText(transaction.getFormattedCreditAmount());

            TextView cashAmountView = (TextView) receiptFooter.findViewById(R.id.cash_amount);
            cashAmountView.setText(transaction.getFormattedCashAmount());
        }
    
    }

    // Modify stocks of products according to the receipt
    void modifyStock() {
        Log.d(TAG, "modifyStock");

        for (OrderItem orderItem: globals.transaction.orderItems) {
            Product p = orderItem.product;
            p.stock -= orderItem.quantity;
        }
        globals.saveLoading();
    }
}
