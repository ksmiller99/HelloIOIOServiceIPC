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
import android.widget.Toast;

public class MainActivity extends Activity {
    boolean isBound = false;
    Messenger mMessenger;

    public static final int REPLY_MSG = 1;

    final Messenger rMessenger = new Messenger(new IncomingHandler());

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
            mMessenger = new Messenger(service);

            // Create a Message
            // Note the usage of MSG_SAY_HELLO as the what value
            Message msg = Message.obtain(null, HelloIOIOService.LED_ON, 0, 0);
            msg.replyTo = rMessenger;


            // Create a bundle with the data
            Bundle bundle = new Bundle();
            bundle.putString("hello", "world");

            // Set the bundle data to the Message
            msg.setData(bundle);

            // Send the Message to the Service (in another process)
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplicationContext(),"onServiceConnection Started",Toast.LENGTH_SHORT).show();

            // unbind or process might have crashes
            mMessenger = null;
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

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case REPLY_MSG:
                    Toast.makeText(getApplicationContext(), "REPLY_MSG message handled", Toast.LENGTH_SHORT).show();


                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}