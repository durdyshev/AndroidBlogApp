package com.example.komp.gurles;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText Regemail;
    private EditText Regparol;
    private EditText Regparolgaytala;
    private Button regdoret;
    private Button reglogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseFirestore=FirebaseFirestore.getInstance();
        Regemail=(EditText)findViewById(R.id.reg_email);
        Regparol=(EditText)findViewById(R.id.reg_parol);
        Regparolgaytala=(EditText)findViewById(R.id.reg_parol_gaytala);
        regdoret=(Button) findViewById(R.id.reg_doret_knopka);
        reglogin=(Button) findViewById(R.id.reg_gir);
        mAuth=FirebaseAuth.getInstance();
        reglogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);

            }
        });


        regdoret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Email=Regemail.getText().toString();
                String Parol=Regparol.getText().toString();
                String Parol2=Regparolgaytala.getText().toString();
                if(!TextUtils.isEmpty(Email) && !TextUtils.isEmpty(Parol) && !TextUtils.isEmpty(Parol2)){
                    if(Parol.equals(Parol2)) {
                        mAuth.createUserWithEmailAndPassword(Email, Parol)
                                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            String currenuserid=mAuth.getCurrentUser().getUid();
                                            String deviceToken=FirebaseInstanceId.getInstance().getToken();
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("token", deviceToken);

                                            firebaseFirestore.collection("ulanyjylar").document(currenuserid).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(Register.this,"Token id",Toast.LENGTH_LONG).show();

                                                    }
                                                }
                                            });
                                            Toast.makeText(Register.this, "Akkaunt döredildi.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(Register.this, Sazlamalar.class);
                                            startActivity(intent);
                                            finish();

                                        } else {


                                            Toast.makeText(Register.this, "Bir mesele ýüze çykdy.", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                    }
                    else{
                        Toast.makeText(Register.this, "Parollar den dal", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(Register.this, "Boş ýer galdy", Toast.LENGTH_SHORT).show();

                }}
        });



    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            mainugrat();
        }
    }

    private void mainugrat() {
        Intent intent =new Intent(Register.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
