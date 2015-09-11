package com.vofka.simsimgcm;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by ${vofka} on ${12/09/2014}.
 */

public class ImageCenterCrop extends Activity {
    private TextView mHelloTextView;
    private EditText mNameEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.croped_vew);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
  //      if (id == R.id.action_settings) {
  //          return true;
  //      }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {

        if(mNameEditText.getText().length()==0){
            mHelloTextView.setText("Hello Kitty");
        }else{
            mHelloTextView.setText("привет, "+mNameEditText.getText());
        }

    }
}