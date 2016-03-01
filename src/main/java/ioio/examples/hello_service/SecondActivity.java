package ioio.examples.hello_service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import ioio.lib.util.android.IOIOService;

public class SecondActivity extends Activity {
    boolean isBound = false;
    Messenger messenger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case HelloIOIOService.LED_ON_REPLY:
                    Toast.makeText(getApplicationContext(), "LED_ON_REPLY message handled", Toast.LENGTH_SHORT).show();
                    break;

                case HelloIOIOService.LED_OFF_REPLY:
                    Toast.makeText(getApplicationContext(), "LED_OFF_REPLY message handled", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void tglOnCLick(View v){
        ToggleButton tgl = (ToggleButton) v;
        int msgType;

        if(tgl.isChecked())
            msgType = HelloIOIOService.LED_ON_REQUEST;
        else
            msgType = HelloIOIOService.LED_OFF_REQUEST;

        Message msg = Message.obtain(null, msgType, 0, 0);
        msg.replyTo = new Messenger(new IncomingHandler());

        Toast.makeText(getApplicationContext(), "Toggle Message " + msgType + " sending...", Toast.LENGTH_SHORT).show();

        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void btnMainOnClick(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //to receive broadcasts from IOIO
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Intent Detected."+intent.getAction(), Toast.LENGTH_LONG).show();
        }
    }

}
