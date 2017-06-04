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

import com.google.firebase.database.DatabaseReference;

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

    public String getClipId() {
        return clipId;
    }

    public String getStatus() {
        return status;
    }

    public String getstatus() {
        return status;
    }

    public String getRigId() {
        return rigId;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setStatus(String clipStatus) {
        this.status = clipStatus;
    }

    public void sendToFirebaseDatabase(DatabaseReference databaseClips, VideoClip clip, String id) {
        databaseClips.child(id).setValue(clip);
    }
}
