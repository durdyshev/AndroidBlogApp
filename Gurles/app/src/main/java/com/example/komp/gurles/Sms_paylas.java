package com.example.komp.gurles;

import android.content.Intent;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ThrowOnExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sms_paylas extends AppCompatActivity {
    private RecyclerView dost_list;
    private List<Obshydostlar_adapter> dostlar;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;
    private Sms_paylas_adapterclass sms_paylas_adapterclass;
    private EditText tekst;
    private ImageButton ugrat;
    private String gelensurat;
    private String blogpostid;
    private String id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_paylas);
        dostlar=new ArrayList<>();
        gelensurat= getIntent().getStringExtra("surat");
        blogpostid=getIntent().getStringExtra("blogpost");
        id=getIntent().getStringExtra("id");
        ugrat=(ImageButton)findViewById(R.id.sms_paylas_ugrat);
        dost_list=(RecyclerView)findViewById(R.id.sms_paylas_recycler);
        sms_paylas_adapterclass=new Sms_paylas_adapterclass(dostlar);
        tekst=(EditText) findViewById(R.id.sms_paylas_edit);
        dost_list.setLayoutManager(new LinearLayoutManager(Sms_paylas.this));
        dost_list.setAdapter(sms_paylas_adapterclass);
        firebaseFirestore= FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();

        Query sirala=firebaseFirestore.collection("/ulanyjylar/"+user_id+"/dostlar").orderBy("ady",Query.Direction.ASCENDING);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        Obshydostlar_adapter obshydostlar_adapter=doc.getDocument().toObject(Obshydostlar_adapter.class);
                        dostlar.add(obshydostlar_adapter);
                        sms_paylas_adapterclass.notifyDataSetChanged();



                    }
                }
            }
        });


        ugrat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
if(sms_paylas_adapterclass.idler.size()==0){
    Toast.makeText(Sms_paylas.this,"Dost saylamadynyz",Toast.LENGTH_LONG).show();
}
else{ Map<String, Object> userMap = new HashMap<>();
    userMap.put("message", id);
    userMap.put("type","paylas");
    userMap.put("seen",false);
    userMap.put("time",  FieldValue.serverTimestamp());
    userMap.put("from", user_id);
    userMap.put("blogpost",blogpostid);




    Map<String,String> not=new HashMap<>();
    not.put("message","post ugratdy");
    not.put("from",user_id);
    not.put("type","message");
    for(int i=0;i<sms_paylas_adapterclass.idler.size();i++){
        Map<String, Object> dostmap = new HashMap<>();
        dostmap.put("time",  FieldValue.serverTimestamp());
        dostmap.put("from",user_id);
        Map<String, Object> ozmap = new HashMap<>();
        ozmap.put("time",  FieldValue.serverTimestamp());
        ozmap.put("from",sms_paylas_adapterclass.idler.get(i));

        firebaseFirestore.collection("/ulanyjylar/"+sms_paylas_adapterclass.idler.get(i)+"/smsNotification").add(not);



        firebaseFirestore.collection("ulanyjylar").document(user_id).collection("chat").document(sms_paylas_adapterclass.idler.get(i)).set(ozmap);
        firebaseFirestore.collection("ulanyjylar").document(sms_paylas_adapterclass.idler.get(i)).collection("chat").document(user_id).set(dostmap);


                 // tekst.setText(sms_paylas_adapterclass.idler.get(i));


        firebaseFirestore.collection("ulanyjylar").document(user_id).collection("hatlar").document(sms_paylas_adapterclass.idler.get(i)).collection("hat").add(userMap);
        firebaseFirestore.collection("ulanyjylar").document(sms_paylas_adapterclass.idler.get(i)).collection("hatlar").document(user_id).collection("hat").add(userMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Intent intent=new Intent(Sms_paylas.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

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
    }
    private void status(String boslyk) {
        Map<String, Object> status = new HashMap<>();
        status.put("status", boslyk);

        firebaseFirestore.collection("/ulanyjylar/").document(mAuth.getCurrentUser().getUid()).update(status);
    }
}
