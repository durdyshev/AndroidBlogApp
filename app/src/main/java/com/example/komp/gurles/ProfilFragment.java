package com.example.komp.gurles;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfilFragment extends Fragment {
    private TextView ady;
    private CardView profil,dost_gos,sazlamalar,blok,saklanan,post;
    private FirebaseAuth mAuth;
    private String user_id;
    private FirebaseFirestore firebaseFirestore;
    private String image;
    private ImageView profil_surat;


    public ProfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);
mAuth=FirebaseAuth.getInstance();
firebaseFirestore=FirebaseFirestore.getInstance();
user_id=mAuth.getCurrentUser().getUid();
profil_surat=(ImageView)view.findViewById(R.id.fragment_profil_surat);
ady=(TextView)view.findViewById(R.id.fragment_profil_ady);


profil=(CardView)view.findViewById(R.id.fragment_profil_cardview_profil);
dost_gos=(CardView)view.findViewById(R.id.fragment_profil_cardview_dost_gos);
sazlamalar=(CardView)view.findViewById(R.id.fragment_profil_cardview_sazlamalar);
blok=(CardView)view.findViewById(R.id.fragment_profil_cardview_blok);
saklanan=(CardView)view.findViewById(R.id.fragment_profil_cardview_saklanan);
post=(CardView)view.findViewById(R.id.fragment_profil_cardview_postlar);

profil.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(getContext(),Profil.class);
        startActivity(intent);
    }
});
blok.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(getContext(),Blok.class);
        startActivity(intent);
    }
});
dost_gos.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(getContext(),DostGos.class);
        startActivity(intent);
    }

});
saklanan.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(getContext(),Saklanan.class);
        startActivity(intent);
    }
});
post.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(getContext(),Postlar.class);
        startActivity(intent);
    }
});

        firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(isAdded()){
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name=task.getResult().getString("ady");
                        image=task.getResult().getString("surat");



                        ady.setText(name);


                        RequestOptions placeholderreq=new RequestOptions();
                        placeholderreq.placeholder(R.mipmap.profil);
                        Glide.with(ProfilFragment.this).setDefaultRequestOptions(placeholderreq).load(image).into(profil_surat);
                    }
                }}
            }




        });









        // Inflate the layout for this fragment


        return view;
    }



}