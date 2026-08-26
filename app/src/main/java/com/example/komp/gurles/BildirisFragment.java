package com.example.komp.gurles;


import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justblog.databinding.FragmentBildirisBinding;
import com.example.komp.gurles.model.ContactData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class BildirisFragment extends Fragment {
    private FragmentBildirisBinding binding;
    private List<Obshydostlar_adapter> dostlar;
    private ArrayList<ContactData> contactList;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private Obshydostlar_adapterclass obshydostlar_adapterclass;
    private int i;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public BildirisFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBildirisBinding.inflate(inflater, container, false);
        initVariables();
        initRecyclerView();


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Kontaklary almaga rugsat berin.", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            getContactList();
            loadcontact();

        }

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void initRecyclerView() {
        obshydostlar_adapterclass = new Obshydostlar_adapterclass(dostlar);
        binding.contactRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.contactRecyclerview.setAdapter(obshydostlar_adapterclass);

    }

    private void initVariables() {
        contactList = new ArrayList<>();
        dostlar = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        sharedPreferences = getContext().getSharedPreferences("UserPreferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    private void loadcontact() {
        if (contactList.size() != sharedPreferences.getInt("contactSize", -1)) {
            addFriendsToFirebase();
        }
        loadFriendsFromFirebase();
        saveContactSizeToSharedPref();
    }
    private void addFriendsToFirebase() {
        for (i = 0; i < contactList.size(); i++) {

            firebaseFirestore.collection("ulanyjylar").whereEqualTo("number", contactList.get(i)).get().addOnCompleteListener(task -> {
                if (!task.getResult().isEmpty()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        final String idi = document.getString("user_id");
                        firebaseFirestore.collection("ulanyjylar").document(user_id).collection("blok").whereEqualTo("user_id", idi).get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.getResult().isEmpty()) {
                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("user_id", idi);
                                        postMap.put("ady", contactList.get(i).getName());

                                        if (idi != null && !idi.equals(user_id)) {
                                            firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dostlar").document(idi).set(postMap)
                                                    .addOnCompleteListener(task2 -> Toast.makeText(getContext(), "Basarili", Toast.LENGTH_LONG).show());
                                        }
                                    }

                                });
                    }
                }
            });

        }

    }

    private void getContactList() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        final Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        while (cursor.moveToNext()) {

            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String nomer = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll(" ", "");

            if (name.isEmpty()) {
                name = nomer;
            }
            contactList.add(new ContactData(name, nomer));

        }

        cursor.close();
    }

    private void loadFriendsFromFirebase() {
        Query sirala = firebaseFirestore.collection("/ulanyjylar/" + user_id + "/dostlar").orderBy("ady", Query.Direction.ASCENDING);
        sirala.addSnapshotListener(getActivity(), (documentSnapshots, e) -> {

            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                if (doc.getType() == DocumentChange.Type.ADDED) {
                    Obshydostlar_adapter obshydostlar_adapter = doc.getDocument().toObject(Obshydostlar_adapter.class);
                    dostlar.add(obshydostlar_adapter);
                    obshydostlar_adapterclass.notifyDataSetChanged();


                }
            }
        });
    }
    private void saveContactSizeToSharedPref() {
        editor.putInt("contactSize", contactList.size());
        editor.commit();
        editor.apply();
    }
}