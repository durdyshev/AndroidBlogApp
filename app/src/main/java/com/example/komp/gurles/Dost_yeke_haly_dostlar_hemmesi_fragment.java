package com.example.komp.gurles;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
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
public class Dost_yeke_haly_dostlar_hemmesi_fragment extends Fragment {
    private List<Obshydostlar_adapter> dostlar;
    private dost_yeke_haly_hemmesi_adapterclass2 dostyekehalyhemmesiadapterclass2;
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;

private String gelenuser_id;
    public Dost_yeke_haly_dostlar_hemmesi_fragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view=inflater.inflate(R.layout.fragment_dost_yeke_haly_dostlar_hemmesi_fragment, container, false);

        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();


        recyclerView=view.findViewById(R.id.fragment_dost_yeke_haly_dostlar_fragment_recycler);
        dostlar=new ArrayList<>();
dostyekehalyhemmesiadapterclass2=new dost_yeke_haly_hemmesi_adapterclass2(dostlar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(dostyekehalyhemmesiadapterclass2);
        gelenuser_id=getActivity().getIntent().getStringExtra("idi");

        Query sirala=firebaseFirestore.collection("/ulanyjylar/"+gelenuser_id+"/dostlar").orderBy("ady",Query.Direction.ASCENDING);
        sirala.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        Obshydostlar_adapter obshydostlar_adapter=doc.getDocument().toObject(Obshydostlar_adapter.class);

                        dostlar.add(obshydostlar_adapter);
                        dostyekehalyhemmesiadapterclass2.notifyDataSetChanged();


                    }
                }
            }
        });





        //Toast.makeText(getActivity(),gelenuser_id+"fragment",Toast.LENGTH_LONG).show();


      return  view;
    }

}
