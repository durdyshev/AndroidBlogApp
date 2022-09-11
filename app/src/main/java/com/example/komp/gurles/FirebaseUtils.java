package com.example.komp.gurles;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class FirebaseUtils {
    private Context mContext;
    public FirebaseUtils(Context context) {
        this.mContext=context;

    }
    //FirebaseUtils(){}
     public String getFirebaseUserId(){
      SharedPreferences sharedPreferences=mContext.getSharedPreferences("UserDetails",Context.MODE_PRIVATE);
       return sharedPreferences.getString("user_id","");
    }
}
