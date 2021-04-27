package com.example.komp.gurles;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dostlar_yeke_haly_dostlar_hemmesi extends AppCompatActivity {
    private ViewPager viewPager;
    private FirebaseAuth mAuth;
    private DostlarPageAdapter dostlarPageAdapter;
    private TabLayout tabLayout;
    private String gelenuser_id;

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dostlar_yeke_haly_dostlar_hemmesi);


        mAuth=FirebaseAuth.getInstance();
        viewPager=(ViewPager)findViewById(R.id.dostlar_yeke_haly_dostlar_pager);
        firebaseFirestore=FirebaseFirestore.getInstance();

        dostlarPageAdapter=new DostlarPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(dostlarPageAdapter);
        tabLayout=(TabLayout)findViewById(R.id.dostlar_yeke_haly_dostlar_tablayout);
        tabLayout.setupWithViewPager(viewPager);
        gelenuser_id=getIntent().getStringExtra("idi");

      /*  Toast.makeText(Dostlar_yeke_haly_dostlar_hemmesi.this,gelenuser_id,Toast.LENGTH_LONG).show();*/




    }
}
