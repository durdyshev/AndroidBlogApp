package com.example.komp.gurles;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Sms_ugrat extends AppCompatActivity {
    private EditText sms;
    private TextView ady,son;
    private ImageButton sms_ugrat;
    private CircleImageView profil;
    private String gelenuser_id,gelenady,user_id;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView sms_recycler;
    private SmsAdapterClass smsAdapterClass;
    private List<SmsAdapter> gelenlist;
    private ImageView surat_ugrat;
    private String suraturl;
    private Uri SuratUrl=null;
    private String BlogPostId;
    private StorageReference storageReference;
    private DocumentSnapshot lastVisible;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private SinchClient sinchClient;
    private Call call;
    private Bitmap compressedImageFile;
    private ImageButton audio;
    private MediaRecorder recorder=null ;
    private String fileName=null;
    private String audio_san="0";
    private static final String LOG_TAG = "AudioRecordTest";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_ugrat);

        audio=(ImageButton)findViewById(R.id.sms_ugrat_audio);
        fileName= Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName+="/audio_ses"+FieldValue.serverTimestamp().toString()+".3gp";
        ady=(TextView)findViewById(R.id.sms_ugrat_ady);
        son=(TextView)findViewById(R.id.sms_ugrat_last_seen);
        profil=(CircleImageView)findViewById(R.id.sms_ugrat_profil);
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        surat_ugrat=(ImageView)findViewById(R.id.sms_ugrat_surat);
        sms=(EditText)findViewById(R.id.sms_ugrat_tekst);
        sms_ugrat=(ImageButton)findViewById(R.id.sms_ugrat_knopka);
        gelenuser_id= getIntent().getStringExtra("id");
        gelenady= getIntent().getStringExtra("ady");
        suraturl= getIntent().getStringExtra("surat");
        mAuth=FirebaseAuth.getInstance();
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        user_id=mAuth.getCurrentUser().getUid();
        mToolbar=(Toolbar)findViewById(R.id.sms_ugrat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        gelenlist=new ArrayList<>();
        smsAdapterClass= new SmsAdapterClass(gelenlist);
        sms_recycler= findViewById(R.id.sms_ugrat_recycler);
        sms_recycler.setAdapter(smsAdapterClass);

        linearLayoutManager=new LinearLayoutManager(this);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        sms_recycler.setLayoutManager(linearLayoutManager);
        sms_recycler.setHasFixedSize(true);

        firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                String online= documentSnapshot.getString("status");
                String profile=documentSnapshot.getString("surat");
                String yazyar=documentSnapshot.getString("typing");
                Glide.with(Sms_ugrat.this).load(profile).into(profil);
                final Date last_active= documentSnapshot.getDate("son");
                String dateString = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm", new Date(String.valueOf(last_active))).toString();


                if(online.equals("offline")){
                    son.setText(dateString);
                }

                if(online.equals("online")){
                    son.setText("online");
                    if(yazyar.equals(user_id)){
                        son.setText("Yazyar...");
                    }
                }


            }
        });



        //Sms recycler
        Query sirala = firebaseFirestore.collection("/ulanyjylar/"+user_id+"/hatlar/"+gelenuser_id+"/hat").orderBy("time",Query.Direction.DESCENDING).limit(5);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for(DocumentChange doc:documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        BlogPostId=doc.getDocument().getId();
                        SmsAdapter smsAdapter=doc.getDocument().toObject(SmsAdapter.class).within(BlogPostId);

                        gelenlist.add(smsAdapter);
                        // smsAdapterClass.notifyItemRangeInserted(0, documentSnapshots.size() - 1);
                        smsAdapterClass.notifyDataSetChanged();
                        //smsAdapterClass.notifyItemInserted(0);
                        lastVisible = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() -1);



                    }
                }
                Query gorulme=firebaseFirestore.collection("/ulanyjylar/"+gelenuser_id+"/hatlar/"+user_id+"/hat").orderBy("time",Query.Direction.DESCENDING);
                gorulme.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        for(DocumentChange doc:documentSnapshots.getDocumentChanges()){
                            if(doc.getType()==DocumentChange.Type.ADDED){
                                String id=doc.getDocument().getId();
                                SmsAdapter smsAdapter=doc.getDocument().toObject(SmsAdapter.class).within(BlogPostId);
                                Map<String,Object> map=new HashMap<>();
                                map.put("seen",true);
                                firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("hatlar")
                                        .document(user_id).collection("hat").document(id).update(map);
                            }
                        }

                    }
                });
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadmoremessage();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        //   Glide.with(Sms_ugrat.this).load(suraturl).into(profil);

        ady.setText(gelenady);
        ady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sms_ugrat.this, Dost_yeke_haly.class);
                intent.putExtra("idi", gelenuser_id);
                startActivity(intent);
            }
        });



        sinchClient = Sinch.getSinchClientBuilder().context(this)
                .applicationKey("9745356e-28c7-4fcc-a6bf-3808ca8edef3")
                .applicationSecret("CiiyXLMqQku3PfzKZkJv0g==")
                .environmentHost("clientapi.sinch.com")
                .userId(user_id)
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.setSupportManagedPush(true);

        sinchClient.getCallClient().addCallClientListener(new CallClientListener() {
            @Override
            public void onIncomingCall(CallClient callClient, final Call incomingcall) {
                final AlertDialog alertDialog=new AlertDialog.Builder(Sms_ugrat.this).create();
                alertDialog.setTitle("Jan gelyar");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "Ocur", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                        call.hangup();
                    }
                });
                alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Ac", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        call=incomingcall;
                        call.answer();
                        call.addCallListener(new SinchCallListener());
                        Toast.makeText(Sms_ugrat.this,"Jan baslady",Toast.LENGTH_LONG).show();
                    }
                });

//                alertDialog.show();
            }
        });
        sinchClient.start();


        audio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    audio_san="1";
                    startRecording();
                    Toast.makeText(Sms_ugrat.this,"Ses yazgy baslady",Toast.LENGTH_LONG).show();
                }
                else if(event.getAction()==MotionEvent.ACTION_UP ){

                    audio_san="0";
                    stopRecording();
                    Toast.makeText(Sms_ugrat.this,"Yazgy gutardy",Toast.LENGTH_LONG).show();
                }
                else if (event.getAction()==MotionEvent.ACTION_BUTTON_PRESS){
                    Toast.makeText(Sms_ugrat.this,"Basyly tutun",Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sms_ugrat.this, Dost_yeke_haly.class);
                intent.putExtra("idi", gelenuser_id);
                startActivity(intent);
            }
        });



        sms.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!=0){
                    audio.setVisibility(View.GONE);
                    sms_ugrat.setVisibility(View.VISIBLE);
                    typingonline();

                }
                else{


                    audio.setVisibility(View.VISIBLE);
                    sms_ugrat.setVisibility(View.GONE);
                    typingoffline();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //       sms_ugrat.setVisibility(View.GONE);
            }
        });
        sms_ugrat.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                if(!sms.getText().toString().isEmpty()){
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("time",  FieldValue.serverTimestamp());
                    userMap.put("message", sms.getText().toString());
                    userMap.put("type","text");
                    userMap.put("seen",false);
                    userMap.put("from", user_id);
                    userMap.put("blogpost","");

                    Map<String, Object> map = new HashMap<>();
                    map.put("time",  FieldValue.serverTimestamp());
                    map.put("from",gelenuser_id);


                    Map<String, Object> chatmap = new HashMap<>();
                    chatmap.put("time",  FieldValue.serverTimestamp());
                    chatmap.put("from",user_id);


                    Map<String,String> not=new HashMap<>();
                    not.put("message",sms.getText().toString());
                    not.put("from",user_id);
                    not.put("type","message");

                    firebaseFirestore.collection("/ulanyjylar/"+gelenuser_id+"/smsNotification").add(not);


                    firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("hatlar").document(user_id).collection("hat").add(userMap);

                    firebaseFirestore.collection("ulanyjylar").document(user_id).collection("chat").document(gelenuser_id).set(map);
                    firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("chat").document(user_id).set(chatmap);


                    firebaseFirestore.collection("ulanyjylar").document(user_id).collection("hatlar").document(gelenuser_id).collection("hat").add(userMap);

                    sms.setText("");


                }

            }
        });

        surat_ugrat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(412,412)
                        // .setAspectRatio(2, 1)
                        .start(Sms_ugrat.this);
            }
        });
    }

    private void loadmoremessage() {

        if (lastVisible != null){
            Query next = firebaseFirestore.collection("/ulanyjylar/" + user_id + "/hatlar/" + gelenuser_id + "/hat").
                    orderBy("time", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(5);
            next.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {
                        lastVisible = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() - 1);


                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                BlogPostId = doc.getDocument().getId();
                                SmsAdapter smsAdapter = doc.getDocument().toObject(SmsAdapter.class).within(BlogPostId);
                                gelenlist.add(smsAdapter);
                                smsAdapterClass.notifyDataSetChanged();
                                sms_recycler.scrollToPosition(gelenlist.size() - 1);


                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                SuratUrl=result.getUri();
                final String randomname=FieldValue.serverTimestamp().toString();


                File newImageFile=new File(SuratUrl.getPath());

                try {
                    compressedImageFile = new Compressor(Sms_ugrat.this)
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
                UploadTask uploadTask =storageReference.child("sms_surat")
                        .child(user_id +randomname+ ".jpg").putBytes(thumbdata);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            final String skacatedilen=task.getResult().getDownloadUrl().toString();
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("message", skacatedilen);
                            userMap.put("seen", false);
                            userMap.put("type","surat");
                            userMap.put("time", FieldValue.serverTimestamp());
                            userMap.put("from", user_id);
                            userMap.put("blogpost","");
                            firebaseFirestore.collection("ulanyjylar").document(user_id).collection("hatlar").document(gelenuser_id).collection("hat").add(userMap);
                            firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("hatlar").document(user_id).collection("hat").add(userMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(Sms_ugrat.this,"Ugradyldy",Toast.LENGTH_LONG).show();

                                    }

                                }
                            });


                        }

                    }
                });



               /* StorageReference yol=storageReference.child("sms_surat").child(user_id+randomname+ ".jpg");
                yol.putFile(SuratUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        final String skacatedilen=task.getResult().getDownloadUrl().toString();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("message", skacatedilen);
                        userMap.put("seen", false);
                        userMap.put("type","surat");
                        userMap.put("time", FieldValue.serverTimestamp());
                        userMap.put("from", user_id);
                        firebaseFirestore.collection("ulanyjylar").document(user_id).collection("hatlar").document(gelenuser_id).collection("hat").add(userMap);
                        firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("hatlar").document(user_id).collection("hat").add(userMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Sms_ugrat.this,"Ugradyldy",Toast.LENGTH_LONG).show();

                                }

                            }
                        });
                    }
                });*/

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sms_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.sms_menu_telefon){
            if(call==null){
                call=sinchClient.getCallClient().callUser(gelenuser_id);
                call.addCallListener(new SinchCallListener());
                opencallerdialog(call);
            }
        }
        if(item.getItemId()==R.id.sms_menu_video){
            if(call==null){
                call=sinchClient.getCallClient().callUserVideo(gelenuser_id);
                call.addCallListener(new VideoCallListener() {
                    @Override
                    public void onVideoTrackAdded(Call call) {
                        // Get a reference to your SinchClient, in the samples this is done through the service interface:
                        VideoController vc = sinchClient.getVideoController();
                        View myPreview = vc.getLocalView();
                        View remoteView = vc.getRemoteView();
                    }

                    @Override
                    public void onVideoTrackPaused(Call call) {

                    }

                    @Override
                    public void onVideoTrackResumed(Call call) {

                    }

                    @Override
                    public void onCallProgressing(Call call) {

                    }

                    @Override
                    public void onCallEstablished(Call call) {

                    }

                    @Override
                    public void onCallEnded(Call call) {

                    }

                    @Override
                    public void onShouldSendPushNotification(Call call, List<PushPair> list) {

                    }
                });
                opencallerdialog(call);
            }
        }

        return true;
    }

    private void opencallerdialog(final Call call) {
        final AlertDialog alertDialog1=new AlertDialog.Builder(Sms_ugrat.this).create();
        alertDialog1.setMessage("Jan edilyar");
        alertDialog1.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "Ocur", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog1.dismiss();
                call.hangup();
            }
        });
        alertDialog1.show();

    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallProgressing(Call call) {
            Toast.makeText(Sms_ugrat.this,"Jan baslady",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCallEstablished(Call call) {
            Toast.makeText(Sms_ugrat.this,"Ulasildi",Toast.LENGTH_LONG).show();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call endedcall) {
            Toast.makeText(Sms_ugrat.this,"Jan gutardy",Toast.LENGTH_LONG).show();


            call = null;
            SinchError a = endedcall.getDetails().getError();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
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
        typingoffline();
       /* if(!smsAdapterClass.mediaPlayer.equals(null)){
        smsAdapterClass.mediaPlayer.reset();}*/

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

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {

            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {

        recorder.stop();
        recorder.release();
        recorder = null;
        uploadtask();

    }

    private void uploadtask() {
        StorageReference audio_yol=storageReference.child("audio").child(user_id+FieldValue.serverTimestamp()+".3gp");
        Uri uri=Uri.fromFile(new File(fileName));
        audio_yol.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    String download=task.getResult().getDownloadUrl().toString();
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("message", download);
                    userMap.put("seen", false);
                    userMap.put("type","audio");
                    userMap.put("time", FieldValue.serverTimestamp());
                    userMap.put("from", user_id);
                    userMap.put("blogpost","");
                    firebaseFirestore.collection("ulanyjylar").document(user_id).collection("hatlar").document(gelenuser_id).collection("hat").add(userMap);
                    firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("hatlar").document(user_id).collection("hat").add(userMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Sms_ugrat.this,"Ugradyldy",Toast.LENGTH_LONG).show();

                            }

                        }
                    });

                }

            }
        });
    }
    private void typingonline(  ){
        Map<String,Object> Mappo=new HashMap<>();
        Mappo.put("typing",gelenuser_id);
        firebaseFirestore.collection("ulanyjylar").document(user_id).update(Mappo);

    }
    private void typingoffline(){
        Map<String,Object> elmappo=new HashMap<>();
        elmappo.put("typing","hickim");
        firebaseFirestore.collection("ulanyjylar").document(user_id).update(elmappo);

    }
}

