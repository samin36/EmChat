package sa.home.projects.emchat.Fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import sa.home.projects.emchat.Activities.ProfileActivity;
import sa.home.projects.emchat.Model.User;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.Consts;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView recyclerView;

    private DatabaseReference friendsDatabase;
    private DatabaseReference usersDatabase;

    private FirebaseAuth auth;

    private FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsDatabase = FirebaseDatabase.getInstance().getReference(Consts.DB_FRIEND_DATA);
        friendsDatabase.keepSynced(true);
        usersDatabase = FirebaseDatabase.getInstance().getReference("Users/");
        usersDatabase.keepSynced(true);

//        Toasty.info(getActivity(), usersDatabase.orderByChild("name").getRef().getKey(), Toasty.LENGTH_LONG).show();

        auth = FirebaseAuth.getInstance();
        String currentUid = auth.getUid();

//        friendsDatabase.child(currentUid);
//        friendsDatabase.

        recyclerView = view.findViewById(R.id.fragment_friends_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DividerItemDecoration itemDecor =
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getResources().getDrawable(R.drawable.recycler_view_divider_line));
        recyclerView.addItemDecoration(itemDecor);

        return view;
    }

    private void getAllFriends() {
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(usersDatabase.orderByChild("name"), User.class).build();

//        Toasty.info(getActivity(), "" + options.getSnapshots().size(), Toasty.LENGTH_LONG).show();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position,
                                            @NonNull User model) {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(Uri.parse(model.getThumbImage())).placeholder(R.drawable.default_profile_pic).into(holder.avatar);

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
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Consts.CURRENT_UID, currentUid);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        getAllFriends();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
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
