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

public class StockListAdapter extends BaseAdapter {
    private static final String TAG = "StockListAdapter";
    private Context context;
    private List<Product> productList;
    private TextView.OnEditorActionListener onEditorActionListener;
    private View.OnClickListener onClickListener;

    public StockListAdapter(Context context, List<Product> productList,
            TextView.OnEditorActionListener onEditorActionListener,
            View.OnClickListener onClickListener) {
        this.context = context;
        this.productList = productList;
        this.onEditorActionListener = onEditorActionListener;
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
            view = inflater.inflate(R.layout.stock_item, null);
        }
        Product p = this.productList.get(position);
        TextView productNameView = (TextView) view.findViewById(R.id.product_name);
        productNameView.setText(String.format("%d, %s", p.productId, p.productName));
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

        EditText stockView = (EditText) view.findViewById(R.id.stock);
        stockView.setText(Integer.toString(p.stock));
        stockView.setTag(position);
        stockView.setOnEditorActionListener(this.onEditorActionListener);
        Button buttonView = (Button) view.findViewById(R.id.btn_update_stock);
        buttonView.setOnClickListener(this.onClickListener);
        return view;
    }

}
