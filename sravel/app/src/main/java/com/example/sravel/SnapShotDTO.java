package com.example.sravel;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class SnapShotDTO implements Parcelable {
    public String id;
    public String uid;
    public String time;
    public String location;
    public Double latitude;
    public Double longitude;
    public String imageUrl;
    public String title;
    public String description;
    public String hashtag;
    public int heartCount;
    public HashMap<String, Boolean> heartCheck;
    public int mytripCount;
    public HashMap<String, Boolean> mytripCheck;
    public String hashtag2;

    public SnapShotDTO() {
    }

    public SnapShotDTO(String id, String uid, String time, String location, Double latitude, Double longitude, String imageUrl, String title, String description, String hashtag, int heartCount, HashMap<String, Boolean> hm, int mytripCount, HashMap<String, Boolean> hm2, String hashtag2) {
        this.id = id;
        this.uid = uid;
        this.time = time;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.hashtag = hashtag;
        this.heartCount = heartCount;
        this.heartCheck = hm;
        this.mytripCount = mytripCount;
        this.mytripCheck = hm2;
        this.hashtag2 = hashtag2;
    }

    protected SnapShotDTO(Parcel in) {
        id = in.readString();
        uid = in.readString();
        time = in.readString();
        location = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        imageUrl = in.readString();
        title = in.readString();
        description = in.readString();
        hashtag = in.readString();
        heartCheck = (HashMap<String, Boolean>) in.readSerializable();
        heartCount = in.readInt();
        mytripCheck = (HashMap<String, Boolean>) in.readSerializable();
        mytripCount = in.readInt();
        hashtag2 = in.readString();
    }

    public static final Creator<SnapShotDTO> CREATOR = new Creator<SnapShotDTO>() {
        @Override
        public SnapShotDTO createFromParcel(Parcel in) {
            return new SnapShotDTO(in);
        }

        @Override
        public SnapShotDTO[] newArray(int size) {
            return new SnapShotDTO[size];
        }
    };

    public HashMap<String, Boolean> getHeartCheck() {
        return heartCheck;
    }

    public HashMap<String, Boolean> getMytripCheck() {
        return mytripCheck;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(uid);
        dest.writeString(time);
        dest.writeString(location);
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
        dest.writeString(imageUrl);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(hashtag);
        dest.writeSerializable(heartCheck);
        dest.writeInt(heartCount);
        dest.writeSerializable(mytripCheck);
        dest.writeInt(mytripCount);
        dest.writeString(hashtag2);
    }
}
