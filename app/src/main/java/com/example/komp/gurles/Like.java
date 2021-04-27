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

public class Like extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<LikeAdapter> like;
    private LikeAdapterClass likeAdapterClass;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id,gelenpostid,gelenid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        recyclerView=(RecyclerView)findViewById(R.id.like_recycler);
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        like=new ArrayList<>();
        user_id=mAuth.getCurrentUser().getUid();
        gelenpostid=getIntent().getStringExtra("post_id");
        gelenid=getIntent().getStringExtra("idi");

        likeAdapterClass=new LikeAdapterClass(like);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(likeAdapterClass);

        Query sirala=firebaseFirestore.collection("/ulanyjylar/"+gelenid+"/postlar/"+gelenpostid+"/Like").orderBy("wagt",Query.Direction.ASCENDING);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        String BlogPostId = doc.getDocument().getId();
                        LikeAdapter likeAdapter=doc.getDocument().toObject(LikeAdapter.class).within(BlogPostId);
                        like.add(likeAdapter);
                        likeAdapterClass.notifyDataSetChanged();


                    }
                }
            }
        });


    }
}
