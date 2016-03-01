package ioio.examples.hello_service;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    public void btnMainOnClick(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
