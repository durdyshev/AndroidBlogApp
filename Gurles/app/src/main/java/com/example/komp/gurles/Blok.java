package com.example.komp.gurles;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Blok extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<BlokAdapter> gelenlist;
    private BlokAdapterClass blokAdapterClass;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blok);
        recyclerView=(RecyclerView)findViewById(R.id.blok_recycler);
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        gelenlist=new ArrayList<>();
        user_id=mAuth.getCurrentUser().getUid();

        blokAdapterClass=new BlokAdapterClass(gelenlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(blokAdapterClass);

        Query sirala=firebaseFirestore.collection("/ulanyjylar/"+user_id+"/blok").orderBy("ady",Query.Direction.ASCENDING);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        BlokAdapter blokAdapter=doc.getDocument().toObject(BlokAdapter.class);
                        gelenlist.add(blokAdapter);
                        blokAdapterClass.notifyDataSetChanged();


                    }
                }
            }
        });

    }
}
