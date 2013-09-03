package net.m_kawato.tabletpos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TopMenu extends Activity implements OnClickListener {
    private static final String TAG = "TopMenu";
    private Globals globals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_menu);

        // Initialization
        globals = (Globals) this.getApplication();
        globals.initialize();
        loadProductData();
        loadPlaceData();
        
        // "Order Input" button
        Button btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);

        // "About" button
        Button btnAbout = (Button) findViewById(R.id.btn_about);
        btnAbout.setOnClickListener(this);
    }
    
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btn_start:
                i = new Intent(this, Order.class);
                startActivity(i);
                break;
            case R.id.btn_about:
                // TODO define action for "About" button
                break;
        }        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    // load product data from product information file
    private void loadProductData() {
        Log.d(TAG, "loadProductData");
        AssetManager as = getResources().getAssets();
        List<String> lines = new ArrayList<String>();
        try {
            InputStream is = as.open(Globals.PRODUCTS_FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null){     
                lines.add(line);    
            } 
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to open product information file");
        }

        for(int i = 0; i < lines.size(); i++) {
            String[] lineSplit = lines.get(i).split("\\s*?,\\s*?");
            int productId = -1;
            try {
                productId = Integer.parseInt(lineSplit[1]);
            } catch (NumberFormatException e) {
                continue;
            }
            String category = lineSplit[2];
            String productName = lineSplit[4];
            BigDecimal unitPrice = new BigDecimal(lineSplit[5]);
            int numPiecesInBox = Integer.parseInt(lineSplit[6]);
            BigDecimal unitPriceBox = new BigDecimal(lineSplit[7]);

            Log.d(TAG, String.format("loadProductData: " +
                "productId=%d, category=%s, productName=%s, unitPrice=%f, numPircesInBox=%d, unitPriceBox=%f", 
                productId, category, productName, unitPrice, numPiecesInBox, unitPriceBox));
            Product p = new Product(this, productId, productName, category, unitPrice, unitPriceBox, numPiecesInBox);
            globals.products.add(p);
            globals.addCategory(category);
        }
    }

    // load route and place data from place information file
    private void loadPlaceData() {
        Log.d(TAG, "loadPlaceData");
        AssetManager as = getResources().getAssets();
        List<String> lines = new ArrayList<String>();
        try {
            InputStream is = as.open(Globals.PLACES_FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null){     
                lines.add(line);    
            } 
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to open product information file");
        }

        for(int i = 0; i < lines.size(); i++) {
            String[] lineSplit = lines.get(i).split("\\s*?,\\s*?");
            // skip comment line
            try {
                Integer.parseInt(lineSplit[0]);
            } catch (NumberFormatException e) {
                continue;
            }

            String placeCode = lineSplit[0];
            String placeName = lineSplit[1];
            String routeName = lineSplit[4];
            String routeCode = lineSplit[5];

            Log.d(TAG, String.format("loadPlacetData: " +
                "placeCode=%s, placeName=%s, routeCode=%s, routeName=%s", 
                placeCode, placeName, routeCode, routeName));
            globals.addPlace(routeCode, placeCode);
            globals.addRouteName(routeCode, routeName);
            globals.addPlaceName(placeCode, placeName);
        }
    }
}
