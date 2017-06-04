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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Grab a database reference object
    private DatabaseReference mDatabaseClips;
    private String mId;
    private VideoView mResultVideo;

    public final int REQUEST_VIDEO_CAPTURE = 1;
    public final int RC_SIGN_IN = 123;
    private StorageReference mStorage;
    private String mFullPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabaseClips = FirebaseDatabase.getInstance().getReference("video-clips");

        // Grab the database reference for the database root node
        mStorage = FirebaseStorage.getInstance().getReference();
        mFullPath = mStorage.toString();

        Button click = (Button)findViewById(R.id.videorec);
        mResultVideo = (VideoView)findViewById(R.id.videoView);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // Already signed in
            //TODO @hasanih Display view that awaits for Firebase signals

        } else {
            // Not signed in
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                            .build(),
                    RC_SIGN_IN);

        }
    }

    // On button click, this function creates a new intent and captures the video
    private void dispatchTakeVideoIntent(View v) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            mId = mDatabaseClips.push().getKey();
            mFullPath += "Clips/" + mId;
        }
    }

    private void addClipToDatabase(String status) {
        VideoClip videoClip = new VideoClip(mId, status, "1", "1", mFullPath);
        videoClip.sendToFirebaseDatabase(mDatabaseClips, videoClip, mId);
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
                    Toast.makeText(MainActivity.this, "This should never happen!", Toast.LENGTH_LONG).show();
                    return;
                }
            }

        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            addClipToDatabase("Uploading");

            // Full path already has mStorage path so, ignore this substring when
            // assigning filepath
            StorageReference filepath = mStorage.child(mFullPath.replace(mStorage.toString(), ""));

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

