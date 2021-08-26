package com.example.sravel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class ViewPager extends AppCompatActivity {

    private androidx.viewpager.widget.ViewPager viewPager ;
    private ViewPagerAdapter pagerAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        Intent intent = getIntent();
        ArrayList<SnapShotDTO> list = intent.getExtras().getParcelableArrayList("list");
        viewPager = findViewById(R.id.viewPager) ;
        pagerAdapter = new ViewPagerAdapter(this,list) ;
        viewPager.setAdapter(pagerAdapter) ;
    }
}