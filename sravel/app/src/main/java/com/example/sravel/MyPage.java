package com.example.sravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

public class MyPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextView buttonLogout, buttonExit;
    TextView button_find_password;
    TextView button_mypost;
    TextView textView_name;
    TextView textView_email;
    TextView button_mytrip;
    FirebaseUser user;
    final String[] userInfo = new String[2];
    private FirebaseFirestore db;
    public final String TAG = "mypageTest";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = "";
        if (user == null) {
            startActivity(new Intent(MyPage.this, Login.class));
            return;
        } else {
            uid = user.getUid();
        }
        textView_name = findViewById(R.id.textView_name_mypage);
        textView_email = findViewById(R.id.textView_email_mypage);
        buttonLogout = findViewById(R.id.button_logout_mypage);
        buttonExit =  findViewById(R.id.button_remove_account_mypage);
        button_find_password = findViewById(R.id.button_passwordChange_mypage);
        button_mypost = findViewById(R.id.button_mypost_mypage);
        button_mytrip = findViewById(R.id.button_mytrip_mypage);

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
                startActivity(new Intent(MyPage.this, SnapShotPlus.class));
            }
        });

        button_zip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPage.this, PostItems.class);
                intent.putExtra("category", "popular");
                startActivity(intent);
            }
        });

        button_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyPage.this, MyPage.class));
            }
        });

        button_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyPage.this, MainActivity.class));
            }
        });

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
                        textView_email.setText(userInfo[0]);
                        textView_name.setText(userInfo[1]);
                        Log.d("userTest", userInfo[0] + " " + userInfo[1]);
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        button_mytrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPage.this, MyPostItems.class);
                intent.putExtra("category", "mytrip");
                startActivity(intent);
            }
        });

        button_mypost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPage.this, MyPostItems.class);
                intent.putExtra("category", "mypage");
                startActivity(intent);
            }
        });

        button_find_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPassword();
            }
        });
        buttonLogout.setOnClickListener(this::onClick);
        buttonExit.setOnClickListener(this::onClick);
    }

    private void findPassword() {
        String emailAddress = userInfo[0];
        mAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    signOut();
                    Toast.makeText(MyPage.this, "이메일을 보냈습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                } else {
                    Toast.makeText(MyPage.this, "메일 보내기에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void signOut() {
        Toast.makeText(MyPage.this, "로그아웃하였습니다.", Toast.LENGTH_LONG).show();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
    }

    private void revokeAccess() {
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MyPage.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                    }
                });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_logout_mypage:
                signOut();
                finishAffinity();
                break;
            case R.id.button_remove_account_mypage:
                revokeAccess();
                finishAffinity();
                break;
        }
    }
}