package net.m_kawato.tabletpos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Confirm extends Activity implements OnClickListener, AdapterView.OnItemClickListener, DialogInterface.OnDismissListener, OnEditorActionListener {
    private static final String TAG = "Confirm";
    private Globals globals;
    private int selectedPosition;
    private SimpleAdapter orderListAdapter;
    private List<Map<String, Object>> orderItemList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
 
        this.globals = (Globals) this.getApplication();
        OrderInputHelper orderInputHelper = new OrderInputHelper(this, globals);

        // Build ListView of order items
        ListView orderListView = (ListView) findViewById(R.id.list);
        LayoutInflater inflater = this.getLayoutInflater();
        View header = inflater.inflate(R.layout.confirm_header, null);
        View footer = inflater.inflate(R.layout.confirm_footer, null);
        orderListView.addHeaderView(header);
        orderListView.addFooterView(footer);

        this.orderItemList = new ArrayList<Map<String, Object>>(); 
        for(OrderItem orderItem: globals.transaction.orderItems) {
            this.orderItemList.add(orderItem.toMap());
        }

        String[] from = {"image", "product_name", "unit_price", "unit_price_box", "quantity", "amount"};
        int[] to = {R.id.image, R.id.product_name, R.id.unit_price, R.id.unit_price_box, R.id.quantity, R.id.amount};
        this.orderListAdapter = new SimpleAdapter(this, this.orderItemList, R.layout.confirm_item, from, to);
        orderListView.setAdapter(this.orderListAdapter);        
        orderListView.setOnItemClickListener(this);

        // Loading sheet number
        TextView loadingSheetNumberView = (TextView) findViewById(R.id.loading_sheet_number);
        loadingSheetNumberView.setText(globals.loadingSheetNumber);
        
        // Spinner for route selection
        orderInputHelper.buildRouteSelector();

        // Spinner for place selection
        orderInputHelper.buildPlaceSelector();

        // total amount, credit amount and cash amount
        TextView totalAmountView = (TextView) findViewById(R.id.total_amount);
        totalAmountView.setText(globals.transaction.getFormattedTotalAmount());

        EditText creditAmountView = (EditText) findViewById(R.id.credit_amount);
        creditAmountView.setText(globals.transaction.getCreditAmount().toString());
        creditAmountView.setOnEditorActionListener(this);
        Button btnUpdate = (Button) findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(this);

        TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
        cashAmountView.setText(globals.transaction.getFormattedCashAmount());        
        
        // "Order" button
        Button btnOrder = (Button) findViewById(R.id.btn_order);
        btnOrder.setOnClickListener(this);

        // "Receipt" button
        Button btnReceipt = (Button) findViewById(R.id.btn_receipt);
        btnReceipt.setOnClickListener(this);        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.confirm, menu);
        return true;
    }

    // Event handler for items of order item list
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position--; // compensate for position increase when header is inserted
        Log.d(TAG, "onItemClickListener: position = " + position);
        OrderItem orderItem = globals.transaction.orderItems.get(position);
        Log.d(TAG, "onItemClick: productId = " + orderItem.product.productId + ", productName = " + orderItem.product.productName);
        this.selectedPosition = position;
        OrderInputDialog dialog = new OrderInputDialog(this, orderItem.product);
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    // Event handler for TextEdit
    @Override  
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d(TAG, "onEditorAction: actionId=" + actionId);
        if (actionId == EditorInfo.IME_ACTION_GO) {  
            updateCreditAmount();
        }  
        return true;  
    }  

    // Event handler for buttons
    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
        case R.id.btn_update:
            updateCreditAmount();
            break;
        case R.id.btn_order:
            i = new Intent(this, Order.class);
            startActivity(i);
            break;
        case R.id.btn_receipt:
            i = new Intent(this, Receipt.class);
            startActivity(i);
            break;
        }        
    } 

    // Event handler for order edit dialog
    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(TAG, "onDismiss");
        OrderItem orderItem = globals.transaction.orderItems.get(this.selectedPosition);
        this.orderItemList.set(this.selectedPosition, orderItem.toMap());
        this.orderListAdapter.notifyDataSetChanged();

        // Update totalAmount and cashAmount
        TextView totalAmountView = (TextView) findViewById(R.id.total_amount);
        totalAmountView.setText(globals.transaction.getFormattedTotalAmount());
        TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
        cashAmountView.setText(globals.transaction.getFormattedCashAmount());        
    }    
    
    private void updateCreditAmount() {
        EditText creditAmountView = (EditText) findViewById(R.id.credit_amount);
        String creditAmountText = creditAmountView.getText().toString();
        if (creditAmountText.equals("")) {
            return;
        }
        BigDecimal creditAmount = new BigDecimal(creditAmountText);
        if (globals.transaction.getTotalAmount().compareTo(creditAmount) <= 0) {
            // TODO show toast (issue #36)
            return;
        }
        globals.transaction.creditAmount = creditAmount;

        // Update cashAmount
        TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
        cashAmountView.setText(globals.transaction.getFormattedCashAmount());        
    }
}
