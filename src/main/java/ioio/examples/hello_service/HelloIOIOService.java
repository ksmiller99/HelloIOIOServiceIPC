package ioio.examples.hello_service;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 * An example IOIO service. While this service is alive, it will attempt to
 * connect to a IOIO and blink the LED. A notification will appear on the
 * notification bar, enabling the user to stop the service.
 */
public class HelloIOIOService extends IOIOService {

    private static final String TAG = "IOIOService";

    // Used to receive messages from the Activity
    final Messenger inMessenger = new Messenger(new IncomingHandler());
    // Use to send message to the Activity
    private Messenger outMessenger;


    public static final int LED_ON = 1;
    public static final int LED_OFF = 2;

    public static int led_state = LED_ON;

    private boolean led_changed = true;

    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(),"Service.onCreate Finished",Toast.LENGTH_SHORT).show();


    }


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Messenger rmsgr = msg.replyTo;
            Message rmsg;

            switch (msg.what) {
                case LED_ON:
                    Toast.makeText(getApplicationContext(), "LED_ON message handled", Toast.LENGTH_SHORT).show();

                    rmsg = Message.obtain(null,LED_OFF, 0, 0);
                    Toast.makeText(getApplicationContext(), "Sending reply message", Toast.LENGTH_SHORT).show();
                    try {
                        rmsgr.send(rmsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;


                case LED_OFF:
                    Toast.makeText(getApplicationContext(), "LED_OFF message handled", Toast.LENGTH_SHORT).show();

                    rmsg = Message.obtain(null, LED_ON, 0, 0);
                    Toast.makeText(getApplicationContext(), "Sending reply message", Toast.LENGTH_SHORT).show();
                    try {
                        rmsgr.send(rmsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    protected IOIOLooper createIOIOLooper() {
        return new BaseIOIOLooper() {
            private DigitalOutput led_;

            @Override
            protected void setup() throws ConnectionLostException,
                    InterruptedException {
                led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);
            }

            @Override
            public void loop() throws ConnectionLostException,
                    InterruptedException {
                led_.write(false);
                Thread.sleep(500);
                led_.write(true);
                Thread.sleep(500);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int result = super.onStartCommand(intent, flags, startId);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (intent != null && intent.getAction() != null
                && intent.getAction().equals("stop")) {
            // User clicked the notification. Need to stop the service.
            nm.cancel(0);
            stopSelf();
        } else {
            // Service starting. Create a notification.
            Notification notification = new Notification(
                    R.drawable.ic_launcher, "IOIO service running",
                    System.currentTimeMillis());
            notification
                    .setLatestEventInfo(this, "IOIO Service", "Click to stop",
                            PendingIntent.getService(this, 0, new Intent(
                                    "stop", null, this, this.getClass()), 0));
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            nm.notify(0, notification);
        }
        return result;
    }

    public IBinder onBind(Intent intent) {
        Log.d("KSM", "CD_IOIOService.onBind");
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_LONG).show();
        return mMessenger.getBinder();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Toast.makeText(getApplicationContext(),"Service.onStart Finished",Toast.LENGTH_SHORT).show();

    }
}
