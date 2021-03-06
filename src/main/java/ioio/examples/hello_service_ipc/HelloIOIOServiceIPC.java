package ioio.examples.hello_service_ipc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

public class HelloIOIOServiceIPC extends IOIOService {

    final Messenger messenger = new Messenger(new IncomingHandler());

    public static final int IOIO_STATUS_REQUEST = 0;    //request IOIO Status
    public static final int IOIO_STATUS_REPLY   = 1;    //request IOIO Status
    public static final int ERROR_REPLY         = 2;    //TODO determine error details
    public static final int LED_ON_REQUEST      = 3;    //request turning on status LED
    public static final int LED_OFF_REQUEST     = 4;    //request turning off status LED
    public static final int LED_ON_REPLY        = 5;    //LED was turned on
    public static final int LED_OFF_REPLY       = 6;    //LED was turned off
    public static final int LED_STATUS_REQUEST  = 7;    //Status of LED request
    public static final int LED_STATUS_REPLY    = 8;    //arg1 == 1 if true

    public static boolean led_state = false;
    public static boolean ioio_state = false;

    //declare Intents for broadcast messages
    Intent setupIntent;
    Intent disconnectedIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        //create Intents for broadcast messages
        Log.d("KSM","Service onCreate");
        setupIntent = new Intent();
        setupIntent.setAction("IOIO_CONNECTED");

        disconnectedIntent = new Intent();
        disconnectedIntent.setAction("IOIO_DISCONNECTED");

    }


    static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Messenger rmsgr = msg.replyTo;
            Message rmsg;

            switch (msg.what) {
                case LED_ON_REQUEST:
                    Log.d("KSM", "LED_ON_REQUEST message handled");

                    if (!ioio_state){
                        rmsg = Message.obtain(null, ERROR_REPLY, msg.what, 0);
                        Log.d("KSM", "Sending reply message ERROR_REPLY ");
                    }else {
                        rmsg = Message.obtain(null, LED_ON_REPLY, 0, 0);
                        Log.d("KSM", "Sending reply message LED_ON_REPLY ");
                        led_state = true;
                    }

                    try {
                        rmsgr.send(rmsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;


                case LED_OFF_REQUEST:
                    Log.d("KSM","LED_OFF message handled");

                    if (!ioio_state){
                        rmsg = Message.obtain(null, ERROR_REPLY, msg.what, 0);
                        Log.d("KSM", "Sending reply message ERROR_REPLY ");
                    }else {
                        rmsg = Message.obtain(null, LED_OFF_REPLY, 0, 0);
                        Log.d("KSM", "Sending reply message LED_OFF_REQUEST");
                        led_state = false;
                    }

                    try {
                        rmsgr.send(rmsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;

                case LED_STATUS_REQUEST:
                    Log.d("KSM", "LED_STATUS_REQUEST message handled");

                    rmsg = Message.obtain(null, LED_STATUS_REPLY, led_state?1:0, 0);
                    Log.d("KSM", "Sending LED_STATUS_REPLY");
                    try {
                        rmsgr.send(rmsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;

                case IOIO_STATUS_REQUEST:
                    Log.d("KSM", "IOIO_STATUS_REQUEST message handled");

                    rmsg = Message.obtain(null, IOIO_STATUS_REPLY, ioio_state?1:0, 0);
                    Log.d("KSM", "Sending reply message IOIO_STATUS_REPLY");
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

    @Override
    protected IOIOLooper createIOIOLooper() {
        return new BaseIOIOLooper() {
            private DigitalOutput led_;

            @Override
            protected void setup() throws ConnectionLostException,
                    InterruptedException {
                ioio_state = true;
                led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);
                sendBroadcast(setupIntent);
            }

            @Override
            public void loop() throws ConnectionLostException, InterruptedException {
                led_.write(true);
                if(led_state) {
                    Thread.sleep(500);
                    led_.write(false);
                    Thread.sleep(500);
                }
            }

            @Override
            public void disconnected() {
                Log.d("KSM","IOIO Disconnect");
                ioio_state = false;
                sendBroadcast(disconnectedIntent);
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
                    R.drawable.ic_launcher, "IOIO IPC service running",
                    System.currentTimeMillis());
            notification
                    .setLatestEventInfo(this, "IOIO IPC Service", "Running",
                            PendingIntent.getService(this, 0, new Intent(
                                    "stop", null, this, this.getClass()), 0));
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            nm.notify(0, notification);
        }
        return result;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("KSM", "Service.onBind");
        return messenger.getBinder();
    }

}
