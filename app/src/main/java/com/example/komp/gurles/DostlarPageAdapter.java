package com.example.komp.gurles;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class DostlarPageAdapter extends FragmentStatePagerAdapter {


    public DostlarPageAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Dost_yeke_haly_dostlar_hemmesi_fragment();
            case 1:
                return new Dost_yeke_haly_dostlar_hemmesi_menzes_fragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Hemmesi";
            case 1:
                return "Meňzeş";
            default:
                return null;
        }
    }
}
