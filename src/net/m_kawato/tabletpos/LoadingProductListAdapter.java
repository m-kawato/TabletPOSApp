package net.m_kawato.tabletpos;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadingProductListAdapter extends BaseAdapter {
    private static final String TAG = "LoadingProductListAdapter";
    private Context context;
    private List<Product> productList;
    private View.OnClickListener onClickListener;
    
    public LoadingProductListAdapter(Context context, List<Product> productList, View.OnClickListener onClickListener) {
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
        Log.d(TAG, String.format("getView: position=%d", position));
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.loading_item, null);
        }
        Product product = this.productList.get(position);

        String imgFilePath =
                String.format("%s/%s/%s-%d.jpg",
                Globals.SDCARD_DIRNAME,
                Globals.IMAGE_DIRNAME,
                Globals.PRODUCT_IMAGE_PREFIX,
                product.productId);
        File imgFile = new File(Environment.getExternalStorageDirectory(), imgFilePath);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        if (imgFile.canRead()) {
            Log.d(TAG, "image file = " + imgFile.toString());
            Bitmap bm = BitmapFactory.decodeFile(imgFile.getPath());
            imageView.setImageBitmap(bm);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.INVISIBLE);
        }
        
        TextView productNameView = (TextView) view.findViewById(R.id.product_name);
        productNameView.setText(String.format("%d, %s", product.productId, product.productName));

        CheckBox checkBox = (CheckBox)  view.findViewById(R.id.loading_checked);
        checkBox.setTag(position);
        checkBox.setChecked(product.loaded);
        checkBox.setOnClickListener(this.onClickListener);
        return view;
    }
}
