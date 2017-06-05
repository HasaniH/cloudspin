// Copyright 2017 Google Inc.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package com.google.cloudspinphoto;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Grab a database reference object
    static DatabaseReference databaseClips;
    static String id;

    static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int RC_SIGN_IN = 123;

    VideoView result_video;
    private StorageReference mStorage;
    static String full_path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.  onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseClips = FirebaseDatabase.getInstance().getReference("VideoClips");

        // Grab the database reference for the database root node
        mStorage = FirebaseStorage.getInstance().getReference();
        full_path = mStorage.toString();

        Button click = (Button)findViewById(R.id.videorec);
        result_video = (VideoView)findViewById(R.id.videoView);
        Button signOutButton = (Button)findViewById(R.id.signOut);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            Log.v("hello", "world");
        } else {
            // not signed in
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                            .build(),
                    RC_SIGN_IN);

            signOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AuthUI.getInstance()
                            .signOut(MainActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    // User is now signed out
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                            });

                }
            });

        }
    }


    // On button click, this function creates a new intent and captures the video
    public void dispatchTakeVideoIntent(View v) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            id = databaseClips.push().getKey();
            full_path += "Clips/" + id;
        }
    }

    protected void addClipToDatabase(String status) {
        VideoClip videoClip = new VideoClip(id, status, "1", "1", full_path);
        videoClip.sendToFirebaseDatabase(databaseClips, videoClip, id);
    }


    // Send file to firebase storage upon finished recording and send
    // videoClip metadata to firebase database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                //Show message when
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(MainActivity.this, "User pressed the back button.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(MainActivity.this, "No Network Connection", Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(MainActivity.this, "This should never happen.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            addClipToDatabase("Uploading");

            // Full path already has mStorage path so, ignore this substring when
            // assigning filepath
            StorageReference filepath = mStorage.child(full_path.replace(mStorage.toString(), "") + ".mp4");

            filepath.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Upload Done.", Toast.LENGTH_LONG).show();
                    addClipToDatabase("Uploaded");
                }
            });
        }
    }
}

