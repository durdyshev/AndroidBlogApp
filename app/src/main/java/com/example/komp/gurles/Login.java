package com.example.komp.gurles;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private EditText Email;
    private EditText Parol;
    private Button Reg;
    private Button Login;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference userreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Email=(EditText)findViewById(R.id.login_email);
        Parol=(EditText)findViewById(R.id.login_parol);
        Login=(Button)findViewById(R.id.login_gir_knopka);
        Reg=(Button)findViewById(R.id.reg_knopka);
        mAuth = FirebaseAuth.getInstance();
        progressBar=(ProgressBar)findViewById(R.id.login_progress);
        firebaseFirestore=FirebaseFirestore.getInstance();

        Reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Login.this,Register.class);
                startActivity(intent);

            }
        });
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String TextEmail=Email.getText().toString().trim();
                String Textparol=Parol.getText().toString().trim();
                if(!TextUtils.isEmpty(TextEmail) && !TextUtils.isEmpty(Textparol)){
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(TextEmail,Textparol).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String currenuserid=mAuth.getCurrentUser().getUid();
                                String deviceToken=FirebaseInstanceId.getInstance().getToken();
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("token", deviceToken);


                                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                                String uid=user.getUid();
                                userreference= FirebaseDatabase.getInstance().getReference().child("Ulanyjylar").child(uid);
                                HashMap<String,Object> adymap=new HashMap<>();
                                adymap.put("online",true);

                                userreference.setValue(adymap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(Login.this,"reference doredildi",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                                firebaseFirestore.collection("ulanyjylar").document(currenuserid).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   Toast.makeText(Login.this,"Token id",Toast.LENGTH_LONG).show();

                               }
                                }
                            });

                                Intent intent=new Intent(Login.this,MainActivity.class);
                                startActivity(intent);
                                finish();

                            }
                            else{
                                Toast.makeText(Login.this,"Bir mesele ýüze çykdy",Toast.LENGTH_LONG).show();

                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    });

                }
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser user=mAuth.getCurrentUser();

        if(user != null){
            Intent intent=new Intent(Login.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
