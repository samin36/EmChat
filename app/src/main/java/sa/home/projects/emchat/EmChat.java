package sa.home.projects.emchat;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class EmChat extends Application {

    public static final String CHANNEL_ID = "FRIEND_REQUEST_CHANNEL";

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

//        Picasso.Builder builder = new Picasso.Builder(this);
//        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
//        Picasso built = builder.build();
//        built.setIndicatorsEnabled(true);
//        built.setLoggingEnabled(true);
//        Picasso.setSingletonInstance(built);

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel friendRequestChannel = new NotificationChannel(CHANNEL_ID, "Friend Request",
                                                                               NotificationManager.IMPORTANCE_HIGH);
            friendRequestChannel.setDescription("This notification is for friend requests");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(friendRequestChannel);
        }
    }
}
