package com.example.sravel;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.transition.Transition;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.DataSource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, AutoPermissionsListener {
    private GoogleMap mMap;
    private FirebaseFirestore db;
    public final String TAG = "MainTest";
    private Marker currentMarker = null;
    TextView textView_now_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        textView_now_location = findViewById(R.id.textView_now_location);

        ImageView icon = new ImageView(this); // Create an icon
        icon.setImageResource(R.drawable.add);

        FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageResource(R.drawable.home);
        SubActionButton button_home = itemBuilder.setContentView(itemIcon).build();
        ImageView itemIcon2 = new ImageView(this);
        itemIcon.setImageResource(R.drawable.add);
        SubActionButton button_add = itemBuilder.setContentView(itemIcon2).build();
        ImageView itemIcon3 = new ImageView(this);
        itemIcon.setImageResource(R.drawable.user);
        SubActionButton button_user = itemBuilder.setContentView(itemIcon3).build();
        ImageView itemIcon4 = new ImageView(this);
        itemIcon.setImageResource(R.drawable.zipfile);
        SubActionButton button_zip = itemBuilder.setContentView(itemIcon4).build();

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button_add)
                .addSubActionView(button_home)
                .addSubActionView(button_user)
                .addSubActionView(button_zip)
                .attachTo(actionButton)
                .build();

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SnapShotPlus.class));
            }
        });

        button_zip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostItems.class);
                intent.putExtra("category", "popular");
                startActivity(intent);
            }
        });

        Button button_food = findViewById(R.id.button_food);
        Button button_city = findViewById(R.id.button_city);
        Button button_sky = findViewById(R.id.button_sky);
        Button button_place = findViewById(R.id.button_place);
        Button button_ocean = findViewById(R.id.button_ocean);
        Button button_animal = findViewById(R.id.button_animal);

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AutoPermissions.Companion.loadAllPermissions(this, 100);

        //바다 카테고리
        button_ocean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                db.collection("snapshots")
                        .whereEqualTo("hashtag", "바다")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                        MarkerOptions mOptions = new MarkerOptions();
                                        mOptions.title(snapShotDTO.title);

                                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                        mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                        Double latitude = snapShotDTO.latitude; //위도
                                        Double longitude = snapShotDTO.longitude; //경도

                                        //간단한 텍스트
                                        mOptions.snippet(snapShotDTO.description);

                                        mOptions.position(new LatLng(latitude, longitude));
                                        mMap.addMarker(mOptions);

                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });
        //하늘 카테고리
        button_sky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                db.collection("snapshots")
                        .whereEqualTo("hashtag", "하늘")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                        MarkerOptions mOptions = new MarkerOptions();
                                        mOptions.title(snapShotDTO.title);

                                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                        mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                        Double latitude = snapShotDTO.latitude; //위도
                                        Double longitude = snapShotDTO.longitude; //경도

                                        //간단한 텍스트
                                        mOptions.snippet(snapShotDTO.description);

                                        mOptions.position(new LatLng(latitude, longitude));
                                        mMap.addMarker(mOptions);

                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });

        //동물 카테고리
        button_animal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                db.collection("snapshots")
                        .whereEqualTo("hashtag", "동물")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                        MarkerOptions mOptions = new MarkerOptions();
                                        mOptions.title(snapShotDTO.title);

                                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                        mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                        Double latitude = snapShotDTO.latitude; //위도
                                        Double longitude = snapShotDTO.longitude; //경도

                                        //간단한 텍스트
                                        mOptions.snippet(snapShotDTO.description);

                                        mOptions.position(new LatLng(latitude, longitude));
                                        mMap.addMarker(mOptions);

                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });

        //음식 카테고리
        button_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                db.collection("snapshots")
                        .whereEqualTo("hashtag", "음식")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                        MarkerOptions mOptions = new MarkerOptions();
                                        mOptions.title(snapShotDTO.title);

                                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                        mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                        Double latitude = snapShotDTO.latitude; //위도
                                        Double longitude = snapShotDTO.longitude; //경도

                                        //간단한 텍스트
                                        mOptions.snippet(snapShotDTO.description);

                                        mOptions.position(new LatLng(latitude, longitude));
                                        mMap.addMarker(mOptions);

                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });

        //동물 카테고리
        button_animal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                db.collection("snapshots")
                        .whereEqualTo("hashtag", "동물")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                        MarkerOptions mOptions = new MarkerOptions();
                                        mOptions.title(snapShotDTO.title);

                                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                        mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                        Double latitude = snapShotDTO.latitude; //위도
                                        Double longitude = snapShotDTO.longitude; //경도

                                        //간단한 텍스트
                                        mOptions.snippet(snapShotDTO.description);

                                        mOptions.position(new LatLng(latitude, longitude));
                                        mMap.addMarker(mOptions);

                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });

        //도시 카테고리
        button_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                db.collection("snapshots")
                        .whereEqualTo("hashtag", "도시")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                        MarkerOptions mOptions = new MarkerOptions();
                                        mOptions.title(snapShotDTO.title);

                                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                        mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                        Double latitude = snapShotDTO.latitude; //위도
                                        Double longitude = snapShotDTO.longitude; //경도

                                        //간단한 텍스트
                                        mOptions.snippet(snapShotDTO.description);

                                        mOptions.position(new LatLng(latitude, longitude));
                                        mMap.addMarker(mOptions);

                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });

        //명소 카테고리
        button_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                db.collection("snapshots")
                        .whereEqualTo("hashtag", "명소")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                        MarkerOptions mOptions = new MarkerOptions();
                                        mOptions.title(snapShotDTO.title);

                                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                        mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                        Double latitude = snapShotDTO.latitude; //위도
                                        Double longitude = snapShotDTO.longitude; //경도

                                        //간단한 텍스트
                                        mOptions.snippet(snapShotDTO.description);

                                        mOptions.position(new LatLng(latitude, longitude));
                                        mMap.addMarker(mOptions);

                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });


        button_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyPage.class));
            }
        });



        SearchView sv = (SearchView) findViewById(R.id.searchview);
        sv.setSubmitButtonEnabled(true);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("search", query);
                if(query.charAt(0) == '#'){
                    searchHashTag(query);
                }
                Geocoder geocoder = new Geocoder(getBaseContext());
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocationName(query, 3);
                    if (addresses != null && !addresses.equals(" ")) {
                        search(addresses);
                    }
                } catch (Exception e) {

                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }
    public void searchHashTag(String query){
        mMap.clear();
        db.collection("snapshots")
                .whereEqualTo("hashtag2", query)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                MarkerOptions mOptions = new MarkerOptions();
                                mOptions.title(snapShotDTO.title);

                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                Double latitude = snapShotDTO.latitude; //위도
                                Double longitude = snapShotDTO.longitude; //경도

                                //간단한 텍스트
                                mOptions.snippet(snapShotDTO.description);

                                mOptions.position(new LatLng(latitude, longitude));
                                mMap.addMarker(mOptions);

                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent(MainActivity.this, ViewPager.class);
        Double latitude = marker.getPosition().latitude;
        Double longitude = marker.getPosition().longitude;
        Geocoder mGeocoder = new Geocoder(getApplicationContext());
        List<Address> address = null;
        try {
            address = mGeocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView_now_location.setText(address.get(0).getAddressLine(0));
        ArrayList<SnapShotDTO> detailList = new ArrayList<>();

        db.collection("snapshots")
                .whereGreaterThanOrEqualTo("latitude", latitude)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                Log.d(TAG, "Main hashtag" + snapShotDTO.hashtag);
                                Log.d(TAG, "Main hashtag2" + snapShotDTO.hashtag2);
                                detailList.add(snapShotDTO);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                intent.putExtra("list", detailList);
                startActivity(intent);
            }
        };
        timer.schedule(timerTask, 2000);

        return true;
    }

    // 구글맵 주소 검색 메서드
    protected void search(List<Address> addresses) {
        Address address = addresses.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        String addressText = String.format(
                "%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address
                        .getAddressLine(0) : " ", address.getFeatureName());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(addressText);

        //mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        startLocationService();
        //마커 클릭 이벤트 처리
        mMap.setOnMarkerClickListener(this);

        //모든 마커 찍기
        db.collection("snapshots")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                SnapShotDTO snapShotDTO = document.toObject(SnapShotDTO.class);
                                MarkerOptions mOptions = new MarkerOptions();
                                mOptions.title(snapShotDTO.title);

                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.apple);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                mOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                                Double latitude = snapShotDTO.latitude; //위도
                                Double longitude = snapShotDTO.longitude; //경도

                                //간단한 텍스트
                                mOptions.snippet(snapShotDTO.description);

                                mOptions.position(new LatLng(latitude, longitude));
                                mMap.addMarker(mOptions);

                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                MarkerOptions mOptions = new MarkerOptions();
                mOptions.title("새로운 여행지");

                Double latitude = latLng.latitude; //위도
                Double longitude = latLng.longitude; //경도
                Geocoder mGeocoder = new Geocoder(getApplicationContext());
                List<Address> address = null;
                try {
                    address = mGeocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView_now_location.setText(address.get(0).getAddressLine(0));

                //간단한 텍스트
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());

                mOptions.position(new LatLng(latitude, longitude));
                //mMap.addMarker(mOptions);
            }
        });
    }

    private void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }

    private void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            int chk1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int chk2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            Location location = null;
            if (chk1 == PackageManager.PERMISSION_GRANTED && chk2 == PackageManager.PERMISSION_GRANTED) {
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else {
                return;
            }

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Geocoder mGeocoder = new Geocoder(getApplicationContext());
                List<Address> address = mGeocoder.getFromLocation(latitude, longitude, 1);
                String msg = "최근 위치 ->  Latitue : " + latitude + "\nLongitude : " + longitude;
                showCurrentLocation(latitude, longitude);
                textView_now_location.setText(address.get(0).getAddressLine(0));
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);


        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }


class GPSListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        String message = "내 위치 -> Latitude : " + latitude + "\nLongitude:" + longitude;
        Log.d(TAG, message);

        showCurrentLocation(latitude, longitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}

    private void showCurrentLocation(Double latitude, Double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

        //디폴트 위치, Seoul
        Log.d(TAG, "실행");
        String markerTitle = "내위치";
        String markerSnippet = "위치정보가 확인되었습니다.";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(curPoint);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curPoint, 15);
        mMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onDenied(int i, String[] strings) {
    }

    @Override
    public void onGranted(int i, String[] strings) {
        Log.d(TAG, "permissions granted ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }
}