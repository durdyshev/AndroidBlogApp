package com.example.komp.gurles;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class EsasyFragment extends Fragment {
    private RecyclerView blog_list_view;
    private List<Postadapter>bloglist;
    private List<String> dostlist;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;
    private Post_adapter_class post_adapter_class;
    private Activity context;


    public EsasyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_esasy, container, false);
context=getActivity();
        bloglist=new ArrayList<>();
        dostlist=new ArrayList<>();
        post_adapter_class=new Post_adapter_class(bloglist);
        blog_list_view=view.findViewById(R.id.blog_list_gornus);
        blog_list_view.setAdapter(post_adapter_class);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        if(mAuth.getCurrentUser()!=null) {

            firebaseFirestore.collection("/ulanyjylar/" + user_id + "/dostlar/").get().addOnCompleteListener(getActivity(),new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            String dostlar = documentSnapshot.getString("user_id");

                            dostlist.add(dostlar);
                        }
                        dostlist.add(user_id);
                        if(!dostlist.isEmpty()){
                            for (int i = 0; i < dostlist.size(); i++) {
                                Query sirala = firebaseFirestore.collection("/ulanyjylar/" + dostlist.get(i) + "/postlar").orderBy("wagt", Query.Direction.DESCENDING);
                                sirala.addSnapshotListener(context,new EventListener<QuerySnapshot>() {
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


                    }
                }


            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


        }













      /*  firebaseFirestore.collection("/ulanyjylar/"+dost_idler()+"/postlar").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
           for(DocumentChange doc:documentSnapshots.getDocumentChanges()){
               if(doc.getType()==DocumentChange.Type.ADDED){
                   Postadapter postadapter=doc.getDocument().toObject(Postadapter.class);
               bloglist.add(postadapter);
               post_adapter_class.notifyDataSetChanged();
               }
           }

            }
        });*/



        // Inflate the layout for this fragment


        return view;
    }



}
