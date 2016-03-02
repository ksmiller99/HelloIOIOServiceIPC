package ioio.examples.hello_service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    private TextView textView_;
    private SeekBar seekBar_;
    private ToggleButton toggleButton_;

    boolean isBound = false;
    Messenger messenger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, HelloIOIOService.class));
        setContentView(R.layout.main);

        textView_ = (TextView) findViewById(R.id.TextView);
        seekBar_ = (SeekBar) findViewById(R.id.SeekBar);
        toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);

        //enableUi(false);
        //finish();
        Intent intent = new Intent(this, HelloIOIOService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(getApplicationContext(),"onCreate Finished",Toast.LENGTH_SHORT).show();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(getApplicationContext(),"Main.onServiceConnected",Toast.LENGTH_SHORT).show();
            isBound = true;

            // Create the Messenger object
            messenger = new Messenger(service);

            //TODO enable UI
            Message msg = Message.obtain(null, HelloIOIOService.IOIO_STATUS_REQUEST);
            msg.replyTo = new Messenger(new IncomingHandler());
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplicationContext(),"main.onServiceDisconnect",Toast.LENGTH_SHORT).show();

            // unbind or process might have crashes
            messenger = null;
            isBound = false;
            //TODO disble UI
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(getApplicationContext(),"onStart Finished",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        unbindService(serviceConnection);
        messenger = null;
        isBound = false;

        super.onDestroy();
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

                case HelloIOIOService.IOIO_STATUS_REPLY:
                    Toast.makeText(getApplicationContext(), "IOIO_STATUS_REPLY message handled", Toast.LENGTH_SHORT).show();
                    //enableUi((msg.arg1 == 1) ? true : false);
                    break;

                default:
                    Toast.makeText(getApplicationContext(), "UNKNOWN MESSAGE TYPE: "+msg.what, Toast.LENGTH_SHORT).show();
                    super.handleMessage(msg);
            }
        }
    }

    public void tglOnCLick(View v){
        Toast.makeText(getApplicationContext(),"Toggle Button pressed.",Toast.LENGTH_SHORT).show();
        ToggleButton tgl = (ToggleButton) v;
        int msgType;

        if(tgl.isChecked())
            msgType = HelloIOIOService.LED_ON_REQUEST;
        else
            msgType = HelloIOIOService.LED_OFF_REQUEST;

        Message msg = Message.obtain(null, msgType, 0, 0);
        msg.replyTo = new Messenger(new IncomingHandler());

        Toast.makeText(getApplicationContext(),"Toggle Message "+msgType+" sending...",Toast.LENGTH_SHORT).show();

        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void btnSecondOnClick(View v){
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    //to receive broadcasts from IOIO
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
            if(intent.getAction()=="com.examples.hello_service.IOIO_DISCONNECTED"){
                //enableUi(false);
            }else if(intent.getAction()=="com.examples.hello_service.IOIO_CONNECTED") {
                enableUi(true);
            }
        }
    }

    private void enableUi(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar_.setEnabled(enable);
                toggleButton_.setEnabled(enable);
            }
        });
    }
}


