package com.example.sravel;

import java.util.HashMap;
import java.util.Map;

public class SnapShotDTO {
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

    public SnapShotDTO() {
    }

    public SnapShotDTO(String id, String uid, String time, String location, Double latitude, Double longitude, String imageUrl, String title, String description, String hashtag, int heartCount, HashMap<String, Boolean> hm, int mytripCount, HashMap<String, Boolean> hm2) {
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
    }

}
