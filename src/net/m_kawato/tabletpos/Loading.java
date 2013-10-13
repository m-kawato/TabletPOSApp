package net.m_kawato.tabletpos;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

public class Loading extends Activity implements View.OnClickListener {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loading, menu);
        return true;
    }
    
    // Event handler for check boxes and GoToTop button
    @Override
    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
        case R.id.btn_gototop:
            i = new Intent(this, TopMenu.class);
            startActivity(i);
            break;
        case R.id.loading_checked:
            int position = (Integer) v.getTag();
            boolean checked = ((CheckBox) v).isChecked();
            Log.d(TAG, String.format("onClick: oder_checked position=%d, checked=%b", position, checked));
            break;
        }        
    }

}
