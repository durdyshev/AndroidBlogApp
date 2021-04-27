package com.example.komp.gurles;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class BildirisFragment extends Fragment {
    private RecyclerView dost_list;
    private List<Obshydostlar_adapter> dostlar;
    private ArrayList<String> ady;
    private ArrayList<String> number;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;
    private Obshydostlar_adapterclass obshydostlar_adapterclass;
    private CircleImageView profil_image;
    private TextView profil_ady,profil_pikir;

    private int i;
    DocumentSnapshot lastVisible;

    public BildirisFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_bildiris,container,false);

        dostlar=new ArrayList<>();
        dost_list=view.findViewById(R.id.dost_list_gornus);
        obshydostlar_adapterclass=new Obshydostlar_adapterclass(dostlar);
        dost_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        dost_list.setAdapter(obshydostlar_adapterclass);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        profil_image=view.findViewById(R.id.fragment_bildiris_profil_surat);
        profil_ady=view.findViewById(R.id.fragment_bildiris_profil_ady);
        profil_pikir=view.findViewById(R.id.fragment_bildiris_profil_pikir);


        firebaseFirestore.collection("ulanyjylar").document(user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(isAdded()) {
                    String ady = documentSnapshot.getString("ady");
                    String profil = documentSnapshot.getString("surat");
                    String pikir = documentSnapshot.getString("pikir");
                    profil_ady.setText(ady);
                    profil_pikir.setText(pikir);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.centerInside();
                    Glide.with(getActivity()).load(profil).apply(requestOptions).into(profil_image);

                }
            }
        });


        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Kontaklary almaga rugsat berin.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
            } else {
                loadcontact();

            }
        }
        else {
            loadcontact();


        }

        // Inflate the layout for this fragment
        return view;
    }

    private void loadcontact() {

        ady=new ArrayList<>();number=new ArrayList<>();
        ContentResolver contentResolver=getContext().getContentResolver();
        final Cursor cursor=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,null,null,ContactsContract.Contacts.DISPLAY_NAME+" ASC");
        while (cursor.moveToNext()){

          final  String name=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
          final  String nomer=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll(" ","");

if(name.isEmpty() && !nomer.isEmpty() ){ady.add(nomer);number.add(nomer);}
if(!name.isEmpty() && !nomer.isEmpty()){ady.add(name);number.add(nomer);}







        }




        cursor.close();

        firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
               if(task.isSuccessful()){
                   if (task.getResult().exists()){
                    String kontakt=task.getResult().getString("kontakt");


                       if(!kontakt.equals(String.valueOf(number.size()))){


                           Map<String,Object> kontakto=new HashMap<>();
                           kontakto.put("kontakt",String.valueOf(number.size()));
                           firebaseFirestore.collection("ulanyjylar").document(user_id).update(kontakto);
                           for(i=0;i<number.size();i++){
                               Log.d(TAG,number.get(i));
                               Log.d(TAG,ady.get(i));
                               final String dost_ady=ady.get(i);

                               firebaseFirestore.collection("ulanyjylar").whereEqualTo("number",number.get(i)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                   @Override
                                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                       if(!task.getResult().isEmpty()){
                                           for (DocumentSnapshot document : task.getResult()) {
                                               final String idi=document.getString("user_id");
                                               firebaseFirestore.collection("ulanyjylar").document(user_id).collection("blok").whereEqualTo("user_id",idi).get()
                                                       .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                               if(task.getResult().isEmpty()){
                                                                   Map<String,Object> postMap=new HashMap<>();
                                                                   postMap.put("user_id",idi);
                                                                   postMap.put("ady",dost_ady);

                                                                   if(!idi.equals(user_id)){
                                                                       firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dostlar").document(idi).set(postMap)
                                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                   @Override
                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                       Toast.makeText(getContext(),"Basarili",Toast.LENGTH_LONG).show();
                                                                                   }
                                                                               });
                                                                   }
                                                               }

                                                           }
                                                       });
                                           }
                                       }
                                   }
                               });

                           }
                           Query sirala=firebaseFirestore.collection("/ulanyjylar/"+user_id+"/dostlar").orderBy("ady",Query.Direction.ASCENDING);
                           sirala.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                               @Override
                               public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                   for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                                       if(doc.getType()==DocumentChange.Type.ADDED){
                                           Obshydostlar_adapter obshydostlar_adapter=doc.getDocument().toObject(Obshydostlar_adapter.class);
                                           dostlar.add(obshydostlar_adapter);
                                           obshydostlar_adapterclass.notifyDataSetChanged();


                                       }
                                   }
                               }
                           });

                       }
                       else{
                           Query sirala=firebaseFirestore.collection("/ulanyjylar/"+user_id+"/dostlar").orderBy("ady",Query.Direction.ASCENDING);
                           sirala.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                               @Override
                               public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                   for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                                       if(doc.getType()==DocumentChange.Type.ADDED){
                                           Obshydostlar_adapter obshydostlar_adapter=doc.getDocument().toObject(Obshydostlar_adapter.class);
                                           dostlar.add(obshydostlar_adapter);
                                           obshydostlar_adapterclass.notifyDataSetChanged();


                                       }
                                   }
                               }
                           });
                       }
                }}
            }
        });







}
}


