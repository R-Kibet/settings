package com.example.clone;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.clone.Models.Users;
import com.example.clone.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //add one dependency storage


        getSupportActionBar().hide();

        //initialize firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();


        //initializing back arrow
        binding.leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });



        //initializing the save button
        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                 /*
                When clicking the save button:
                1.It will fetch the variables from  username and status field
                2.put them in the object field obj
                3.In the database we create key value pair at the specific logged (status ,username)
                4.Push tu the database any updated text fields
                 */

                if (!binding.txtUserName.getText().toString().equals("") && !binding.status.getText().toString().equals(""))
                {
                    //Extract username and status
                    String username = binding.txtUserName.getText().toString();
                    String status = binding.status.getText().toString();


                    //place in a key value bill
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("username", username);
                    obj.put("status", status);

                    database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                            .updateChildren(obj);

                    Toast.makeText(SettingsActivity.this, "Profile  updated", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        Toast.makeText(SettingsActivity.this, "Kindly Update profile", Toast.LENGTH_SHORT).show();
                    }
            }

        });


        //FETCH profile info from database and displaying it
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Users users = snapshot.getValue(Users.class);

                        //use picasso to fetch image from database storage
                        Picasso.get()
                                .load(users.getProfilePic())
                                //default image
                                .placeholder(R.drawable.avatar)
                                .into(binding.profilePic);

                        //in users model we set getters and setters
                        binding.txtUserName.setText(users.getUsername());
                        binding.status.setText(users.getStatus());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        //moving to gallery
        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);//setting action to get content
                intent.setType("images/*");//selecting all image format
                startActivityForResult(intent,25);//request code allows you to pass anyhting

            }
        });

        binding.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Privacy policy", Toast.LENGTH_SHORT).show();
            }
        });

        binding.textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "About Us", Toast.LENGTH_SHORT).show();
            }
        });
        binding.textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Invite a Friend", Toast.LENGTH_SHORT).show();
            }
        });
        binding.textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Notification", Toast.LENGTH_SHORT).show();
            }
        });
        binding.textView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Support", Toast.LENGTH_SHORT).show();
            }
        });




    }

    //store data and fetch it in firebase

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //check if user has selected something

        if(data.getData() != null) {
            //setting image
            Uri sFile = data.getData();
            binding.profilePic.setImageURI(sFile);


            //storing in firebase
            final StorageReference reference = storage.getReference().child("profilePic") //profile pic will be created and act as location to be saved
                    .child(FirebaseAuth.getInstance().getUid());

            //create the file
            reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    //on success we wan to get the reference url/uri and store on firebase

                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(@NonNull Uri uri) {

                            //store it under the users
                       database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()) //id shows for which user is logged
                               .child("profilePic").setValue(uri.toString());//profilePic is the key  and value is from the storage

                        }//we have stored the image but once up restarts it wont show image as we haven't fetched it hence line FETCH
                    });

                }
            });

        }
    }
}