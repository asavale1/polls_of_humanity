package pollsofhumanity.hardikar.com.pollsofhumanity.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import pollsofhumanity.hardikar.com.pollsofhumanity.BaseActivity;
import pollsofhumanity.hardikar.com.pollsofhumanity.ManageSharedPref;
import pollsofhumanity.hardikar.com.pollsofhumanity.R;

/**
 *
 */
public class ResultsAlarmReceiver extends BroadcastReceiver {
    ManageSharedPref manageSharedPref;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Results alarm received");

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manageSharedPref = new ManageSharedPref(context);
        if(manageSharedPref.getId() != -1){
            mNotificationManager.notify(
                    0,
                    createNotification(context)
            );
        }

    }

    public Notification createNotification(Context context){

        manageSharedPref.setResultsId(manageSharedPref.getId());

        System.out.println("Result notification created");
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
        nb.setContentTitle("Results ready");
        nb.setAutoCancel(true);
        nb.setSmallIcon(R.drawable.oval_button_yes);

        Intent resultIntent = new Intent(context, BaseActivity.class);
        resultIntent.putExtra("question_id", manageSharedPref.getResultsId());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);


        PendingIntent pi = PendingIntent.getActivity(context, 0, resultIntent, 0);
        nb.setContentIntent(pi);

        return nb.build();

    }
}