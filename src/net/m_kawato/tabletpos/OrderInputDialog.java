package net.m_kawato.tabletpos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;

public class OrderInputDialog extends Dialog implements View.OnClickListener {
    private final String TAG = "OrderInputDialog";
    private Product product;
    private int quantity;
    private int quantityBox;

    public OrderInputDialog(Context context, Product product) {
        super(context);
        this.product= product;
        this.quantity = product.quantity;
        this.quantityBox = product.quantityBox;
    }
        
    protected void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_dialog_order_input);
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.dialog_order_input);
    }
    
    @Override
    public void show() {
        super.show();

        TextView productNameView = (TextView) findViewById(R.id.product_name);
        productNameView.setText(this.product.productName);

        EditText quantityView = (EditText) findViewById(R.id.quantity);
        quantityView.setText(Integer.toString(this.product.quantity));
        quantityView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                Log.d(TAG, "beforeTextChanged: " + s + ", " + start + ", " + count + ", " + after);
            }
         
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + ", " + start + ", " + before + ", " + count);
            }
         
            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                Log.d(TAG, "afterTextChanged: " + s);
                if (s.length() > 0) {
                    OrderInputDialog.this.quantity = Integer.parseInt(s);
                }
            }
        });

        Button btnQuantityDown = (Button) findViewById(R.id.btn_quantity_down);
        btnQuantityDown.setOnClickListener(this);

        Button btnQuantityUp = (Button) findViewById(R.id.btn_quantity_up);
        btnQuantityUp.setOnClickListener(this);

        EditText quantityBoxView = (EditText) findViewById(R.id.quantity_box);
        quantityBoxView.setText(Integer.toString(this.product.quantityBox));
        quantityBoxView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                Log.d(TAG, "beforeTextChanged: " + s + ", " + start + ", " + count + ", " + after);
            }
         
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + ", " + start + ", " + before + ", " + count);
            }
         
            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                Log.d(TAG, "afterTextChanged: " + s);
                if (s.length() > 0) {
                    OrderInputDialog.this.quantityBox = Integer.parseInt(s);
                }
            }
        });
        
        Button btnQuantityBoxDown = (Button) findViewById(R.id.btn_quantity_box_down);
        btnQuantityBoxDown.setOnClickListener(this);
        
        Button btnQuantityBoxUp = (Button) findViewById(R.id.btn_quantity_box_up);
        btnQuantityBoxUp.setOnClickListener(this);

        Button btnOk = (Button) findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
    }

    
    private void saveOrder() {
        Log.d(TAG, "saveOrder");
        this.product.quantity = this.quantity;
        this.product.quantityBox = this.quantityBox;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        EditText edittext;

        switch(v.getId()) {
        case R.id.btn_quantity_down:
            if (this.quantity > 0) {
                edittext = (EditText) findViewById(R.id.quantity);
                edittext.setText(Integer.toString(this.quantity - 1));
            }
            break;
        case R.id.btn_quantity_up:
            edittext = (EditText) findViewById(R.id.quantity);
            edittext.setText(Integer.toString(this.quantity + 1));
            break;
        case R.id.btn_quantity_box_down:
            if (this.quantityBox > 0) {
                edittext = (EditText) findViewById(R.id.quantity_box);
                edittext.setText(Integer.toString(this.quantityBox - 1));
            }
            break;
        case R.id.btn_quantity_box_up:
            edittext = (EditText) findViewById(R.id.quantity_box);
            edittext.setText(Integer.toString(this.quantityBox + 1));
            break;
        case R.id.btn_ok:
            saveOrder();
            this.dismiss();
            break;
        case R.id.btn_cancel:
            this.dismiss();
            break;
        }
    }

}
