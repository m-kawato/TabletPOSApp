package net.m_kawato.tabletpos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        globals.loadLoadingSheetNumber();
        globals.loadTransactionId();
        loadProductData();
        loadPlaceData();
        
        // Buttons (Enter Order, Enter Loading, About)
        Button btnEnterOrder = (Button) findViewById(R.id.btn_enterorder);
        btnEnterOrder.setOnClickListener(this);

        Button btnLoading = (Button) findViewById(R.id.btn_loading);
        btnLoading.setOnClickListener(this);

        // "About" button
        Button btnAbout = (Button) findViewById(R.id.btn_about);
        btnAbout.setOnClickListener(this);
    }
    
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btn_enterorder:
                // increment transactionId and go to Order page
                globals.incrTransactionId();
                i = new Intent(this, Order.class);
                startActivity(i);
                break;
            case R.id.btn_loading:
                i = new Intent(this, Loading.class);
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
        if (globals.categories != null && globals.categories.size() != 0) {
            return;
        }
        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        String dirname = String.format("%s/%s", sdcardPath, Globals.SDCARD_DIRNAME);
        String productsFilename = String.format("%s/%s", dirname, Globals.PRODUCTS_FILENAME);
        globals.categories = new ArrayList<String>();
        List<String> lines = new ArrayList<String>();
        try {
            File productsFile = new File(productsFilename);
            FileInputStream fin = new FileInputStream(productsFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String line;
            while((line = reader.readLine()) != null){     
                lines.add(line);    
            } 
            fin.close();
        } catch (IOException e) {
            Log.e(TAG, "!! Failed to load product data");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setTitle("Fatal Error");
            alertDialogBuilder.setMessage("Failed to load product data: \n" + productsFilename);
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TopMenu.this.finish();
                        }
                    });
            alertDialogBuilder.create().show();
            Log.d(TAG, "after alertDialog(product)");
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
            globals.addProduct(p);
            globals.addCategory(category);
        }

        // Sort categories and products in alphabetical order
        Collections.sort(globals.categories, new Comparator<String>() {
            @Override
            public int compare(String category1, String category2) {
                return category1.compareToIgnoreCase(category2);
            }
        });
        Collections.sort(globals.products, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return p1.productName.compareToIgnoreCase(p2.productName);
            }
        });
        
        // load loading/stock states from SQLite3 database
        PosDbHelper dbHelper = new PosDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT product_id,stock FROM loading WHERE loaded = 1", null);
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            int productId = c.getInt(0);
            int stock = c.getInt(1);
            Product p = globals.getProduct(productId);
            p.loaded = true;
            p.stock = stock;
            c.moveToNext();
        }
        c.close();
    }

    // load route and place data from place information file
    private void loadPlaceData() {
        Log.d(TAG, "loadPlaceData");
        if (globals.routes != null && globals.routes.size() != 0) {
            return;
        }
        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        String dirname = String.format("%s/%s", sdcardPath, Globals.SDCARD_DIRNAME);
        String placesFilename = String.format("%s/%s", dirname, Globals.PLACES_FILENAME);
        globals.routes = new ArrayList<String>(); 
        globals.places = new HashMap<String, List<String>>();
        globals.routeName = new HashMap<String, String>();
        globals.placeName = new HashMap<String, String>();
        List<String> lines = new ArrayList<String>();
        try {
            File placesFile = new File(placesFilename);
            FileInputStream fin = new FileInputStream(placesFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String line;
            while((line = reader.readLine()) != null){     
                lines.add(line);    
            } 
            fin.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to load place data");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setTitle("Fatal Error");
            alertDialogBuilder.setMessage("Failed to load place data: \n" + placesFilename);
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TopMenu.this.finish();
                        }
                    });
            alertDialogBuilder.create().show();
            Log.d(TAG, "after alertDialog(place)");
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

        // Sort routes and places in alphabetical order
        Collections.sort(globals.routes, new Comparator<String>() {
            @Override
            public int compare(String routeCode1, String routeCode2) {
                String routeName1 = globals.routeName.get(routeCode1);
                String routeName2 = globals.routeName.get(routeCode2);
                return routeName1.compareToIgnoreCase(routeName2);
            }
        });
        
        for (String routeCode: globals.routes) {
            List<String> places = globals.places.get(routeCode);
            Collections.sort(places, new Comparator<String>() {
                @Override
                public int compare(String placeCode1, String placeCode2) {
                    String placeName1 = globals.placeName.get(placeCode1);
                    String placeName2 = globals.placeName.get(placeCode2);
                    return placeName1.compareToIgnoreCase(placeName2);
                }
                
            });
        }
    }
}
