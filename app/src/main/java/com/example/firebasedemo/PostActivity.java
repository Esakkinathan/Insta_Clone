package com.example.firebasedemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hendraanggrian.appcompat.socialview.autocomplete.Hashtag;
import com.hendraanggrian.appcompat.socialview.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    private ImageView close,imageAdded;
    private TextView post;
    SocialAutoCompleteTextView description;
    private Uri imageUri;
    private String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        CropImage.activity().start(PostActivity.this);
    }

    private void uploadImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading Image");
        pd.show();
        if(imageUri!=null){
            StorageReference filePath = FirebaseStorage.getInstance().getReference("Posts").child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Uri downloadUri = task.getResult();
                                imageUrl = downloadUri.toString();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                String postId = ref.push().getKey();
                                HashMap<String,Object> map = new HashMap<>();
                                map.put("postid",postId);
                                map.put("imageurl",imageUrl);
                                map.put("description",description.getText().toString());
                                map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                ref.child(postId).setValue(map);

                                DatabaseReference mHashtagRef = FirebaseDatabase.getInstance().getReference("HashTags");
                                List<String> hashTags = description.getHashtags();
                                if(!hashTags.isEmpty()){
                                    for (String tag: hashTags){
                                        map.clear();
                                        map.put("tag",tag.toLowerCase());
                                        map.put("postid",postId);

                                        mHashtagRef.child(tag.toLowerCase()).child(postId).setValue(map);
                                    }
                                }
                                pd.dismiss();
                                startActivity(new Intent(PostActivity.this, MainActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
        else{
            Toast.makeText(this, "No Image was selected", Toast.LENGTH_SHORT).show();

        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            imageAdded.setImageURI(imageUri);
        }
        else{
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    hashtagAdapter.add(new Hashtag(dataSnapshot.getKey(), (int) dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        description.setHashtagAdapter(hashtagAdapter);
    }
}