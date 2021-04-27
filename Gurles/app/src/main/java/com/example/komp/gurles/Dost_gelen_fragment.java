package com.example.komp.gurles;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Dost_gelen_fragment extends Fragment {



    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    public RecyclerView recyclerView;
    private Dost_gelen_adapterclass dost_gelen_adapterclass;
    private List<Dost_gelen_adapter> gelenlist;
    private String user_id;




    public Dost_gelen_fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_dost_gelen_fragment, container, false);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        gelenlist=new ArrayList<>();
        dost_gelen_adapterclass = new Dost_gelen_adapterclass(gelenlist);
        recyclerView=view.findViewById(R.id.dost_gos_recycler);
        recyclerView.setAdapter(dost_gelen_adapterclass);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        firebaseFirestore.collection("/ulanyjylar/"+user_id+"/dost_iberen").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for(DocumentChange doc:documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        Dost_gelen_adapter postadapter=doc.getDocument().toObject(Dost_gelen_adapter.class);
                        gelenlist.add(postadapter);
                        dost_gelen_adapterclass.notifyDataSetChanged();
                    }
                }

            }
        });

        return view;
    }
}
