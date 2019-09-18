package sa.home.projects.emchat.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import sa.home.projects.emchat.Activities.ProfileActivity;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.Consts;

import static sa.home.projects.emchat.EmChat.CHANNEL_ID;


public class FirebaseMessagingService
        extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        String clickAction = remoteMessage.getNotification().getClickAction();

        Map<String, String> extraData = remoteMessage.getData();
        String whoseActivityToOpen = extraData.get("whose_profile_to_open");

        Intent clickIntent = new Intent(clickAction);

        if (whoseActivityToOpen != null) {
            clickIntent.putExtra(Consts.CURRENT_UID, whoseActivityToOpen);
        }
        PendingIntent clickPendingIntent = PendingIntent.getActivity(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.emchat_app_icon).setContentTitle(title).setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentIntent(clickPendingIntent).build();


        manager.notify(1, notification);
    }
}
