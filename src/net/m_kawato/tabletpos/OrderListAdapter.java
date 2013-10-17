package net.m_kawato.tabletpos;

import java.io.File;
import java.util.List;

import android.R.drawable;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class OrderListAdapter extends BaseAdapter {
    private static final String TAG = "OrderListAdapter";
    private Context context;
    private List<OrderItem> orderItemList;
    private TextView.OnEditorActionListener onEditorActionListener;
    private View.OnClickListener onClickListener;
    
    public OrderListAdapter(Context context, List<OrderItem> orderItemList,
            TextView.OnEditorActionListener onEditorActionListener,
            View.OnClickListener onClickListener) {
        this.context = context;
        this.orderItemList = orderItemList;
        this.onEditorActionListener = onEditorActionListener;
        this.onClickListener = onClickListener;
    }  

    @Override
    public int getCount() {
        return this.orderItemList.size();
    }
    
    @Override
    public Object getItem(int position) {
        return this.orderItemList.get(position);
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
            view = inflater.inflate(R.layout.confirm_item, null);
        }
        OrderItem orderItem = this.orderItemList.get(position);
        Product p = orderItem.product;
        TextView productNameView = (TextView) view.findViewById(R.id.product_name);
        productNameView.setText(p.productName);
        String imgFilePath =
                String.format("%s/%s/%s-%d.jpg",
                Globals.SDCARD_DIRNAME,
                Globals.IMAGE_DIRNAME,
                Globals.PRODUCT_IMAGE_PREFIX,
                p.productId);
        File imgFile = new File(Environment.getExternalStorageDirectory(), imgFilePath);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        if (imgFile.canRead()) {
            Log.d(TAG, "image file = " + imgFile.toString());
            Bitmap bm = BitmapFactory.decodeFile(imgFile.getPath());
            imageView.setImageBitmap(bm);
        } else {
            imageView.setImageResource(drawable.ic_menu_gallery);
        }

        TextView unitPriceView = (TextView) view.findViewById(R.id.unit_price);
        unitPriceView.setText(p.getFormattedUnitPrice());
        TextView unitPriceBoxView = (TextView) view.findViewById(R.id.unit_price_box);
        unitPriceBoxView.setText(p.getFormattedUnitPriceBox());
        TextView stockView = (TextView) view.findViewById(R.id.stock);
        stockView.setText(Integer.toString(p.stock));
        EditText quantityView = (EditText) view.findViewById(R.id.quantity);
        quantityView.setText(Integer.toString(orderItem.quantity));
        quantityView.setTag(position);
        quantityView.setOnEditorActionListener(this.onEditorActionListener);
        Button buttonView = (Button) view.findViewById(R.id.btn_update_quantity);
        buttonView.setOnClickListener(this.onClickListener);
        TextView amountView = (TextView) view.findViewById(R.id.amount);
        amountView.setText(orderItem.getFormattedAmount());
        return view;
    }
}
