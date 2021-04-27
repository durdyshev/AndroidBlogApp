package com.example.komp.gurles;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Profil extends AppCompatActivity {
private CircleImageView profil,profil_kamera,arkafon_kamera;
private ImageView arkafon;
private CardView ady,status,nomer,id;
private FirebaseFirestore firebaseFirestore;
private FirebaseAuth mAuth;
private String user_id;
private TextView tekst_ady,tekst_pikir,tekst_nomer,tekst_id;
private StorageReference storageReference;
private Uri Esasysurat=null;
private String Esasysurat_click="0";
private Dialog dialog;
private List<String> Post;
private List<String> arkafonlist;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        dialog=new Dialog(Profil.this);
        //firebase
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();

        //suratlar
        profil=(CircleImageView)findViewById(R.id.profil_surat);
        profil_kamera=(CircleImageView)findViewById(R.id.profil_camera);
        arkafon_kamera=(CircleImageView)findViewById(R.id.profil_arkafon_camera);
        arkafon=(ImageView)findViewById(R.id.profil_arkafon);
        //tekstler
        tekst_ady=(TextView)findViewById(R.id.profil_ady);
        tekst_pikir=(TextView)findViewById(R.id.profil_pikir);
        tekst_nomer=(TextView)findViewById(R.id.profil_nomer);
        tekst_id=(TextView)findViewById(R.id.profil_id);
        //cardview
        ady=(CardView)findViewById(R.id.profil_cardview_ady);
        status=(CardView)findViewById(R.id.profil_cardview_status);
       nomer=(CardView)findViewById(R.id.profil_cardview_nomer);
        id=(CardView)findViewById(R.id.profil_cardview_id);
        firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name=task.getResult().getString("ady");
                        String image=task.getResult().getString("surat");
                        String id=task.getResult().getString("id");
                        String arkafon_string=task.getResult().getString("arkafon");
                        String pikir=task.getResult().getString("pikir");
                        String nomer=task.getResult().getString("number");

                        if(nomer.equals(null)){
                            tekst_nomer.setText("Nomer yok");
                        }
                        else{
                            tekst_nomer.setText(nomer);
                        }
                        if(id.equals(null)){
                            tekst_id.setText("Id goyulmady");
                        }
                        else{
                            tekst_id.setText(id);
                        }
                        if(pikir.equals(null)){
                            tekst_pikir.setText("Pikir goyulmadyk");
                        }
                        else {
                            tekst_pikir.setText(pikir);
                        }
                        tekst_ady.setText(name);


                        RequestOptions placeholderreq=new RequestOptions();
                        placeholderreq.placeholder(R.mipmap.profil);
                        Glide.with(Profil.this).setDefaultRequestOptions(placeholderreq).load(image).into(profil);

                        RequestOptions profilholder=new RequestOptions();
                        profilholder.placeholder(R.drawable.background);
                        Glide.with(Profil.this).setDefaultRequestOptions(profilholder).load(arkafon_string).into(arkafon);

                    }
                }
                else{

                }

            }
        });

        id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText ad_edit;CircleImageView goybolsun,uytget;TextView obshy_tekst;
                dialog.setContentView(R.layout.uytget_layout);
                obshy_tekst=(TextView)dialog.findViewById(R.id.uytget_layout_tekst);
                ad_edit=(EditText)dialog.findViewById(R.id.uytget_layout_edittext);
                goybolsun=(CircleImageView) dialog.findViewById(R.id.uytget_layout_goybolsun);
                uytget=(CircleImageView) dialog.findViewById(R.id.uytget_layout_uytget_knopka);
                obshy_tekst.setText("Id üvtget");
                ad_edit.setText(tekst_id.getText().toString());
                dialog.show();

                uytget.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String ady_tekst=ad_edit.getText().toString();
                        if(!TextUtils.isEmpty(ady_tekst)){
                        firebaseFirestore.collection("ulanyjylar").whereEqualTo("id",ady_tekst).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                           if(!task.getResult().isEmpty()){
                               for (DocumentSnapshot document : task.getResult()) {
                                   String idi = document.getString("id");

                                   String userid = document.getString("user_id");
                           if(userid.equals(user_id)){
                               Toast.makeText(Profil.this,"Köne id",Toast.LENGTH_LONG).show();
                               dialog.dismiss();
                           }
                               }
                           }
                           else {
                               Map<String,Object>usermap=new HashMap<>();
                               usermap.put("id",ady_tekst);
                               firebaseFirestore.collection("ulanyjylar").document(user_id).update(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       Toast.makeText(Profil.this,"Id üýtgedildi",Toast.LENGTH_LONG).show();
                                       dialog.dismiss();
                                       tekst_id.setText(ady_tekst);
                                   }
                               });

                           }
                            }
                        });

                        }
                    }
                });
                goybolsun.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        ady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText ad_edit;CircleImageView goybolsun,uytget;TextView obshy_tekst;
                dialog.setContentView(R.layout.uytget_layout);
                ad_edit=(EditText)dialog.findViewById(R.id.uytget_layout_edittext);
                goybolsun=(CircleImageView) dialog.findViewById(R.id.uytget_layout_goybolsun);
                uytget=(CircleImageView) dialog.findViewById(R.id.uytget_layout_uytget_knopka);
                obshy_tekst=(TextView)dialog.findViewById(R.id.uytget_layout_tekst);
                ad_edit.setText(tekst_ady.getText().toString());
                obshy_tekst.setText("At Üýtget");
                dialog.show();

                uytget.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String ady_tekst=ad_edit.getText().toString();
                        if(!TextUtils.isEmpty(ady_tekst)){

                            Map<String,Object>usermap=new HashMap<>();
                            usermap.put("ady",ady_tekst);
                        firebaseFirestore.collection("ulanyjylar").document(user_id).update(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                           Toast.makeText(Profil.this,"Ady üýtgedildi",Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            tekst_ady.setText(ady_tekst);
                            }
                        });
                    }
                    }
                });
                goybolsun.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });



                /*Intent intent=new Intent(Profil.this,At_uytget.class);
                               startActivity(intent);*/

            }
        });

 status.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
         Intent intent=new Intent(Profil.this,Pikir_uytget.class);
         startActivity(intent);

     }
 });
        arkafon_kamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Esasysurat_click="0";
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(Profil.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(Profil.this, "Surat almaga rugsat berin.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(Profil.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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
                        .setMinCropResultSize(412,412)
                        .setAspectRatio(1, 1)
                        .start(Profil.this);
            }
        });
        profil_kamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Esasysurat_click="1";
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(Profil.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(Profil.this, "Surat almaga rugsat berin.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(Profil.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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
                        .start(Profil.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Esasysurat=result.getUri();
                final FieldValue randomname=FieldValue.serverTimestamp();


               if(Esasysurat_click.equals("1")){

                   File newImageFile=new File(Esasysurat.getPath());

                   try {
                       compressedImageFile = new Compressor(Profil.this)
                               .setMaxWidth(200)
                               .setMaxHeight(200)
                               .setQuality(75)
                               .compressToBitmap(newImageFile);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   ByteArrayOutputStream baos=new ByteArrayOutputStream();
                   compressedImageFile.compress(Bitmap.CompressFormat.JPEG,50,baos);
                   byte[] thumbdata =baos.toByteArray();
                   UploadTask uploadTask =storageReference.child("profil_surat/kiceldilen")
                           .child(user_id +randomname+ ".jpg").putBytes(thumbdata);
                   uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                           final String kici=task.getResult().getDownloadUrl().toString();
                            Post=new ArrayList<>();
                            Post.add(kici);

                           Map<String, Object> userMap = new HashMap<>();
                           userMap.put("surat", kici);

                           Map<String,Object> postMap=new HashMap<>();
                           postMap.put("surat_url",Post);
                           postMap.put("informasiya","");
                           postMap.put("user_id",user_id);
                           postMap.put("wagt", FieldValue.serverTimestamp());
                           postMap.put("tipi","profil");
                           firebaseFirestore.collection("ulanyjylar").document(user_id).collection("postlar").add(postMap);

                           firebaseFirestore.collection("ulanyjylar").document(user_id).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if(task.isSuccessful()){
                                       Toast.makeText(Profil.this,"Profil surat üytgedildi",Toast.LENGTH_LONG).show();
                                       profil.setImageURI(Esasysurat);
                                   }
                               }
                           });

                       }
                   });



               }
               else{
                   File newImageFile=new File(Esasysurat.getPath());

                   try {
                       compressedImageFile = new Compressor(Profil.this)
                               .setMaxWidth(200)
                               .setMaxHeight(200)
                               .setQuality(75)
                               .compressToBitmap(newImageFile);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   ByteArrayOutputStream baos=new ByteArrayOutputStream();
                   compressedImageFile.compress(Bitmap.CompressFormat.JPEG,50,baos);
                   byte[] thumbdata =baos.toByteArray();
                   UploadTask uploadTask =storageReference.child("arkafon_suratlar/kiceldilen")
                           .child(user_id +randomname+ ".jpg").putBytes(thumbdata);
                   uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                           final String kici=task.getResult().getDownloadUrl().toString();
                           arkafonlist=new ArrayList<>();

                           arkafonlist.add(kici);
                           Map<String, Object> userMap = new HashMap<>();
                           userMap.put("arkafon", kici);

                           Map<String,Object> postMap=new HashMap<>();
                           postMap.put("surat_url",Post);
                           postMap.put("informasiya","");
                           postMap.put("user_id",user_id);
                           postMap.put("wagt", FieldValue.serverTimestamp());
                           postMap.put("tipi","arkafon");
                           firebaseFirestore.collection("ulanyjylar").document(user_id).collection("postlar").add(postMap);



                           firebaseFirestore.collection("ulanyjylar").document(user_id).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if(task.isSuccessful()){
                                       Toast.makeText(Profil.this,"Arkafon üytgedildi",Toast.LENGTH_LONG).show();
                                       RequestOptions placeholderreq=new RequestOptions();
                                       placeholderreq.placeholder(R.drawable.image_placeholder);
                                       Glide.with(Profil.this).setDefaultRequestOptions(placeholderreq).load(Esasysurat).into(arkafon);

                                   }
                               }
                           });

                       }
                   });

               }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
