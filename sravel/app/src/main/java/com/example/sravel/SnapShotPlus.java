package com.example.sravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
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
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SnapShotPlus extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 1;
    private File tempFile;
    private static final int PICK_FROM_CAMERA = 2;
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private String time;
    private String location = "";
    private String title = "";
    private String description = "";
    private String imageUrl = "";
    private String hashtag = "";
    private String hashtag2 = "";
    private FirebaseStorage mStorage;
    private FirebaseFirestore db;
    public final String TAG = "snapshotplusTest";
    FirebaseUser user;
    String uid;
    private String id;
    private FirebaseAuth mAuth;
    SnapShotDTO updateDto;
    boolean updateCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_shot_plus);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = "";
        if (user == null) {
            startActivity(new Intent(SnapShotPlus.this, Login.class));
            return;
        } else {
            uid = user.getUid();
        }
        Button button_add = findViewById(R.id.button_add);
        ImageButton button_camera = findViewById(R.id.button_camera);
        ImageButton button_gallery = findViewById(R.id.button_gallery);
        Button button_map = findViewById(R.id.button_map);
        EditText editText_title = findViewById(R.id.editText_Title);
        EditText editText_description = findViewById(R.id.editText_SimpleDescription);
        EditText editText_hashtag = findViewById(R.id.editText_hashtag);
        ImageView imageView = findViewById(R.id.imageView);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        button_camera.setImageResource(R.drawable.camera);
        button_gallery.setImageResource(R.drawable.gallery);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            latitude = intent.getExtras().getDouble("latitude");
            longitude = intent.getExtras().getDouble("longitude");
            Log.d(TAG, "위도 " + latitude + " 경도" + longitude);
        }
        if (intent.getExtras() != null && intent.getExtras().getParcelable("update") != null) {
            updateCheck = true;
            updateDto = intent.getExtras().getParcelable("update");
            longitude = updateDto.longitude;
            latitude = updateDto.latitude;
            editText_title.setText(updateDto.title);
            editText_description.setText(updateDto.description);
            editText_hashtag.setText(updateDto.hashtag2);
            imageUrl = updateDto.imageUrl;
            id = updateDto.id;
            Glide.with(this).load(imageUrl).into(imageView);
        }


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton) {
                    hashtag = "#바다";
                } else if (checkedId == R.id.radioButton2) {
                    hashtag = "#하늘";
                } else if (checkedId == R.id.radioButton3) {
                    hashtag = "#거리";
                } else if (checkedId == R.id.radioButton4) {
                    hashtag = "#음식";
                } else if (checkedId == R.id.radioButton5) {
                    hashtag = "#동물";
                } else if(checkedId == R.id.radioButton6){
                    hashtag = "#명소";
                }else if(checkedId == R.id.radioButton_city){
                    hashtag = "#도시";
                }else if(checkedId == R.id.radioButton_nature){
                    hashtag = "#자연";
                }else if(checkedId == R.id.radioButton_country){
                    hashtag = "#시골";
                }
            }
        });

        mStorage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SnapShotPlus.this, SnapShotLocationSelect.class));
            }
        });

        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tedPermission();
                takePhoto();
            }
        });

        button_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tedPermission();
                goToAlbum();
            }
        });

        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd");
        time = mFormat.format(mDate);

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                HashMap<String, Boolean> hm = new HashMap<>();
                hm.put("testUid", true);
                Log.d("plusTest", "button" + imageUrl);
                DocumentReference snapshotRef = db.collection("snapshots").document();
                SnapShotDTO snapShotDTO = new SnapShotDTO(snapshotRef.getId(), uid, time, location, latitude, longitude, imageUrl, title, description, hashtag, 0, hm, 0, hm, "#" + hashtag2);
                id = snapshotRef.getId();
                snapshotRef.set(snapShotDTO);
                uploadS3();
                startActivity(new Intent(SnapShotPlus.this, MainActivity.class));
            }
        };

        TimerTask timerTask2 = new TimerTask() {
            @Override
            public void run() {

                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentReference snapRef = db.collection("snapshots").document(id);
                        DocumentSnapshot snapshot = transaction.get(snapRef);

                        transaction.update(snapRef, "latitude", latitude);
                        transaction.update(snapRef, "longitude", longitude);
                        transaction.update(snapRef, "imageUrl", imageUrl);
                        transaction.update(snapRef, "title", title);
                        transaction.update(snapRef, "description", description);
                        transaction.update(snapRef, "hashtag", hashtag);
                        transaction.update(snapRef, "hashtag2", hashtag2);

                        startActivity(new Intent(SnapShotPlus.this, MainActivity.class));

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
        };

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = editText_title.getText().toString();
                description = editText_description.getText().toString();
                hashtag2 = editText_hashtag.getText().toString();
                Toast.makeText(getApplicationContext(), "여행지가 등록되었습니다.", Toast.LENGTH_LONG).show();
                if (updateCheck) {
                    timer.schedule(timerTask2, 4000);
                } else {
                    timer.schedule(timerTask, 5000);
                }
            }
        });
    }

    private void uploadS3() {
        String accessKey = "";
        String secretKey = "";
        String bucketName = "snavel";

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(this).build();
        TransferNetworkLossHandler.getInstance(this);

        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = id + ".jpg";

        TransferObserver uploadObserver = transferUtility.upload(bucketName, "images/" + imageFileName, tempFile);
        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "onStateChanged: " + id + ", " + state.toString());
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;
                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        });
    }

    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }

    private void goToAlbum() {
        //앨범에서 이미지 가져오기
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e("TAG", tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }
            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {
            Uri photoUri = data.getData();
            Cursor cursor = null;
            try {
                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = {MediaStore.Images.Media.DATA};

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            setImage();

        } else if (requestCode == PICK_FROM_CAMERA) {
            setImage();
        }
    }

    private void setImage() {
        ImageView imageView = findViewById(R.id.imageView);
        Uri uri = Uri.fromFile(tempFile);
        StorageReference storageReference = mStorage.getReference().child("snapshotImages").child("uid").child(tempFile.getAbsolutePath());
        storageReference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                final Task<Uri> imageTask = task.getResult().getStorage().getDownloadUrl();
                while (!imageTask.isComplete()) ;
                imageUrl = imageTask.getResult().toString();
                Log.d("plusTest", "set image " + imageUrl);
            }
        });

        Glide.with(this).load(tempFile.getAbsoluteFile()).into(imageView);
    }

    private void takePhoto() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.sravel.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            } else {

                Uri photoUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            }
        }
    }

    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "sravel_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/sravel/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

}