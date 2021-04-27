package com.example.komp.gurles;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class SmsFragment extends Fragment {
    private RecyclerView chat_list;
    private List<Chatadapter> gelenlist;
    private Chatadapterclass chatadapterclass;
    private FirebaseAuth mAuth;
private FirebaseFirestore firebaseFirestore;
private String user_id;


    public SmsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_sms, container, false);
        gelenlist=new ArrayList<>();
        chat_list=view.findViewById(R.id.chat_recycler);
        chatadapterclass=new Chatadapterclass(gelenlist);
        chat_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        chat_list.setAdapter(chatadapterclass);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();



        Query sirala=firebaseFirestore.collection("/ulanyjylar/"+user_id+"/chat").orderBy("time",Query.Direction.DESCENDING);
        sirala.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        Chatadapter  chatadapter=doc.getDocument().toObject(Chatadapter.class);
                        gelenlist.add(chatadapter);
                        chatadapterclass.notifyDataSetChanged();


                    }
                }
            }
        });









        return view;
    }


}
