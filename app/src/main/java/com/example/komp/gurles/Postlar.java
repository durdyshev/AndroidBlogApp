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

public class Postlar extends AppCompatActivity {
    private RecyclerView blog_list_view;
    private List<Postadapter> bloglist;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;
    private Post_adapter_class post_adapter_class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postlar);
        bloglist=new ArrayList<>();

        post_adapter_class=new Post_adapter_class(bloglist);
        blog_list_view=findViewById(R.id.postlar_recycler);
        blog_list_view.setAdapter(post_adapter_class);
        blog_list_view.setLayoutManager(new LinearLayoutManager(this));

        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();


        Query sirala = firebaseFirestore.collection("/ulanyjylar/" + user_id + "/postlar").orderBy("wagt", Query.Direction.DESCENDING);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots != null){
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String BlogPostId = doc.getDocument().getId();
                            Postadapter postadapter = doc.getDocument().toObject(Postadapter.class).within(BlogPostId);

                            bloglist.add(postadapter);
                            post_adapter_class.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}
