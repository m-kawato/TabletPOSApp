package net.m_kawato.tabletpos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    private ReceiptHelper receiptHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        this.globals = (Globals) this.getApplication();
        this.receiptHelper = new ReceiptHelper(this, globals);

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
        case android.content.DialogInterface.BUTTON_POSITIVE:
            Log.d(TAG, "onClick: BUTTON_POSITIVE");

            // modify stock for items contained in the receipt
            modifyStock();

            // export receipt to file
            exportReceiptFile();

            // reset order status and return to Order page
            globals.initialize();
            Intent i = new Intent(this, Order.class);
            startActivity(i);
            break;
        case android.content.DialogInterface.BUTTON_NEGATIVE:
            break;
        }
        
    }

    // Export receipt data to sdcard
    private void exportReceiptFile() {
        Log.d(TAG, "exportReceiptFile");

        String timestamp = DateFormat.format("dd-MM-yyyy kk:mm", Calendar.getInstance()).toString();
        Log.d(TAG, String.format("exportReceipt: filename=%s, timestamp=%s",
                receiptHelper.getReceiptFile().getPath(), timestamp));

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
            File outfile = receiptHelper.getReceiptFile();
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
        List<Transaction> transactionList = receiptHelper.getPastTransactionList();
        if (transactionList == null) {
            transactionList = new ArrayList<Transaction>();
        }        

        LinearLayout placeHolder = (LinearLayout) findViewById(R.id.past_orders);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        for(Transaction transaction: transactionList) {
            receiptHelper.buildReceipView(placeHolder, transaction, inflater, R.layout.receipt_header);
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
