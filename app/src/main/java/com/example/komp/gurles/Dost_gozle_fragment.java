package com.example.komp.gurles;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * A simple {@link Fragment} subclass.
 */
public class Dost_gozle_fragment extends Fragment {
    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth mAuth;
    private String user_id;
    private TextView tekst;
    private TextView ad;
    private ImageView surat;

    private ImageButton gosknopka;
    private RadioButton radio_ady;
    private RadioButton radio_id;

    private EditText id;



    public Dost_gozle_fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_dost_gozle_fragment, container, false);
        surat = (ImageView)view.findViewById(R.id.dost_gos_profil);
        id = (EditText)view. findViewById(R.id.dost_gos_tekst_id);
        tekst = (TextView)view. findViewById(R.id.dost_gos_id);
        ad = (TextView)view. findViewById(R.id.dost_gos_ady);
        gosknopka = (ImageButton)view. findViewById(R.id.dost_gos_gozle_surat_knopka);
        radio_ady=(RadioButton)view.findViewById(R.id.radio_ady);
        radio_id=(RadioButton)view.findViewById(R.id.radio_id);
        firebaseFirestore = FirebaseFirestore.getInstance();

surat.setVisibility(View.INVISIBLE);
ad.setVisibility(View.INVISIBLE);
tekst.setVisibility(View.INVISIBLE);

radio_ady.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        radio_id.setChecked(false);
    }
});
radio_id.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        radio_ady.setChecked(false);
    }
});



        gosknopka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(tekst.getText().toString())){
                    Toast.makeText(getActivity(),"Yaz bir zatlar mat tvoyu",Toast.LENGTH_LONG).show();

                }
                else{
                    String ido = id.getText().toString();
                    if(radio_id.isChecked()){
                        firebaseFirestore.collection("ulanyjylar").whereEqualTo("id", ido).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.getResult().isEmpty()) {

                                                                       Toast.makeText(getActivity(), "Dostunyz tapylmady", Toast.LENGTH_LONG).show();



                                }
                                else{
                                    Toast.makeText(getActivity(), "Dostunyz tapyldy", Toast.LENGTH_LONG).show();
                                    surat.setVisibility(View.VISIBLE);
                                    ad.setVisibility(View.VISIBLE);
                                    tekst.setVisibility(View.VISIBLE);
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String idi = document.getString("id");
                                        String adyy = document.getString("ady");
                                        String suraturl = document.getString("surat");
                                        String userid = document.getString("user_id");
                                        tekst.setText(idi);
                                        ad.setText(adyy);
                                        user_id = userid;

                                        RequestOptions placeholderreq = new RequestOptions();
                                        placeholderreq.placeholder(R.mipmap.profil);
                                        Glide.with(getActivity()).setDefaultRequestOptions(placeholderreq).load(suraturl).into(surat);


                                    }

                                }
                            }
                        });

                    }
                    if(radio_ady.isChecked()){
                        firebaseFirestore.collection("ulanyjylar").whereEqualTo("ady", ido).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.getResult().isEmpty()) {


                                    Toast.makeText(getActivity(), "Dostunyz tapylmady", Toast.LENGTH_LONG).show();



                                }
                                else{
                                    Toast.makeText(getActivity(), "Dostunyz tapyldy", Toast.LENGTH_LONG).show();
                                    surat.setVisibility(View.VISIBLE);
                                    ad.setVisibility(View.VISIBLE);
                                    tekst.setVisibility(View.VISIBLE);
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String idi = document.getString("id");
                                        String adyy = document.getString("ady");
                                        String suraturl = document.getString("surat");
                                        String userid = document.getString("user_id");
                                        String nomer= document.getString("number");
                                        tekst.setText(idi);
                                        ad.setText(adyy);
                                        user_id = userid;

                                        RequestOptions placeholderreq = new RequestOptions();
                                        placeholderreq.placeholder(R.mipmap.profil);
                                        Glide.with(getActivity()).setDefaultRequestOptions(placeholderreq).load(suraturl).into(surat);


                                    }

                                }
                            }
                        });

                    }




                }}
        });
        surat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_id!=null){
                    Intent intent = new Intent(getActivity(), Dost_yeke_haly.class);
                    intent.putExtra("idi", user_id);
                    startActivity(intent);
                }
                else{

                    Toast.makeText(getActivity(),"Yalnyslyk",Toast.LENGTH_LONG).show();
                }

            }
        });


        return view;
    }

}
