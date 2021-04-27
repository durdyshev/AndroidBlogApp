package com.example.komp.gurles;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
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

public class Saklanan extends AppCompatActivity {
    private RecyclerView saklan_recycler;
    private List<SaklananAdapter> postlar;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;
    private SaklananAdapterClass saklananAdapterClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saklanan);
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        postlar=new ArrayList<>();

        saklananAdapterClass=new SaklananAdapterClass(postlar);
        saklan_recycler=findViewById(R.id.saklanan_recycler);
        saklan_recycler.setAdapter(saklananAdapterClass);
        saklan_recycler.setLayoutManager(new GridLayoutManager(this,3));

        Query sirala = firebaseFirestore.collection("/ulanyjylar/" + user_id + "/saklanan").orderBy("wagt", Query.Direction.DESCENDING);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots != null){
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            SaklananAdapter saklananAdapter= doc.getDocument().toObject(SaklananAdapter.class);

                            postlar.add(saklananAdapter);
                            saklananAdapterClass.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}
