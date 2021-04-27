package com.example.komp.gurles;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dost_yeke_haly extends AppCompatActivity {
    private TextView tekstid;
    private FirebaseFirestore firebaseFirestore;

    private String gelenuser_id;
    private Button hemmesi;
    private CircleImageView profilsurat;
    private TextView Ady,dost_sany,post_sany;
private CircleImageView home,block,friend,info;
private ImageView arkafon;
    private FirebaseAuth mAuth;
    private String user_id;
    private Uri SuratUri;
    private String image;
    private String ozimage;
    private String ozname;
    private String ozid;
    public RecyclerView post_recycler,dost_recycler,info_recycler;
    private Post_adapter_class post_adapter_class;
    private List<Postadapter> bloglist;
    private Dost_yeke_haly_dostlar_adapterclass dost_yeke_haly_dostlar_adapterclass;
    private List<Dost_yeke_haly_dostlar_adapter>dostlist;
    public Toolbar mToolbar;
    private LinearLayout linear_gos,linear_ayyr;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dost_yeke_haly);
        mToolbar=(Toolbar)findViewById(R.id.toolbar2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Dost");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        linear_gos=(LinearLayout)findViewById(R.id.dost_yeke_haly_linear_gos);
        linear_ayyr=(LinearLayout)findViewById(R.id.dost_yeke_haly_linear_ayyr);
        dost_sany=(TextView)findViewById(R.id.dost_yeke_haly_dost_san);
        post_sany=(TextView)findViewById(R.id.dost_yeke_haly_post_san);
        tekstid=(TextView)findViewById(R.id.dost_yeke_haly_id);
        Ady=(TextView)findViewById(R.id.dost_yeke_haly_ady);
        profilsurat=(CircleImageView)findViewById(R.id.dost_yeke_haly_profil);
        arkafon=(ImageView)findViewById(R.id.dost_yeke_haly_arkafon);

        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        firebaseFirestore=FirebaseFirestore.getInstance();
        gelenuser_id= getIntent().getStringExtra("idi");
        home=(CircleImageView)findViewById(R.id.dost_yeke_haly_home);
        block=(CircleImageView)findViewById(R.id.dost_yeke_haly_block);
        friend=(CircleImageView)findViewById(R.id.dost_yeke_haly_friend);
        info=(CircleImageView)findViewById(R.id.dost_yeke_haly_info);
        post_recycler= findViewById(R.id.dost_yeke_haly_recycle_post);
        dost_recycler=findViewById(R.id.dost_yeke_haly_recycle_dostlar);
        info_recycler=findViewById(R.id.dost_yeke_haly_recycle_info);
        hemmesi=(Button)findViewById(R.id.dost_yeke_haly_dost_hemmesi);
        loaddost();









        hemmesi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Dost_yeke_haly.this,Dostlar_yeke_haly_dostlar_hemmesi.class);
                intent.putExtra("idi",gelenuser_id);

                startActivity(intent);
            }
        });

        friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Dost_yeke_haly.this,Sms_ugrat.class);
                intent.putExtra("id",gelenuser_id);
                intent.putExtra("ady",Ady.getText().toString());
                startActivity(intent);
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> dost=new HashMap<>();
                dost.put("user_id",user_id);


               firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dost_ugradylan").document(gelenuser_id).set(dost);
               firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("dost_iberen").document(user_id).set(dost).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful())
                       {linear_ayyr.setVisibility(View.VISIBLE);
                       linear_gos.setVisibility(View.GONE);}
                       }

               });

            }
        });
        block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dostlar").document(gelenuser_id).delete();

                Map<String,Object> dost=new HashMap<>();
                dost.put("user_id",user_id);


                firebaseFirestore.collection("ulanyjylar").document(user_id).collection("blok").document(gelenuser_id).set(dost).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                        }

                    }
                });
            }
        });

firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dostlar").document(gelenuser_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if(documentSnapshot.exists()){
        linear_ayyr.setVisibility(View.VISIBLE);
        linear_gos.setVisibility(View.GONE);
        }
        else{
            linear_gos.setVisibility(View.VISIBLE);
            linear_ayyr.setVisibility(View.GONE);
        }
    }
});

        firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name=task.getResult().getString("ady");
                        image=task.getResult().getString("surat");
                        String id=task.getResult().getString("pikir");
                        String arkafon_surat=task.getResult().getString("arkafon");


                        Ady.setText(name);
                        tekstid.setText(id);

                        RequestOptions placeholderreq=new RequestOptions();
                        placeholderreq.centerCrop();
                        Glide.with(Dost_yeke_haly.this).load(image).apply(placeholderreq).into(profilsurat);
                        RequestOptions profilholder=new RequestOptions();
                        profilholder.placeholder(R.drawable.background);
                        Glide.with(Dost_yeke_haly.this).setDefaultRequestOptions(profilholder).load(arkafon_surat).into(arkafon);


                    }
                }
            }




        });


        firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("dostlar").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
           String dost_san= String.valueOf(documentSnapshots.size());
           dost_sany.setText("Dostlar:"+dost_san);
            }
        });
        firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("postlar").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                String post_san= String.valueOf(documentSnapshots.size());
                post_sany.setText("Postlar:"+post_san);
            }
        });

        firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        ozname=task.getResult().getString("ady");
                        ozimage=task.getResult().getString("surat");
                        ozid=task.getResult().getString("id");



                    }
                }
            }
        });





    }


    private void loaddost() {
        dost_recycler.setVisibility(View.VISIBLE);
        post_recycler.setVisibility(View.VISIBLE);


        dostlist=new ArrayList<>();
        dost_yeke_haly_dostlar_adapterclass= new Dost_yeke_haly_dostlar_adapterclass(dostlist);


        dost_recycler.setAdapter(dost_yeke_haly_dostlar_adapterclass);
        dost_recycler.setLayoutManager(new GridLayoutManager(Dost_yeke_haly.this, 3));


        Query sirala=firebaseFirestore.collection("/ulanyjylar/"+gelenuser_id+"/dostlar").orderBy("ady",Query.Direction.ASCENDING).limit(6);
       sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for(DocumentChange doc:documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){

                        Dost_yeke_haly_dostlar_adapter dost_yeke_haly_dostlar_adapter=doc.getDocument().toObject(Dost_yeke_haly_dostlar_adapter.class);
                       dostlist.add(dost_yeke_haly_dostlar_adapter);
                        dost_yeke_haly_dostlar_adapterclass.notifyDataSetChanged();
                    }
                }

            }
        });

        bloglist=new ArrayList<>();
        post_adapter_class = new Post_adapter_class(bloglist);


        post_recycler.setAdapter(post_adapter_class);
        post_recycler.setLayoutManager(new LinearLayoutManager(Dost_yeke_haly.this));



        firebaseFirestore.collection("/ulanyjylar/"+gelenuser_id+"/postlar").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for(DocumentChange doc:documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        String BlogPostId=doc.getDocument().getId();
                        Postadapter postadapter=doc.getDocument().toObject(Postadapter.class).within(BlogPostId);
                        bloglist.add(postadapter);
                        post_adapter_class.notifyDataSetChanged();
                    }
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







}
