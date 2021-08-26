package com.example.sravel;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ViewPagerAdapter extends PagerAdapter {

    private Context mContext = null;
    private ArrayList<SnapShotDTO> list;
    private String id;
    private FirebaseFirestore db;
    private String uid;
    private String imageName;
    private Retrofit mRetrofit;
    private RetrofitAPI mRetrofitAPI;
    private Call<ImageModelVO> mCallImageList;
    FirebaseUser user;
    private FirebaseAuth mAuth;
    public String[] similarIdSet;
    Timer timer;
    TimerTask timerTask;
    ArrayList<SnapShotDTO> similarList;
    PostItemAdapter adapter;
    public final String TAG = "viewPagerTest";

    public ViewPagerAdapter() {

    }


    // Context를 전달받아 mContext에 저장하는 생성자 추가.
    public ViewPagerAdapter(Context context, ArrayList<SnapShotDTO> list) {
        mContext = context;
        this.list = list;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = "";
        if (user == null) {
            mContext.startActivity(new Intent(mContext, Login.class));
            return;
        } else {
            uid = user.getUid();
        }
        Log.d(TAG, "생성 uid " + uid);
        Log.d(TAG, "이제 생성" + list.get(0).title);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;

        TextView textView;
        TextView textView_time;
        TextView textView_location;
        TextView textView_hashtag;
        TextView textView_description;
        ImageButton heartButton = null;
        ImageButton mytripButton = null;
        //모델 연결
        setRetrofitInit();

        if (mContext != null) {
            // LayoutInflater를 통해 "/res/layout/page.xml"을 뷰로 생성.
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.activity_detail_snap_shot, container, false);

            textView = (TextView) view.findViewById(R.id.textview_title_detail);
            textView_time = view.findViewById(R.id.textview_time_detail);
            textView_location = view.findViewById(R.id.textview_location_detail);
            textView_hashtag = view.findViewById(R.id.textview_hashtag_detail);
            textView_description = view.findViewById(R.id.textview_description_detail);
            heartButton = view.findViewById(R.id.button_heart);
            mytripButton = view.findViewById(R.id.button_my_trip);


            if (list != null) {
                textView.setText(list.get(position).title);
                String time = list.get(position).time;
                Glide.with(mContext).load(list.get(position).imageUrl).into((ImageView) view.findViewById(R.id.imageview_detail));
                textView_time.setText(time.substring(0, 4) + "." + time.substring(4, 6) + "." + time.substring(6));
                textView_location.setText(list.get(position).location);
                textView_hashtag.setText(list.get(position).hashtag + list.get(position).hashtag2);
                textView_description.setText(list.get(position).description);
                id = list.get(position).id;
                imageName = id + ".jpg";
                callImageList();
                Log.d(TAG, "하트버튼 uid " + uid);
                HashMap<String, Boolean> hm = list.get(position).getHeartCheck();
                Log.d(TAG, "hm " + hm.toString());
                if (hm.containsKey(uid)) {
                    heartButton.setImageResource(R.drawable.fullheart);
                } else {
                    heartButton.setImageResource(R.drawable.heart);
                }

                HashMap<String, Boolean> hm2 = list.get(position).getMytripCheck();
                Log.d(TAG, "hm2 " + hm2.toString());
                if (hm2.containsKey(uid)) {
                    mytripButton.setImageResource(R.drawable.fullheart);
                } else {
                    mytripButton.setImageResource(R.drawable.heart);
                }
            }

        } else {
            Log.d(TAG, "list null");
        }
        similarList = new ArrayList<>();
        adapter = new PostItemAdapter(similarList);

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_similar);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClicklistener(new PostItemAdapter.OnPostItemClickListener() {
            @Override
            public void onItemClick(PostItemAdapter.ViewHolder holder, View view, int position) {
                SnapShotDTO item = adapter.getItem(position);

                Intent intent = new Intent(mContext, DetailSnapShot.class);
                intent.putExtra("latitude", item.latitude);
                intent.putExtra("longitude", item.longitude);
                mContext.startActivity(intent);
            }
        });

        ImageButton finalMytripButton = mytripButton;
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
                            finalMytripButton.setImageResource(R.drawable.heart);
                        } else {
                            hm.put(uid, true);
                            newCount = snapShotDTO.mytripCount + 1;
                            finalMytripButton.setImageResource(R.drawable.fullheart);
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

        ImageButton finalHeartButton = heartButton;
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
                            finalHeartButton.setImageResource(R.drawable.heart);
                        } else {
                            hm.put(uid, true);
                            newCount = snapShotDTO.heartCount + 1;
                            finalHeartButton.setImageResource(R.drawable.fullheart);
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


        // 뷰페이저에 추가.
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 뷰페이저에서 삭제.
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        // 전체 페이지 수는 10개로 고정.
        return list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View) object);
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
        ImageModelPostVO imageModelPostVO = new ImageModelPostVO(imageName);
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
            }
        }

        @Override
        public void onFailure(Call<ImageModelVO> call, Throwable t) {
            t.printStackTrace();
        }
    };

    public void splitResult(String result) {
        similarIdSet = result.split(",");
        for (int i = 1; i < 10; i++) {
            int index = similarIdSet[i].indexOf("'");
            int lastIndex = similarIdSet[i].lastIndexOf("'");
            similarIdSet[i] = similarIdSet[i].substring(index + 1, lastIndex - 4);
            Log.d(TAG, similarIdSet[i]);
        }

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
                                            similarList.add(snapShotDTO);
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
        timer.schedule(timerTask, 3000);
    }

}
