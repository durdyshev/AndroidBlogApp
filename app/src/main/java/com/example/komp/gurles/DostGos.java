package com.example.komp.gurles;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DostGos extends AppCompatActivity {
    private ViewPager viewPager;
private FirebaseAuth mAuth;
private SectionsPageAdapter sectionsPageAdapter;
private TabLayout tabLayout;
    private Toolbar mToolbar;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dost_gos);
        mAuth=FirebaseAuth.getInstance();
        viewPager=(ViewPager)findViewById(R.id.dost_gos_pager);
        firebaseFirestore=FirebaseFirestore.getInstance();

sectionsPageAdapter=new SectionsPageAdapter(getSupportFragmentManager());
viewPager.setAdapter(sectionsPageAdapter);
tabLayout=(TabLayout)findViewById(R.id.dost_gos_tablayout);
tabLayout.setupWithViewPager(viewPager);


        mToolbar=(Toolbar)findViewById(R.id.dost_gos_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Islegler");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    songorulme(FieldValue.serverTimestamp());
    }
    private void status(String boslyk) {
        Map<String, Object> status = new HashMap<>();
        status.put("status", boslyk);

        firebaseFirestore.collection("/ulanyjylar/").document(mAuth.getCurrentUser().getUid()).update(status);
    }
    private void songorulme(FieldValue boslyk) {
        Map<String, Object> map = new HashMap<>();
        map.put("son", boslyk);

        firebaseFirestore.collection("/ulanyjylar/").document(mAuth.getCurrentUser().getUid()).update(map);
    }

}

