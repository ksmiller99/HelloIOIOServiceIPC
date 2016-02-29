package ioio.examples.hello_service;

import android.app.Activity;
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
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    boolean isBound = false;
    Messenger messenger = null;

    public static final int REPLY_MSG_ON = 1;
    public static final int REPLY_MSG_OFF = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, HelloIOIOService.class));
        setContentView(R.layout.main);
        //finish();
        Intent intent = new Intent(this, HelloIOIOService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(getApplicationContext(),"onCreate Finished",Toast.LENGTH_SHORT).show();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(getApplicationContext(),"onServiceConnection Started",Toast.LENGTH_SHORT).show();
            isBound = true;

            // Create the Messenger object
            messenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplicationContext(),"onServiceConnection Started",Toast.LENGTH_SHORT).show();

            // unbind or process might have crashes
            messenger = null;
            isBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(getApplicationContext(),"onStart Finished",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(getApplicationContext(),"onResume Finished",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop(){
        unbindService(serviceConnection);
        messenger = null;
        isBound = false;
        super.onStop();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case REPLY_MSG_ON:
                    Toast.makeText(getApplicationContext(), "REPLY_MSG_ON message handled", Toast.LENGTH_SHORT).show();

                    break;

                case REPLY_MSG_OFF:
                    Toast.makeText(getApplicationContext(), "REPLY_MSG_OFF message handled", Toast.LENGTH_SHORT).show();


                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void tglOnCLick(View v){
        ToggleButton tgl = (ToggleButton) v;
        int msgType;

        if(tgl.)
            msgType = HelloIOIOService.LED_OFF;
        else
            msgType = HelloIOIOService.LED_ON;

        Message msg = Message.obtain(null, msgType, 0, 0);
        msg.replyTo = new Messenger(new IncomingHandler());

        Toast.makeText(getApplicationContext(),"Toggle Message "+msgType+" sending...",Toast.LENGTH_SHORT).show();

        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

}