package sa.home.projects.emchat.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import sa.home.projects.emchat.Model.User;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.Consts;
import sa.home.projects.emchat.Utils.UiUtils;

public class AllUsers extends AppCompatActivity {

    private RecyclerView recyclerView;

    private Toolbar toolbar;

    private DatabaseReference databaseRef;

    private AlertDialog progressDialog;

    private FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        databaseRef = FirebaseDatabase.getInstance().getReference("Users/");


        progressDialog = UiUtils.createProgressDialog(this);

        toolbar = findViewById(R.id.all_users_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.all_users);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.all_users_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecor =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getResources().getDrawable(R.drawable.recycler_view_divider_line));
        recyclerView.addItemDecoration(itemDecor);

        getAllUsers();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        progressDialog.dismiss();
    }

    private void getAllUsers() {
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(databaseRef.orderByChild("name"), User.class).build();

        progressDialog.show();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position,
                                            @NonNull User model) {

                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(Uri.parse(model.getThumbImage()))
                        .placeholder(R.drawable.default_profile_pic).into(holder.avatar);

                String currentUid = getRef(position).getKey();

                holder.entireView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendToProfileActivity(currentUid);
                    }
                });

            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_view_holder, viewGroup, false);

                return new UsersViewHolder(view);
            }
        };
    }

    private void sendToProfileActivity(String currentUid) {
        Intent intent = new Intent(AllUsers.this, ProfileActivity.class);
        intent.putExtra(Consts.CURRENT_UID, currentUid);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private class UsersViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView avatar;

        private TextView userName;
        private TextView userStatus;

        private View entireView;


        UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            entireView = itemView;

            avatar = itemView.findViewById(R.id.all_users_image);

            userName = itemView.findViewById(R.id.all_users_name);
            userStatus = itemView.findViewById(R.id.all_users_status);
            userStatus.setSelected(true);
        }
    }
}
