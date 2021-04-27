package com.example.komp.gurles;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DostlarPageAdapter extends FragmentPagerAdapter {
    public DostlarPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {

        switch (position){
            case 0:
                Dost_yeke_haly_dostlar_hemmesi_fragment dost_yeke_haly_dostlar_hemmesi_fragment=new Dost_yeke_haly_dostlar_hemmesi_fragment();
                return dost_yeke_haly_dostlar_hemmesi_fragment;

            case 1: Dost_yeke_haly_dostlar_hemmesi_menzes_fragment dost_yeke_haly_dostlar_hemmesi_menzes_fragment=new Dost_yeke_haly_dostlar_hemmesi_menzes_fragment();
                return dost_yeke_haly_dostlar_hemmesi_menzes_fragment;

            default:return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Hemmesi";

            case 1:
                return "Meňzeş";

            default:
                return null;
        }


    }
}

