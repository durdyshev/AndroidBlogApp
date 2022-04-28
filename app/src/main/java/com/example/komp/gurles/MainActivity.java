package com.example.komp.gurles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mainToolbar;
    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton post_gos_knopka;
    private String user_id;
    private BottomNavigationView  asakky_knopkalar;
    private Fragment esasyFragment;
    private Fragment bildirisFragment;
    private Fragment profilFragment;
    private Fragment smsFragment;
    private SinchClass sinchClass;
    private Call call;
    private SinchClient sinchClient;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        asakky_knopkalar=(BottomNavigationView)findViewById(R.id.main_bottom_nav);
        BottomNavigationViewHelper.disableShiftMode(asakky_knopkalar);
        firebaseFirestore=FirebaseFirestore.getInstance();


        //Fragmentlar
        esasyFragment=new EsasyFragment();
        bildirisFragment=new BildirisFragment();
        profilFragment=new ProfilFragment();
        smsFragment=new SmsFragment();
        if(mAuth.getCurrentUser()!=null){
            replacefragment(esasyFragment);
            asakky_knopkalar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.menu_esasy:
                            replacefragment(esasyFragment);
                            mainToolbar.setTitle("Home");
                            return true;
                        case R.id.menu_bildirisler:
                            replacefragment(bildirisFragment);
                            mainToolbar.setTitle("Friends");
                            return true;
                        case R.id.menu_sms:
                            replacefragment(smsFragment);
                            mainToolbar.setTitle("Chat");
                            return true;
                        case R.id.menu_profil:
                            replacefragment(profilFragment);
                            mainToolbar.setTitle("Profile");

                            return true;

                        default: return false;

                    }


                }
            });}


        mainToolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Line");
        post_gos_knopka=(FloatingActionButton)findViewById(R.id.post_gos);
        post_gos_knopka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent=new Intent(MainActivity.this,Tazepost.class);
               startActivity(intent);
            }
        });

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            Intent intent=new Intent(MainActivity.this,Login.class);
            startActivity(intent);
            finish();
        }
        else{
            sinchClass=new SinchClass(sinchClient,call,MainActivity.this,mAuth.getCurrentUser().getUid());

            // Informasiyalar firestorda bamy sony barlayas
            user_id=mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                          // status("online");
                        if(!task.getResult().exists()){
                            Intent intent=new Intent(MainActivity.this,Sazlamalar.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                    else{
                        String error=task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Mesele yuze cykdy"+error,Toast.LENGTH_LONG).show();

                    }
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            status("online");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            status("offline");
            songorulme(FieldValue.serverTimestamp());
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_cyk_knopka){

          //  status("offline");
            //songorulme(FieldValue.serverTimestamp());

            try{
                Thread.sleep(1000);
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("token", FieldValue.delete());
                firebaseFirestore.collection("ulanyjylar").document(mAuth.getCurrentUser().getUid()).update(userMap);
                Intent intent=new Intent(MainActivity.this,Login.class);
                startActivity(intent);
                finish();
                mAuth.signOut();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }







        }
        if(item.getItemId()==R.id.menu_sazla_knopka){
            Intent intent=new Intent(MainActivity.this,Sazlamalar.class);
            startActivity(intent);
        }
        if(item.getItemId()== R.id.menu_agza_gos){
            Intent intent=new Intent(MainActivity.this,DostGos.class);
            startActivity(intent);

        }

        return true;
    }

    private void replacefragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.esasy_container,fragment);
        fragmentTransaction.commit();

    }
}