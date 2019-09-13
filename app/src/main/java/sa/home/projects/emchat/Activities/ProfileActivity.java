package sa.home.projects.emchat.Activities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.Consts;
import sa.home.projects.emchat.Utils.UiUtils;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView username;
    private TextView description;
    private TextView friendStats;
    private Button friendStatusButton;
    private Button unfriendButton;

    private DatabaseReference databaseRef;

    private DatabaseReference notificationRef;
    private FirebaseAuth auth;

    private String currentUid;

    private String friendStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        notificationRef = getDatabaseReference(Consts.DB_NOTIFICATIONS);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.profile_username);

        description = findViewById(R.id.profile_description);
        description.setSelected(true);

        friendStats = findViewById(R.id.profile_stats);

        friendStatusButton = findViewById(R.id.profile_send_friend_request);
        unfriendButton = findViewById(R.id.profile_unfriend_friend);
        UiUtils.addButtonEffect(unfriendButton);
        UiUtils.addButtonEffect(friendStatusButton);

        currentUid = getIntent().getStringExtra(Consts.CURRENT_UID);
        if (!currentUid.isEmpty()) {
            String path = String.format("Users/%s", currentUid);
            databaseRef = getDatabaseReference(path);

            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String imageUri = dataSnapshot.child("thumbImage").getValue().toString();
                    Picasso.get().load(Uri.parse(imageUri))
                            .placeholder(R.drawable.default_profile_pic).into(profileImage);

                    username.setText(dataSnapshot.child("name").getValue().toString());
                    description.setText(dataSnapshot.child("status").getValue().toString());
                    updateFriendStatus();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        friendStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friendStatus.equals(Consts.NOT_FRIENDS)) {
                    databaseRef = getDatabaseReference(Consts.DB_FRIEND_REQUESTS);

                    String loggedInUserUid = auth.getUid();
                    databaseRef.child(loggedInUserUid).child(currentUid)
                            .child(Consts.REQUEST_STATUS).setValue(Consts.REQUEST_SENT)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    databaseRef.child(currentUid).child(loggedInUserUid)
                                            .child(Consts.REQUEST_STATUS)
                                            .setValue(Consts.REQUEST_RECEIVED)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {

                                                    HashMap<String, String> notificationData =
                                                            new HashMap<>();
                                                    notificationData.put("from", loggedInUserUid);
                                                    notificationData
                                                            .put("type", Consts.REQUEST_TYPE);

                                                    notificationRef.child(currentUid).push()
                                                            .setValue(notificationData)
                                                            .addOnCompleteListener(task2 -> {
                                                                if (!task.isSuccessful()) {
                                                                    Toasty.error(
                                                                            getApplicationContext(),
                                                                            "Error occured with "
                                                                            + "adding "
                                                                            + "notification data: "
                                                                            + task.getException()
                                                                                    .getMessage(),
                                                                            Toasty.LENGTH_LONG)
                                                                            .show();
                                                                }
                                                            });


                                                    updateFriendRequestButton();
                                                }
                                            });
                                }
                            });
                    updateFriendRequestButton();
                } else if (friendStatus.equals(Consts.REQUEST_SENT) && friendStatusButton.getText()
                        .equals(getString(R.string.cancel_friend_request))) {

                    String loggedInUserUid = auth.getUid();
                    removeFriendRequests(loggedInUserUid);
                    friendStatus = Consts.NOT_FRIENDS;
                    updateFriendRequestButton();
                } else if (friendStatus.equals(Consts.REQUEST_RECEIVED) && friendStatusButton
                        .getText().equals(getString(R.string.accept_friend_request))) {
                    String loggedInUserUid = auth.getUid();
                    removeFriendRequests(loggedInUserUid);
                    updateFriendData(loggedInUserUid);
                    friendStatus = Consts.FRIENDS;
                    updateFriendRequestButton();
                }
            }
        });

        unfriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loggedInUid = auth.getUid();
                if (friendStatus.equals(Consts.FRIENDS)) {
                    removeFriendData(loggedInUid);
                    friendStatus = Consts.NOT_FRIENDS;
                } else if (friendStatus.equals(Consts.REQUEST_RECEIVED)) {
                    removeFriendRequests(loggedInUid);
                    friendStatus = Consts.NOT_FRIENDS;
                }
                updateFriendRequestButton();
            }
        });
    }

    private void removeFriendData(String loggedInUid) {
        databaseRef = getDatabaseReference(Consts.DB_FRIEND_DATA);
        databaseRef.child(loggedInUid).child(currentUid).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        databaseRef.child(currentUid).child(loggedInUid).removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (!task1.isSuccessful()) {
                                        Toasty.error(ProfileActivity.this,
                                                     "Error unfriending friend", Toasty.LENGTH_LONG)
                                                .show();
                                    }
                                });
                    }
                });
    }

    private void updateFriendData(String loggedInUserUid) {
        databaseRef = getDatabaseReference(Consts.DB_FRIEND_DATA);

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat(Consts.DATE_FORMAT);
        String todayDate = dateFormatter.format(today);

        databaseRef.child(loggedInUserUid).child(currentUid).child(Consts.FRIEND_SINCE)
                .setValue(todayDate).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                databaseRef.child(currentUid).child(loggedInUserUid).child(Consts.FRIEND_SINCE)
                        .setValue(todayDate).addOnCompleteListener(task2 -> {
                    if (!task2.isSuccessful()) {
                        Toasty.error(ProfileActivity.this, "Error updating friend database",
                                     Toasty.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void removeFriendRequests(String loggedInUserUid) {
        databaseRef = getDatabaseReference(Consts.DB_FRIEND_REQUESTS);
        databaseRef.child(loggedInUserUid).child(currentUid).removeValue()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toasty.error(ProfileActivity.this, "Error canceling request",
                                     Toasty.LENGTH_LONG).show();
                    }
                });
        databaseRef.child(currentUid).child(loggedInUserUid).removeValue()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toasty.error(ProfileActivity.this, "Error canceling request",
                                     Toasty.LENGTH_LONG).show();
                    }
                });
    }

    private DatabaseReference getDatabaseReference(String friend_requests) {
        databaseRef = FirebaseDatabase.getInstance().getReference().child(friend_requests);
        databaseRef.keepSynced(true);
        return databaseRef;
    }

    public void updateFriendStatus() {
        databaseRef = getDatabaseReference(Consts.DB_FRIEND_REQUESTS);
        String loggedInUId = auth.getUid();
        if (databaseRef != null) {
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String path = String.format("%s/%s/%s", loggedInUId, currentUid,
                                                Consts.REQUEST_STATUS);
                    if (dataSnapshot.hasChild(path)) {
                        String requestStatus = (String) dataSnapshot.child(path).getValue();
                        if (requestStatus.equals(Consts.REQUEST_SENT)) {
                            friendStatus = Consts.REQUEST_SENT;
                        } else if (requestStatus.equals(Consts.REQUEST_RECEIVED)) {
                            friendStatus = Consts.REQUEST_RECEIVED;
                        }
                        updateFriendRequestButton();
                    } else if (friendStatus == null) {
                        friendStatus = Consts.NOT_FRIENDS;
                        checkIfAlreadyFriends();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void checkIfAlreadyFriends() {
        databaseRef = getDatabaseReference(Consts.DB_FRIEND_DATA);
        String loggedInUid = auth.getUid();
        if (databaseRef != null) {
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String path = String.format("%s/%s", loggedInUid, currentUid);
                    if (dataSnapshot.hasChild(path)) {
                        friendStatus = Consts.FRIENDS;
                    }
                    updateFriendRequestButton();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void updateFriendRequestButton() {
        switch (friendStatus) {
            case Consts.REQUEST_SENT:
                friendStatusButton.setText(R.string.cancel_friend_request);
                friendStatusButton.setEnabled(true);
                unfriendButton.setEnabled(false);
                unfriendButton.setVisibility(View.INVISIBLE);
                break;
            case Consts.NOT_FRIENDS:
                friendStatusButton.setText(R.string.send_friend_request);
                friendStatusButton.setEnabled(true);
                unfriendButton.setEnabled(false);
                unfriendButton.setVisibility(View.INVISIBLE);
                break;
            case Consts.REQUEST_RECEIVED:
                friendStatusButton.setText(R.string.accept_friend_request);
                friendStatusButton.setEnabled(true);
                unfriendButton.setEnabled(true);
                unfriendButton.setVisibility(View.VISIBLE);
                unfriendButton.setText(R.string.decline_friend_request);
                break;
            case Consts.FRIENDS:
                friendStatusButton.setText(R.string.are_friends);
                friendStatusButton.setEnabled(false);
                unfriendButton.setEnabled(true);
                unfriendButton.setVisibility(View.VISIBLE);
                unfriendButton.setText(R.string.unfriend_friend);
                break;
        }
    }
}
