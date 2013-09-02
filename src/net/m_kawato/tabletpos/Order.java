package net.m_kawato.tabletpos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

public class Order extends Activity implements OnClickListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, DialogInterface.OnDismissListener {
    private static final String TAG = "Order";
    private Globals globals;
    private List<Product> productsInCategory;
    private List<Map<String, Object>> productList;
    private int selectedPosition;
    private SimpleAdapter productListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        globals = (Globals) this.getApplication();
        this.productsInCategory = new ArrayList<Product>();
        this.productList = new ArrayList<Map<String, Object>>();
        Log.d(TAG, "onCreate: products.size() = " + globals.products.size());
        
        // Build ListView for products
        String[] from = {"image", "product_name", "unit_price", "quantity", "unit_price_box", "quantity_box", "amount"};
        int[] to = {R.id.image, R.id.product_name, R.id.unit_price, R.id.quantity, R.id.unit_price_box, R.id.quantity_box, R.id.amount};
        this.productListAdapter = new SimpleAdapter(this, this.productList, R.layout.order_item, from, to);

        ListView productListView = (ListView) findViewById(R.id.list);
        LayoutInflater inflater = this.getLayoutInflater();
        View header = inflater.inflate(R.layout.order_header, null);
        View footer = inflater.inflate(R.layout.order_footer, null);
        productListView.addHeaderView(header);
        productListView.addFooterView(footer);
        productListView.setAdapter(this.productListAdapter);
        productListView.setOnItemClickListener(this);
        
        // Spinner for route selection
        ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String route: globals.routes) {
            routeAdapter.add(route);
        }
        Spinner spnRoute = (Spinner) findViewById(R.id.spn_route);
        spnRoute.setAdapter(routeAdapter);
        spnRoute.setSelection(globals.selectedRoute);
        spnRoute.setOnItemSelectedListener(this);

        // Spinner for place selection
        ArrayAdapter<String> placeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        placeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String place: globals.places) {
            placeAdapter.add(place);
        }
        Spinner spnPlace = (Spinner) findViewById(R.id.spn_place);
        spnPlace.setAdapter(placeAdapter);
        spnPlace.setSelection(globals.selectedPlace);
        spnPlace.setOnItemSelectedListener(this);

        // Spinner for category selection
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(String category: globals.categories) {
            categoryAdapter.add(category);
        }
        Spinner spnCategory = (Spinner) findViewById(R.id.spn_category);
        spnCategory.setAdapter(categoryAdapter);    
        spnCategory.setOnItemSelectedListener(this);

        // "Confirm" button
        Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.order, menu);
        return true;
    }

    // Event handler for spinners
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, String.format("onItemSelected: id=%x, position=%d", parent.getId(), position));
        switch (parent.getId()) {
        case R.id.spn_route:
            globals.selectedRoute = position;
            break;
        case R.id.spn_place:
            globals.selectedPlace = position;
            break;
        case R.id.spn_category:
            changeCategory(globals.categories.get(position));
            break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected");
    }

    // Event handler for "Confirm" button
    @Override
    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
        case R.id.btn_confirm:
            i = new Intent(this, Confirm.class);
            startActivity(i);
            break;
        }        
    }

    // Event handler for items of product list
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position--; // compensate for position increase when header is inserted
        Log.d(TAG, "onItemClick: position = " + position);
        Product product = this.productsInCategory.get(position);
        Log.d(TAG, "onItemClick: productId = " + product.productId + ", productName = " + product.productName);
        this.selectedPosition = position;
        OrderInputDialog dialog = new OrderInputDialog(this, product);
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    // Event listener for order input dialog
    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(TAG, "onDismiss");
        Product product = this.productsInCategory.get(this.selectedPosition);
        this.productList.set(this.selectedPosition, product.toMap());
        this.productListAdapter.notifyDataSetChanged();
    }

    // Change selected category of products
    private void changeCategory(String category) {
        Log.d(TAG, String.format("changeCategory: %s", category));
        this.productsInCategory.clear();
        this.productList.clear();        
        Log.d(TAG, String.format("changeCategory: productLise.size = %d", this.productList.size()));
        for(Product product: globals.products) {
            if (! product.category.equals(category)) {
                continue;
            }
            this.productsInCategory.add(product);
            this.productList.add(product.toMap());
        }
        this.productListAdapter.notifyDataSetChanged();
    }

}
