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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
                                    if(snapShotDTO.mytripCheck.containsKey(uid)){
                                        list.add(snapShotDTO);
                                    }else{
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
        }else if(category.equals("popular")){

        }

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
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

    }
}