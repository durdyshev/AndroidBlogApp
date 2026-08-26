package com.example.komp.gurles;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.justblog.R;
import com.example.justblog.databinding.ActivityDostYekeHalyBinding;
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
    private ActivityDostYekeHalyBinding binding;
    private FirebaseFirestore firebaseFirestore;

    private String gelenuser_id;
    private FirebaseAuth mAuth;
    private String user_id;
    private Uri SuratUri;
    private String image;
    private String ozimage;
    private String ozname;
    private String ozid;
    private Post_adapter_class post_adapter_class;
    private List<Postadapter> bloglist;
    private Dost_yeke_haly_dostlar_adapterclass dost_yeke_haly_dostlar_adapterclass;
    private List<Dost_yeke_haly_dostlar_adapter> dostlist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDostYekeHalyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar2);
        getSupportActionBar().setTitle("Dost");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        gelenuser_id = getIntent().getStringExtra("idi");
        loaddost();


        binding.dostYekeHalyDostHemmesi.setOnClickListener(v -> {
            Intent intent = new Intent(Dost_yeke_haly.this, Dostlar_yeke_haly_dostlar_hemmesi.class);
            intent.putExtra("idi", gelenuser_id);

            startActivity(intent);
        });

        binding.dostYekeHalyFriend.setOnClickListener(v -> {
            Intent intent = new Intent(Dost_yeke_haly.this, Sms_ugrat.class);
            intent.putExtra("id", gelenuser_id);
            intent.putExtra("ady", binding.dostYekeHalyAdy.getText().toString());
            startActivity(intent);
        });
        binding.dostYekeHalyHome.setOnClickListener(v -> {
            Map<String, Object> dost = new HashMap<>();
            dost.put("user_id", user_id);


            firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dost_ugradylan").document(gelenuser_id).set(dost);
            firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("dost_iberen").document(user_id).set(dost).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        binding.dostYekeHalyLinearAyyr.setVisibility(View.VISIBLE);
                        binding.dostYekeHalyLinearGos.setVisibility(View.GONE);
                    }
                }

            });

        });
        binding.dostYekeHalyBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dostlar").document(gelenuser_id).delete();

                Map<String, Object> dost = new HashMap<>();
                dost.put("user_id", user_id);


                firebaseFirestore.collection("ulanyjylar").document(user_id).collection("blok").document(gelenuser_id).set(dost).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        }

                    }
                });
            }
        });

        firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dostlar").document(gelenuser_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    binding.dostYekeHalyLinearAyyr.setVisibility(View.VISIBLE);
                    binding.dostYekeHalyLinearGos.setVisibility(View.GONE);
                } else {
                    binding.dostYekeHalyLinearGos.setVisibility(View.VISIBLE);
                    binding.dostYekeHalyLinearAyyr.setVisibility(View.GONE);
                }
            }
        });

        firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("ady");
                        image = task.getResult().getString("surat");
                        String id = task.getResult().getString("pikir");
                        String arkafon_surat = task.getResult().getString("arkafon");


                        binding.dostYekeHalyAdy.setText(name);
                        binding.dostYekeHalyId.setText(id);

                        RequestOptions placeholderreq = new RequestOptions();
                        placeholderreq.centerCrop();
                        Glide.with(Dost_yeke_haly.this).load(image).apply(placeholderreq).into(binding.dostYekeHalyProfil);
                        RequestOptions profilholder = new RequestOptions();
                        profilholder.placeholder(R.drawable.background);
                        Glide.with(Dost_yeke_haly.this).setDefaultRequestOptions(profilholder).load(arkafon_surat).into(binding.dostYekeHalyArkafon);


                    }
                }
            }


        });


        firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("dostlar").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                String dost_san = String.valueOf(documentSnapshots.size());
                binding.dostYekeHalyDostSan.setText("Dostlar:" + dost_san);
            }
        });
        firebaseFirestore.collection("ulanyjylar").document(gelenuser_id).collection("postlar").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                String post_san = String.valueOf(documentSnapshots.size());
                binding.dostYekeHalyPostSan.setText("Postlar:" + post_san);
            }
        });

        firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        ozname = task.getResult().getString("ady");
                        ozimage = task.getResult().getString("surat");
                        ozid = task.getResult().getString("id");


                    }
                }
            }
        });


    }


    private void loaddost() {
        binding.dostYekeHalyRecycleDostlar.setVisibility(View.VISIBLE);
        binding.dostYekeHalyRecyclePost.setVisibility(View.VISIBLE);


        dostlist = new ArrayList<>();
        dost_yeke_haly_dostlar_adapterclass = new Dost_yeke_haly_dostlar_adapterclass(dostlist);


        binding.dostYekeHalyRecycleDostlar.setAdapter(dost_yeke_haly_dostlar_adapterclass);
        binding.dostYekeHalyRecycleDostlar.setLayoutManager(new GridLayoutManager(Dost_yeke_haly.this, 3));


        Query sirala = firebaseFirestore.collection("/ulanyjylar/" + gelenuser_id + "/dostlar").orderBy("ady", Query.Direction.ASCENDING).limit(6);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {

                        Dost_yeke_haly_dostlar_adapter dost_yeke_haly_dostlar_adapter = doc.getDocument().toObject(Dost_yeke_haly_dostlar_adapter.class);
                        dostlist.add(dost_yeke_haly_dostlar_adapter);
                        dost_yeke_haly_dostlar_adapterclass.notifyDataSetChanged();
                    }
                }

            }
        });

        bloglist = new ArrayList<>();
        post_adapter_class = new Post_adapter_class(bloglist);


        binding.dostYekeHalyRecyclePost.setAdapter(post_adapter_class);
        binding.dostYekeHalyRecyclePost.setLayoutManager(new LinearLayoutManager(Dost_yeke_haly.this));


        firebaseFirestore.collection("/ulanyjylar/" + gelenuser_id + "/postlar").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String BlogPostId = doc.getDocument().getId();
                        Postadapter postadapter = doc.getDocument().toObject(Postadapter.class).within(BlogPostId);
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
