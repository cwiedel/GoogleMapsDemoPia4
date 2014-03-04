package se.christianwiedel.gmaps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by chrillan on 02/25/14.
 */

public class MyReceiver extends BroadcastReceiver{

    public static final String ACTION_GEOFENCE_TOAST = "se.christianwiedel.gmaps.GEOFENCE_TOAST";
    public MyReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            String action = intent.getAction();
            if(ACTION_GEOFENCE_TOAST.equals(action)) {
                Toast.makeText(context, "Your geofence was triggered!", Toast.LENGTH_SHORT).show();
                showNotification(context);
            }
        }
    }

    private void showNotification(Context context) {

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Gmaps Demo")
                        .setContentText("Geofence triggered!");
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }
}
