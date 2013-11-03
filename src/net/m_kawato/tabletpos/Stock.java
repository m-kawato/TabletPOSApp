package net.m_kawato.tabletpos;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Stock extends Activity implements TextView.OnEditorActionListener, View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG = "Stock";
    private Globals globals;
    private List<Product> stockList;
    private StockListAdapter stockListAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        this.globals = (Globals) this.getApplication();

        // Build ListView of stock items;
        //   products are sorted in categoryName - productName
        this.stockList = new ArrayList<Product>();
        for (String category: globals.categories) {
            for (Product p: globals.products) {
                if (p.category.equals(category) && p.loaded) {
                    this.stockList.add(p);
                }
            }
        }
        ListView stockListView = (ListView) findViewById(R.id.list);
        LayoutInflater inflater = this.getLayoutInflater();
        View header = inflater.inflate(R.layout.stock_header, null);
        View footer = inflater.inflate(R.layout.stock_footer, null);
        stockListView.addHeaderView(header);
        stockListView.addFooterView(footer);

        this.stockListAdapter = new StockListAdapter(this, this.stockList);
        stockListView.setAdapter(this.stockListAdapter);

        // Event handlers of buttons (Top Menu, Loading)
        Button btnTopMenu = (Button) findViewById(R.id.btn_topmenu);
        btnTopMenu.setOnClickListener(this);

        Button btnLoading = (Button) findViewById(R.id.btn_loading);
        btnLoading.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stock, menu);
        return true;
    }

    // Save stock values to SQLite database
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        globals.saveLoading();
        super.onDestroy();
    }
    
    // Event handler for navigation buttons
    @Override
    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
        case R.id.btn_update_stock:
            LinearLayout stockItemView = (LinearLayout) v.getParent();
            EditText stockView = (EditText) stockItemView.findViewById(R.id.stock);
            int position = (Integer) stockView.getTag();
            Log.d(TAG, "onclick-btn_update_stock: position = " + position);
            updateStock(stockView, position);
            break;
        case R.id.btn_topmenu:
            Log.d(TAG, "Top Menu button is clicked");
            globals.saveLoading();
            i = new Intent(this, TopMenu.class);
            startActivity(i);
            break;
        case R.id.btn_loading:
            Log.d(TAG, "Loading button is clicked");
            globals.saveLoading();
            i = new Intent(this, Loading.class);
            startActivity(i);
            break;
        }        
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d(TAG, "onEditorAction: actionId=" + actionId);
        if (v.getId() == R.id.stock && actionId == EditorInfo.IME_ACTION_GO) {
            int position = (Integer) v.getTag();
            updateStock((EditText) v, position);
        }
        return true;  
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.stock) {
            EditText stockView = (EditText) v;
            int position = (Integer) v.getTag();
            Log.d(TAG, String.format("onFocusChange: position=%d, hasFocus=%b",
                    position, hasFocus));
            Product p = this.stockList.get(position);
            if (hasFocus && p.stock == 0) {
                stockView.setText("");
            } else if (!hasFocus && p.stock == 0) {
                stockView.setText("0");                
            }
        }
    }

    // Update stock for product
    private void updateStock(EditText stockView, int position) {
        Log.d(TAG, String.format("updateStock: position = %d", position));
        String stockText = stockView.getText().toString();
        Product p = this.stockList.get(position);

        if (! stockText .equals("")) {
            p.stock = Integer.parseInt(stockText);
        }
        this.stockListAdapter.notifyDataSetChanged();
    }




}
