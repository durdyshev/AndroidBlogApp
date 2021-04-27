package com.example.komp.gurles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Sazlamalar extends AppCompatActivity {
    private Toolbar mToolbar;
    private CircleImageView surat;
    private Uri Esasysurat=null;
    private Uri arkafon_uri=null;
    private EditText Ady;
    private EditText Id;
    private Button Sakla;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar sazla_progres;
    private String user_id;
    private FirebaseFirestore firebaseFirestore;
    private Boolean uytgedildi=false;
    private FirebaseAuth mAuth;
    private ImageView arkafon;
    private Boolean arkafon_click=false;
    private Boolean Esasysurat_click=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sazlamalar);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        mToolbar=(Toolbar)findViewById(R.id.sazlamalar_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sazlamalar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        surat=(CircleImageView)findViewById(R.id.sazlamalar_surat);
        Ady=(EditText)findViewById(R.id.sazlamalar_ady);
        Sakla=(Button)findViewById(R.id.sazlamalar_sakla);
        Id=(EditText)findViewById(R.id.sazlamalar_id);
        sazla_progres=(ProgressBar)findViewById(R.id.sazlamalar_progres);
        user_id=firebaseAuth.getCurrentUser().getUid();
        arkafon=(ImageView)findViewById(R.id.sazlamalar_arkafon);
        sazla_progres.setVisibility(View.VISIBLE);
        Sakla.setEnabled(false);
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name=task.getResult().getString("ady");
                        String image=task.getResult().getString("surat");
                        String id=task.getResult().getString("id");

                        Esasysurat= Uri.parse(image);
                        Ady.setText(name);
                        Id.setText(id);

                        RequestOptions placeholderreq=new RequestOptions();
                        placeholderreq.placeholder(R.mipmap.profil);
                        Glide.with(Sazlamalar.this).setDefaultRequestOptions(placeholderreq).load(image).into(surat);
                    }
                }
                else{

                }
                sazla_progres.setVisibility(View.INVISIBLE);
                Sakla.setEnabled(true);
            }
        });

        Sakla.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) { sazla_progres.setVisibility(View.VISIBLE);
                Sakla.setEnabled(false);

                final String userady=Ady.getText().toString();
                final String id=Id.getText().toString();
                if(!TextUtils.isEmpty(userady) && Esasysurat!=null && !TextUtils.isEmpty(id)){
                    if(uytgedildi){

                        final String randomname= FieldValue.serverTimestamp().toString();
                        StorageReference surat_yolu =storageReference.child("profil_suratlar").child(user_id+randomname+".jpg");
                        surat_yolu.putFile(Esasysurat).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if(task.isSuccessful()){
                                    surat_indir(task,userady,id);
                                    Sakla.setEnabled(true);
                                }
                                else{
                                    String error=task.getException().toString();
                                    Toast.makeText(Sazlamalar.this,"Mesele ýüze çykdy:"+error,Toast.LENGTH_LONG).show();
                                }
                                sazla_progres.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    else {
                        surat_indir(null,userady,id);
                    }
                }

            }
        });


        surat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Esasysurat_click=true;
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(Sazlamalar.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(Sazlamalar.this, "Surat almaga rugsat berin.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(Sazlamalar.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        surat_gyrk();

                    }
                }
                else{
                    surat_gyrk();

                }

            }


            private void surat_gyrk() {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(Sazlamalar.this);
            }
        });
    }

    private void surat_indir(@NonNull Task<UploadTask.TaskSnapshot> task, final String userady, final String id) {
        final Uri skacatedilen;
        if(task!=null){
            //Suraty indiryar
            skacatedilen=task.getResult().getDownloadUrl();}
        else{

            skacatedilen=Esasysurat;}


        firebaseFirestore.collection("ulanyjylar").whereEqualTo("id",id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.getResult().isEmpty()){
                    Toast.makeText(Sazlamalar.this, "Id alynan", Toast.LENGTH_LONG).show();
                }

/*if(task.getResult().equals(id)){
    Toast.makeText(Sazlamalar.this, "Oz idiniz", Toast.LENGTH_LONG).show();
}*/
                else{
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("ady", userady);
                    userMap.put("id", id);
                    userMap.put("surat", skacatedilen.toString());
                    userMap.put("user_id", user_id);
                    userMap.put("typing","hickim");
                    firebaseFirestore.collection("ulanyjylar").document(user_id).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Sazlamalar.this, "Uytgedildi", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Sazlamalar.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        }
                    });


                }
            }
        });





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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Esasysurat = result.getUri();
                surat.setImageURI(Esasysurat);
                uytgedildi=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

}
