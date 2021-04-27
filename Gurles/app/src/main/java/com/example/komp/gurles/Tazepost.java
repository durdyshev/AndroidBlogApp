package com.example.komp.gurles;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


// Ilki activity result bilen crop edilyar son surat uri cevrilyar sonam storage reference bilen yuklenyar
//         firebaseStorage=FirebaseStorage.getInstance().getReference();
//        firestore=FirebaseFirestore.getInstance();
//Firebase storage bilenem yzyna yuklenyar



public class Tazepost extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageView Surat;
    private EditText Info;
    private Button Sakla;
    private Uri SuratUrl=null;
    private ProgressBar Postprogress;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;
    private Bitmap compressedImageFile;
    private ImageButton btn;
    private GridView gvGallery;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;
    private GalleryAdapter galleryAdapter;
    private List<Uri>mArrayUri=null;
    private String yoliki[]=null;
     private int i;
    private List<String> imagePathList=null;
    private CircleImageView post_video;
    public static final int IMAGE=0;
    public static final int VIDEO=2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tazepost);

        btn = findViewById(R.id.btn);
        gvGallery = (GridView)findViewById(R.id.gv);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user=mAuth.getCurrentUser();
        user_id=user.getUid();
        mToolbar=(Toolbar)findViewById(R.id.tazepost_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Surat=(ImageView)findViewById(R.id.post_surat);
        Info=(EditText)findViewById(R.id.tazepost_info);
        Sakla=(Button)findViewById(R.id.tazepost_paylas_knopka);
        Postprogress=(ProgressBar)findViewById(R.id.tazepost_progress);
        Postprogress.setVisibility(View.INVISIBLE);
        post_video=(CircleImageView)findViewById(R.id.tazepost_video);


        post_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");

                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), VIDEO);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });
        Surat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(412,412)
                        // .setAspectRatio(2, 1)
                        .start(Tazepost.this);
            }
        });
        Sakla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String informasiya=Info.getText().toString();
                final List<String> ikincilist;
                ikincilist=new ArrayList<>();
                if(!TextUtils.isEmpty(informasiya) && mArrayUri!= null ){
                    final FieldValue randomname=FieldValue.serverTimestamp();

                    Postprogress.setVisibility(View.VISIBLE);
                    for( i=0;i<imagePathList.size();i++) {
                        final String san= String.valueOf(i);
                        final String n=String.valueOf(mArrayUri.size()-1);

                        File newImageFile=new File(imagePathList.get(i));

                        try {
                            compressedImageFile = new Compressor(Tazepost.this)
                                    .setMaxWidth(200)
                                    .setMaxHeight(200)
                                    .setQuality(75)
                                    .compressToBitmap(newImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos=new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG,75,baos);
                        byte[] thumbdata =baos.toByteArray();
                        UploadTask uploadTask =storageReference.child("post_surat/kiceldilen")
                                .child(user_id +randomname+i+ ".jpg").putBytes(thumbdata);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    String kici=task.getResult().getDownloadUrl().toString();
                                    ikincilist.add(kici);



                                    Map<String,Object> postMap=new HashMap<>();
                                    postMap.put("surat_url",ikincilist);
                                    postMap.put("informasiya",informasiya);
                                    postMap.put("user_id",user_id);
                                    postMap.put("wagt", FieldValue.serverTimestamp());
                                    postMap.put("tipi","post");
                                     if(san.equals(n)){
                                    firebaseFirestore.collection("ulanyjylar").document(user_id).collection("postlar").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(Tazepost.this,"Post gosuldy",Toast.LENGTH_LONG).show();
                                                Intent intent=new Intent(Tazepost.this,MainActivity.class);
                                                startActivity(intent);
                                                finish();

                                            }
                                            else{
                                                Postprogress.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                     }

                                }

                            }
                        });


                    }


                }


            }




        });


    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      //  yol=new ArrayList<String>();
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                imagePathList=new ArrayList<>();
                if(data.getData()!=null){

                    Uri mImageUri=data.getData();


                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    cursor.close();

                    mArrayUri = new ArrayList<Uri>();

                    imagePathList=new ArrayList<String>();

                    mArrayUri.add(mImageUri);
                   getImageFilePath(mImageUri);


                    galleryAdapter = new GalleryAdapter(getApplicationContext(), (ArrayList<Uri>) mArrayUri);
                    gvGallery.setAdapter(galleryAdapter);
                    gvGallery.setVerticalSpacing(gvGallery.getHorizontalSpacing());
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) gvGallery
                            .getLayoutParams();
                    mlp.setMargins(0, gvGallery.getHorizontalSpacing(), 0, 0);

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                       mArrayUri = new ArrayList<Uri>();
                        imagePathList=new ArrayList<String>();

                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            getImageFilePath(uri);


                            mArrayUri.add(uri);



                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                            galleryAdapter = new GalleryAdapter(getApplicationContext(), (ArrayList<Uri>) mArrayUri);
                            gvGallery.setAdapter(galleryAdapter);
                            gvGallery.setVerticalSpacing(gvGallery.getHorizontalSpacing());
                            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) gvGallery
                                    .getLayoutParams();
                            mlp.setMargins(0, gvGallery.getHorizontalSpacing(), 0, 0);

                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                    }
                }
            } /*else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }*/
             else if(requestCode==VIDEO && resultCode==RESULT_OK && null!=data){
                 Postprogress.setVisibility(View.VISIBLE);
                SuratUrl=data.getData();
                 final String randomname=FieldValue.serverTimestamp().toString();
                 StorageReference yol=storageReference.child("video").child(user_id+randomname+ ".mp4");
                 yol.putFile(SuratUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            String kici = task.getResult().getDownloadUrl().toString();
                            ArrayList<String>kicisurat=new ArrayList<>();
                            kicisurat.add(kici);
                            Map<String,Object> postMap=new HashMap<>();
                            postMap.put("surat_url",kicisurat);
                            postMap.put("informasiya",Info.getText().toString());
                            postMap.put("user_id",user_id);
                            postMap.put("wagt", FieldValue.serverTimestamp());
                            postMap.put("tipi","video");

                                firebaseFirestore.collection("ulanyjylar").document(user_id).collection("postlar").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(Tazepost.this,"Post gosuldy",Toast.LENGTH_LONG).show();
                                            Postprogress.setVisibility(View.INVISIBLE);
                                            Intent intent=new Intent(Tazepost.this,MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else{
                                            Postprogress.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });


                        }
                     }
                 });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
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

    public void getImageFilePath(Uri uri) {

        File file = new File(uri.getPath());
        String[] filePath = file.getPath().split(":");
        String image_id = filePath[filePath.length - 1];

        Cursor cursor = getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor!=null) {
            cursor.moveToFirst();
            
           String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            imagePathList.add(imagePath);
            cursor.close();
        }
    }
}


