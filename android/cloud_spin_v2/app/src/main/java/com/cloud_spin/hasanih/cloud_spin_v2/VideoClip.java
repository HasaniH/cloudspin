package com.cloud_spin.hasanih.cloud_spin_v2;

import android.provider.MediaStore;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by hasanih on 5/25/17.
 */

public class VideoClip {

    private String clipId;
    private String status;
    private String rigId;
    private String phoneId;
    private String fullPath;

    public VideoClip() {};

    public VideoClip(String clipId, String clipStatus, String rigId, String phoneId, String fullPath) {
        this.clipId = clipId;
        this.status = clipStatus;
        this.rigId = rigId;
        this.phoneId = phoneId;
        this.fullPath = fullPath;
    }

    public String getClipId() { return clipId; }

    public String getstatus() { return status; }

    public String getRigId() { return rigId; }

    public String getPhoneId() { return phoneId; }

    public String getFullPath() { return fullPath; }

    public void setStatus(String clipStatus) { this.status = clipStatus; }

    public void sendToFirebaseDatabase(DatabaseReference databaseClips, VideoClip clip, String id){
        databaseClips.child(id).setValue(clip);
    }
}
