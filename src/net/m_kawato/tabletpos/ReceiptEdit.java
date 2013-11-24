package net.m_kawato.tabletpos;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class ReceiptEdit extends Activity implements OnClickListener {
    private Globals globals;
    private static final String TAG = "ReceiptEdit";
    private ReceiptHelper receiptHelper;

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
        }                
    }

    // Build past order list
    private void buildPastOrderList() {
        List<Transaction> transactionList = receiptHelper.getPastTransactionList();
        
        if (transactionList == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("There is no today's receipt");
            alertDialogBuilder.setPositiveButton("OK", null);
            alertDialogBuilder.create().show();
            transactionList = new ArrayList<Transaction>();
        }
        
        LinearLayout placeHolder = (LinearLayout) findViewById(R.id.past_orders);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        for(Transaction transaction: transactionList) {
            receiptHelper.buildReceipView(placeHolder, transaction, inflater, R.layout.receiptedit_header);
        }
    }
}
