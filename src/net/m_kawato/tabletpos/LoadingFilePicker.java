package net.m_kawato.tabletpos;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class LoadingFilePicker extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_file_picker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loading_file_picker, menu);
        return true;
    }

}
