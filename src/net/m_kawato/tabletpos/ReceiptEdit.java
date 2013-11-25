package net.m_kawato.tabletpos;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class ReceiptEdit extends Activity implements OnClickListener, DialogInterface.OnClickListener {
    private Globals globals;
    private static final String TAG = "ReceiptEdit";
    private ReceiptHelper receiptHelper;
    private List<Transaction> transactionList;
    private int selectedTransaction = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_edit);

        this.globals = (Globals) this.getApplication();
        this.receiptHelper = new ReceiptHelper(this, globals);

        // Table of Today's past orders
        buildPastOrderList();

        // Event handler of button (Top Menu)
        Button btnTopMenu = (Button) findViewById(R.id.btn_topmenu);
        btnTopMenu.setOnClickListener(this);   
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.receipt_edit, menu);
        return true;
    }

    // event handler for buttons
    @Override
    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
        case R.id.btn_topmenu:
            Log.d(TAG, "Top Menu button is clicked");
            globals.saveLoading();
            i = new Intent(this, TopMenu.class);
            startActivity(i);
            break;
        case R.id.btn_edit:
            Log.d(TAG, "Edit button is clicked");
            this.selectedTransaction = (Integer) v.getTag();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Edit");
            builder.setMessage("You are now going to load previous sales data for editing.\n" +
              "If proceed, the sales data is deleted from the csv data.");
            builder.setPositiveButton("Proceed", this);
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.create().show();
            break;
        }                
    }

    // event handler for AlertDialog 
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == android.content.DialogInterface.BUTTON_POSITIVE) {
            Log.d(TAG, "onClick: BUTTON_POSITIVE");
            loadPastOrder(this.transactionList.get(this.selectedTransaction));
            Intent i = new Intent(this, Confirm.class);
            startActivity(i);
        }
    }

    // Build past order list
    private void buildPastOrderList() {
        this.transactionList = receiptHelper.getPastTransactionList();
        
        if (transactionList == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("There is no today's receipt");
            alertDialogBuilder.setPositiveButton("OK", null);
            alertDialogBuilder.create().show();
            this.transactionList = new ArrayList<Transaction>();
        }
        
        LinearLayout placeHolder = (LinearLayout) findViewById(R.id.past_orders);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        int tag = 0;
        for(Transaction transaction: this.transactionList) {
            receiptHelper.buildReceipView(placeHolder, transaction, inflater, R.layout.receiptedit_header, tag);
            tag++;
        }
    }

    // Load past order
    private void loadPastOrder(Transaction transaction) {
        Log.d(TAG, "loadPastOrder");

        globals.initialize();
        globals.transaction = transaction;
        List<Integer> linesToRemove = new ArrayList<Integer>();
        
        for(OrderItem orderItem: transaction.orderItems) {
            Product p = orderItem.product;
            p.orderItem = orderItem;
            p.stock += orderItem.quantity;
            linesToRemove.add(orderItem.lineNumber);
        }

        receiptHelper.removeOldOrder(linesToRemove);

        
    }
}
