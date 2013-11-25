package net.m_kawato.tabletpos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReceiptHelper {
    private static final String TAG = "ReceiptHelper";
    private Context context;
    private Globals globals;

    public ReceiptHelper(Context context, Globals globals) {
        this.context = context;
        this.globals = globals;
    }

    // get list of transaction information from receipt file 
    public List<Transaction> getPastTransactionList() {
        // Read past transaction data from receipt file
        List<String> lines = new ArrayList<String>();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(globals.getReceiptFile());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String line;
            while((line = reader.readLine()) != null){     
                lines.add(line);    
            } 
            fin.close();
        } catch (IOException e) {
            Log.e(TAG, "failed to read receipt from sdcard");
            e.printStackTrace();
            return null;
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                }
            }
        }  

        Map<String, Transaction> transactionMap = new HashMap<String, Transaction>();
        List<Transaction> transactionList = new ArrayList<Transaction>();

        for(int i = 0; i < lines.size(); i++) {
            String[] lineSplit = lines.get(i).split("\\s*,\\s*");
            if (lineSplit.length != 10) {
                Log.d(TAG, String.format("line skipped: %s, length=%d", lines.get(i), lineSplit.length));
                continue;
            }
            try {
                String timestamp = lineSplit[0];
                String routeCode = lineSplit[2];
                String placeCode = lineSplit[4];
                int productId = Integer.parseInt(lineSplit[7]);
                int quantity = Integer.parseInt(lineSplit[8]);
                BigDecimal creditAmount = new BigDecimal(lineSplit[5]);
                Log.d(TAG, String.format("buildPastOrderList: timestamp=%s,routeCode=%s,placeCode=%s,productId=%d,quantity=%d,creditAmount=%s",
                        timestamp, routeCode, placeCode, productId, quantity, creditAmount.toString()));
                OrderItem orderItem = new OrderItem(this.context, globals.getProduct(productId), quantity);
                Transaction transaction = transactionMap.get(String.format("%s,%s,%s", timestamp, routeCode, placeCode));
                if (transaction == null) {
                    transaction = new Transaction(this.context, routeCode, placeCode, creditAmount);
                    transactionMap.put(String.format("%s,%s,%s", timestamp, routeCode, placeCode), transaction);
                    transactionList.add(transaction);
                }
                transaction.creditAmount = transaction.creditAmount.add(creditAmount);
                transaction.addOrderItem(orderItem);
            } catch (NumberFormatException e) {
                Log.d(TAG, "line skipped: " + lines.get(i));
                continue;
            }            
        }
        Collections.reverse(transactionList);
        
        // Sort order items for each transaction
        for(Transaction t: transactionList) {
            t.sortOrderItems();
        }

        return transactionList;
    }

    // build View of receipt
    public void buildReceipView(LinearLayout placeHolder, Transaction transaction,
            LayoutInflater inflater, int headerView, int tag) {

        // receipt header (route, place, edit button)

        View receiptHeaderView = inflater.inflate(headerView, null);
        placeHolder.addView(receiptHeaderView);

        String routeName = globals.routeName.get(transaction.routeCode);
        TextView routeView = (TextView) receiptHeaderView.findViewById(R.id.route);
        routeView.setText(String.format("%s (%s)", routeName,
                transaction.routeCode));

        String placeName = globals.placeName.get(transaction.placeCode);
        TextView placeView = (TextView) receiptHeaderView.findViewById(R.id.place);
        placeView.setText(String.format("%s (%s)", placeName, transaction.placeCode));

        if (headerView == R.layout.receiptedit_header) {
            Button btnEdit = (Button) receiptHeaderView.findViewById(R.id.btn_edit);
            btnEdit.setOnClickListener((ReceiptEdit) this.context);
            btnEdit.setTag(tag);
        }

        // order items

        boolean firstItem = true;
        for (OrderItem orderItem : transaction.orderItems) {
            Product p = orderItem.product;
            if (firstItem) {
                firstItem = false;
            } else {
                View itemBorderLine = inflater.inflate(R.layout.item_separator,
                        new FrameLayout(this.context));
                placeHolder.addView(itemBorderLine);
            }

            LinearLayout receiptItem = (LinearLayout) inflater.inflate(R.layout.receipt_item, null);
            placeHolder.addView(receiptItem);

            TextView productNameView = (TextView) receiptItem.findViewById(R.id.product_name);
            productNameView.setText(p.productName);

            TextView unitPriceView = (TextView) receiptItem.findViewById(R.id.unit_price);
            unitPriceView.setText(p.getFormattedUnitPrice());

            TextView unitPriceBoxView = (TextView) receiptItem.findViewById(R.id.unit_price_box);
            unitPriceBoxView.setText(p.getFormattedUnitPriceBox());

            TextView quantityView = (TextView) receiptItem.findViewById(R.id.quantity);
            quantityView.setText(Integer.toString(orderItem.quantity));

            TextView amountView = (TextView) receiptItem.findViewById(R.id.amount);
            amountView.setText(orderItem.getFormattedAmount());
        }

        // footer (total amount, credit amount and cash amount)
        
        LinearLayout receiptFooter = (LinearLayout) inflater.inflate(R.layout.receipt_footer, null);
        placeHolder.addView(receiptFooter);

        TextView totalAmountView = (TextView) receiptFooter.findViewById(R.id.total_amount);
        totalAmountView.setText(transaction.getFormattedTotalAmount());

        TextView creditAmountView = (TextView) receiptFooter.findViewById(R.id.credit_amount);
        creditAmountView.setText(transaction.getFormattedCreditAmount());

        TextView cashAmountView = (TextView) receiptFooter.findViewById(R.id.cash_amount);
        cashAmountView.setText(transaction.getFormattedCashAmount());
    }

    public void buildReceipView(LinearLayout placeHolder, Transaction transaction,
            LayoutInflater inflater, int headerView) {
        buildReceipView(placeHolder, transaction, inflater, headerView, 0);
    }

}
