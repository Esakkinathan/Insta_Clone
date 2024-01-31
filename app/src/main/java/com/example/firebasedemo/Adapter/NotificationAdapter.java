package com.example.firebasedemo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasedemo.Fragments.PostDetailFragment;
import com.example.firebasedemo.Fragments.ProfileFragment;
import com.example.firebasedemo.Model.Notification;
import com.example.firebasedemo.Model.Post;
import com.example.firebasedemo.Model.User;
import com.example.firebasedemo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;


public class NotificationAdapter extends  RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context mContext, List<Notification> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item,parent,false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = mNotification.get(position);

        getUser(holder.imageprofile,holder.userName,notification.getUserid());
        holder.comment.setText(notification.getText());
        if (notification.isPost()){
            holder.postImage.setVisibility(View.VISIBLE);
            getPostImage(holder.postImage,notification.getPostid());
        }else{
            holder.postImage.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notification.isPost()){
                    mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
                            .edit().putString("postId",notification.getPostid() ).apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container,new PostDetailFragment()).commit();
                }else {
                    mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",notification.getUserid()).apply();
                    ((FragmentActivity)mContext).getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
                }
            }
        });


    }




    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageprofile;
        public ImageView postImage;
        public TextView userName;
        public TextView comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageprofile = itemView.findViewById(R.id.imageprofile);
            postImage = itemView.findViewById(R.id.post_image);
            userName = itemView.findViewById(R.id.n_username);
            comment = itemView.findViewById(R.id.comment);
        }
    }
    private void getUser(ImageView imageprofile, TextView userName,String userId) {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if(user.getImageurl().equals("default")){
                            imageprofile.setImageResource(R.drawable.profile);
                        }
                        else{
                        Picasso.get().load(user.getImageurl()).into(imageprofile);
                        }
                        userName.setText(user.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getPostImage(ImageView postImage, String postid) {

        FirebaseDatabase.getInstance().getReference().child("Posts").child(postid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        Picasso.get().load(post.getImageurl()).placeholder(R.mipmap.ic_launcher).into(postImage);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
