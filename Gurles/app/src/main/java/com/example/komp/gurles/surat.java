package com.example.komp.gurles;

import android.app.AlertDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class surat extends AppCompatActivity {
    private ViewPager viewPager;
    private List<String> suraturl;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private ViewpagerAdapter adapter;
    private int dotscount=0;
    private ImageView[] dots;
private LinearLayout linearLayout;
private TextView surat_sanaw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surat);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        viewPager=(ViewPager) findViewById(R.id.surat_ac_viewpager);
        linearLayout=(LinearLayout)findViewById(R.id.surat_ac_linear_dots);
        surat_sanaw=(TextView)findViewById(R.id.surat_ac_sanaw);
        suraturl= getIntent().getStringArrayListExtra("surat");
        ugrat(suraturl);

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
    public void ugrat(List<String> suraturl) {
        adapter = new ViewpagerAdapter(this, suraturl);
        viewPager.setAdapter(adapter);
        dotscount=0;
        dotscount = adapter.getCount();
        dots = new ImageView[dotscount];
        linearLayout.removeAllViews();

        for (int i = 0; i < dotscount; i++) {

            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.non_active_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

            params.setMargins(8, 0, 8, 0);

            linearLayout.addView(dots[i], params);

        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dot));
        surat_sanaw.setText("1/"+dotscount);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int i;
                for( i = 0; i< dotscount; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(surat.this, R.drawable.non_active_dot));
                    surat_sanaw.setText((position+1)+"/"+dotscount);
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(surat.this, R.drawable.active_dot));
                surat_sanaw.setText((position+1)+"/"+dotscount);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });





    }
}
