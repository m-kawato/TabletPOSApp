package net.m_kawato.tabletpos;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;

public class Loading extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "Loading";
    private Globals globals;
    private List<Product> productsInCategory; // products in the selected category
    private LoadingProductListAdapter productListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        globals = (Globals) this.getApplication();
        this.productsInCategory = new ArrayList<Product>();

        // Build ListView for products
        this.productListAdapter = new LoadingProductListAdapter(this, this.productsInCategory, this);

        ListView productListView = (ListView) findViewById(R.id.list);
        LayoutInflater inflater = this.getLayoutInflater();
        View header = inflater.inflate(R.layout.loading_header, null);
        View footer = inflater.inflate(R.layout.loading_footer, null);
        productListView.addHeaderView(header);
        productListView.addFooterView(footer);
        productListView.setAdapter(this.productListAdapter);

        // Spinner for category selection
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(String category: globals.categories) {
            categoryAdapter.add(category);
        }
        Spinner spnCategory = (Spinner) findViewById(R.id.spn_category);
        spnCategory.setAdapter(categoryAdapter);    
        spnCategory.setOnItemSelectedListener(this);
        
        // Event handlers of buttons (Clear All, Top Menu)
        Button btnClearAll = (Button) findViewById(R.id.btn_clear_all);
        btnClearAll.setOnClickListener(this);

        Button btnTopMenu = (Button) findViewById(R.id.btn_topmenu);
        btnTopMenu.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loading, menu);
        return true;
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        saveLoadingState();
        super.onDestroy();
    }

    // Event handler for check boxes and GoToTop button
    @Override
    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
        case R.id.btn_clear_all:
            Log.d(TAG, "Clear All button is clicked");
            for (Product p: globals.products) {
                p.loaded = false;
            }
            this.productListAdapter.notifyDataSetChanged();
            break;
        case R.id.btn_topmenu:
            Log.d(TAG, "Top Menu button is clicked");
            saveLoadingState();
            i = new Intent(this, TopMenu.class);
            startActivity(i);
            break;
        case R.id.loading_checked:
            int position = (Integer) v.getTag();
            boolean checked = ((CheckBox) v).isChecked();
            Log.d(TAG, String.format("onClick: loading_checked position=%d, checked=%b", position, checked));
            Product p = this.productsInCategory.get(position);
            p.loaded = checked;
            break;
        }        
    }

    // Event handler for category selector
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, String.format("onItemSelected: id=%x, position=%d", parent.getId(), position));
        switch (parent.getId()) {
        case R.id.spn_category:
            changeCategory(globals.categories.get(position));
            break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected");
    }

    // Change selected category of products
    private void changeCategory(String category) {
        Log.d(TAG, String.format("changeCategory: %s", category));
        this.productsInCategory.clear();
        for (Product product: globals.products) {
            if (! product.category.equals(category)) {
                continue;
            }
            this.productsInCategory.add(product);
        }
        this.productListAdapter.notifyDataSetChanged();
    }

    // Save loading state to SQLite3 database
    private void saveLoadingState() {
        // TODO implementation of this method
        Log.d(TAG, "saveLoadingState");

        PosDbHelper dbHelper = new PosDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL("DELETE FROM loading");
        String insertLoading = "INSERT INTO loading (product_id, loaded) VALUES (?, ?)";
        for (Product p: globals.products) {
            if (p.loaded) {
                db.execSQL(insertLoading, new String[] {Integer.toString(p.productId), "1"});
            }
        }
    }
}
