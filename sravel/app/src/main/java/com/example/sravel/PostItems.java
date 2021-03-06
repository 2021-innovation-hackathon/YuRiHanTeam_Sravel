package com.example.sravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PostItems extends AppCompatActivity {
    FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    String uid;
    final String[] userInfo = new String[2];
    public final String TAG = "PostTest";
    private ArrayList<SnapShotDTO> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_items);

        list = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        ImageView icon = new ImageView(this); // Create an icon
        icon.setImageResource(R.drawable.sravel);

        FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageResource(R.drawable.button_home);
        SubActionButton button_home = itemBuilder.setContentView(itemIcon).build();

        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageResource(R.drawable.button_plus);
        SubActionButton button_add = itemBuilder.setContentView(itemIcon2).build();

        ImageView itemIcon3 = new ImageView(this);
        itemIcon3.setImageResource(R.drawable.button_mypage);
        SubActionButton button_user = itemBuilder.setContentView(itemIcon3).build();

        ImageView itemIcon4 = new ImageView(this);
        itemIcon4.setImageResource(R.drawable.button_zip);
        SubActionButton button_zip = itemBuilder.setContentView(itemIcon4).build();

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button_home)
                .addSubActionView(button_zip)
                .addSubActionView(button_user)
                .addSubActionView(button_add)
                .attachTo(actionButton)
                .build();


        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostItems.this, SnapShotPlus.class));
            }
        });

        button_zip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostItems.this, PostItems.class);
                intent.putExtra("category", "popular");
                startActivity(intent);
            }
        });

        button_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostItems.this, MyPage.class));
            }
        });

        button_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostItems.this, MainActivity.class));
            }
        });


        ImageButton button_popular = findViewById(R.id.button_popular);
        ImageButton button_recent = findViewById(R.id.button_recent);
        button_popular.setImageResource(R.drawable.popular_blue);
        button_recent.setImageResource(R.drawable.recent_white);

        PostItemAdapter adapter = new PostItemAdapter(list);
        db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(uid);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userInfo[0] = document.get("email").toString();
                        userInfo[1] = document.get("name").toString();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        Intent intent = getIntent();
        String category = intent.getExtras().getString("category");
        if (category.equals("mypage")) {
            db.collection("snapshots")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                    list.add(snapShotDTO);
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else if (category.equals("mytrip")) {
            db.collection("snapshots")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                    if (snapShotDTO.mytripCheck.containsKey(uid)) {
                                        list.add(snapShotDTO);
                                    } else {
                                        continue;
                                    }

                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else if (category.equals("popular")) {
            db.collection("snapshots")
                    .orderBy("heartCount", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                    list.add(snapShotDTO);
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else {
            button_popular.setImageResource(R.drawable.popular_white);
            button_recent.setImageResource(R.drawable.recent_blue);
            db.collection("snapshots")
                    .orderBy("time",  Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                    list.add(snapShotDTO);
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        // ????????????????????? LinearLayoutManager ?????? ??????.
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

        // ????????????????????? SimpleTextAdapter ?????? ??????.
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClicklistener(new PostItemAdapter.OnPostItemClickListener() {
            @Override
            public void onItemClick(PostItemAdapter.ViewHolder holder, View view, int position) {
                SnapShotDTO item = adapter.getItem(position);

                Intent intent = new Intent(getApplicationContext(), DetailSnapShot.class);
                intent.putExtra("latitude", item.latitude);
                intent.putExtra("longitude", item.longitude);
                startActivity(intent);
            }
        });

        button_popular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_popular.setImageResource(R.drawable.popular_blue);
                button_recent.setImageResource(R.drawable.recent_white);
                Intent intent = new Intent(getApplicationContext(), PostItems.class);
                intent.putExtra("category", "popular");
                startActivity(intent);
            }
        });

        button_recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_popular.setImageResource(R.drawable.popular_white);
                button_recent.setImageResource(R.drawable.recent_blue);
                Intent intent = new Intent(getApplicationContext(), PostItems.class);
                intent.putExtra("category", "recent");
                startActivity(intent);
            }
        });


    }
}