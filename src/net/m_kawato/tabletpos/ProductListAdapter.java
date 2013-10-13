package net.m_kawato.tabletpos;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ProductListAdapter extends BaseAdapter {
    private static final String TAG = "OrderItemAdapter";
    private Context context;
    private List<Product> productList;
    private View.OnClickListener onClickListener;
    
    public ProductListAdapter(Context context, List<Product> productList, View.OnClickListener onClickListener) {
        this.context = context;
        this.productList = productList;
        this.onClickListener = onClickListener;
    }  

    @Override
    public int getCount() {
        return this.productList.size();
    }
    
    @Override
    public Object getItem(int position) {
        return this.productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView");
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.order_item, null);
        }
        Product product = this.productList.get(position);
        // ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView productNameView = (TextView) view.findViewById(R.id.product_name);
        productNameView.setText(product.productName);
        TextView unitPriceView = (TextView) view.findViewById(R.id.unit_price);
        unitPriceView.setText(product.unitPrice.toString());
        TextView unitPriceBoxView = (TextView) view.findViewById(R.id.unit_price_box);
        unitPriceBoxView.setText(product.unitPriceBox.toString());
        CheckBox checkBox = (CheckBox)  view.findViewById(R.id.order_checked);
        checkBox.setTag(position);
        checkBox.setChecked(!(product.orderItem == null));
        checkBox.setOnClickListener(this.onClickListener);
        return view;
    }
}
