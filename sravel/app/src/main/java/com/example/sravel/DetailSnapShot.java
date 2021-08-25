package com.example.sravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailSnapShot extends AppCompatActivity {
    private FirebaseFirestore db;
    public final String TAG = "detailTest";
    private String id;
    private String uid;
    private Retrofit mRetrofit;
    private RetrofitAPI mRetrofitAPI;
    private Call<ImageModelVO> mCallImageList;
    private String imageName;
    FirebaseUser user;
    private FirebaseAuth mAuth;
    public String[] similarIdSet;
    Timer timer;
    TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_snap_shot);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = "";
        if (user == null) {
            startActivity(new Intent(DetailSnapShot.this, Login.class));
            return;
        } else {
            uid = user.getUid();
        }

        TextView textView_title = findViewById(R.id.textview_title_detail);
        TextView textView_location = findViewById(R.id.textview_location_detail);
        TextView textView_description = findViewById(R.id.textview_description_detail);
        TextView textView_time = findViewById(R.id.textview_time_detail);
        TextView textView_hashtag = findViewById(R.id.textview_hashtag_detail);
        ImageView imageView = findViewById(R.id.imageview_detail);
        ImageButton heartButton = findViewById(R.id.button_heart);
        ImageButton mytripButton = findViewById(R.id.button_my_trip);

        //모델 연결
        setRetrofitInit();

        Intent intent = getIntent();
        Double latitude = intent.getExtras().getDouble("latitude");
        Double longitude = intent.getExtras().getDouble("longitude");
        id = "";

        ArrayList<SnapShotDTO> list = new ArrayList<>();
        PostItemAdapter adapter = new PostItemAdapter(list);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                for (int i = 1; i < similarIdSet.length; i++) {
                    db.collection("snapshots")
                            .whereEqualTo("id", similarIdSet[i])
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
            }
        };

        db.collection("snapshots")
                .whereEqualTo("latitude", latitude)
                .whereEqualTo("longitude", longitude)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                String time = snapShotDTO.time;
                                Glide.with(getApplicationContext()).load(snapShotDTO.imageUrl).into(imageView);
                                textView_title.setText(snapShotDTO.title);
                                textView_time.setText(time.substring(0, 4) + "." + time.substring(4, 6) + "." + time.substring(6));
                                textView_location.setText(snapShotDTO.location);
                                textView_hashtag.setText("#" + snapShotDTO.hashtag);
                                textView_description.setText(snapShotDTO.description);
                                id = snapShotDTO.id;
                                imageName = id + ".jpg";
                                Log.d(TAG, id);
                                callImageList();

                                if (snapShotDTO.heartCheck.containsKey(uid)) {
                                    heartButton.setImageResource(R.drawable.fullheart);
                                } else {
                                    heartButton.setImageResource(R.drawable.heart);
                                }

                                if (snapShotDTO.mytripCheck.containsKey(uid)) {
                                    mytripButton.setImageResource(R.drawable.fullheart);
                                } else {
                                    mytripButton.setImageResource(R.drawable.heart);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        RecyclerView recyclerView = findViewById(R.id.recyclerView_similar);
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

        mytripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentReference snapRef = db.collection("snapshots").document(id);
                        DocumentSnapshot snapshot = transaction.get(snapRef);
                        SnapShotDTO snapShotDTO = snapshot.toObject(SnapShotDTO.class);

                        // Note: this could be done without a transaction
                        //       by updating the population using FieldValue.increment()
                        HashMap<String, Boolean> hm = new HashMap<>();
                        hm = snapShotDTO.mytripCheck;
                        int newCount = 0;
                        if (snapShotDTO.mytripCheck.containsKey(uid)) {
                            hm.remove(uid);
                            newCount = snapShotDTO.mytripCount - 1;
                            mytripButton.setImageResource(R.drawable.heart);
                        } else {
                            hm.put(uid, true);
                            newCount = snapShotDTO.mytripCount + 1;
                            mytripButton.setImageResource(R.drawable.fullheart);
                        }

                        transaction.update(snapRef, "mytripCount", newCount);
                        transaction.update(snapRef, "mytripCheck", hm);

                        // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Transaction failure.", e);
                            }
                        });

            }
        });

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentReference snapRef = db.collection("snapshots").document(id);
                        DocumentSnapshot snapshot = transaction.get(snapRef);
                        SnapShotDTO snapShotDTO = snapshot.toObject(SnapShotDTO.class);

                        // Note: this could be done without a transaction
                        //       by updating the population using FieldValue.increment()
                        HashMap<String, Boolean> hm = new HashMap<>();
                        hm = snapShotDTO.heartCheck;
                        int newCount = 0;
                        if (snapShotDTO.heartCheck.containsKey(uid)) {
                            hm.remove(uid);
                            newCount = snapShotDTO.heartCount - 1;
                            heartButton.setImageResource(R.drawable.heart);
                        } else {
                            hm.put(uid, true);
                            newCount = snapShotDTO.heartCount + 1;
                            heartButton.setImageResource(R.drawable.fullheart);
                        }

                        transaction.update(snapRef, "heartCount", newCount);
                        transaction.update(snapRef, "heartCheck", hm);

                        // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Transaction failure.", e);
                            }
                        });
            }
        });
    }

    private void setRetrofitInit() {
        String baseURL = "";

        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mRetrofitAPI = mRetrofit.create(RetrofitAPI.class);
    }

    private void callImageList() {
        imageName = id + ".jpg";
        Log.d(TAG, imageName);
        ImageModelPostVO imageModelPostVO = new ImageModelPostVO("images/"+imageName);
        mCallImageList = mRetrofitAPI.getImageList(imageModelPostVO);
        mCallImageList.enqueue(mRetrofitCallback);
    }

    private Callback<ImageModelVO> mRetrofitCallback = new Callback<ImageModelVO>() {
        @Override
        public void onResponse(Call<ImageModelVO> call, Response<ImageModelVO> response) {
            ImageModelVO result = response.body();
            if (result != null) {
                Log.d(TAG, result.getResult());
                splitResult(result.getResult());
            } else {
                Log.d(TAG, "null 뜸");
                splitResult("{0: 'dfsdf', 'dfsdf.jpg' ,1: 'Y8oGfS958XQvRDQ3BJNY.jpg'}");
            }
        }

        @Override
        public void onFailure(Call<ImageModelVO> call, Throwable t) {
            t.printStackTrace();
        }
    };

    public void splitResult(String result) {
        similarIdSet = result.split(",");
        for (int i = 1; i < similarIdSet.length; i++) {
            int index = similarIdSet[i].indexOf("'");
            int lastIndex = similarIdSet[i].lastIndexOf("'");
            similarIdSet[i] = similarIdSet[i].substring(index + 1, lastIndex - 4);
            Log.d(TAG, similarIdSet[i]);
        }
        timer.schedule(timerTask, 2000);
    }
}