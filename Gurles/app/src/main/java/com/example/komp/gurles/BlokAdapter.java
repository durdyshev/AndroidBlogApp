package com.example.komp.gurles;

public class BlokAdapter {
    private String ady;
    private String profil_surat;
    private String user_id;
    private String number;
    private String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdy() {
        return ady;
    }

    public void setAdy(String ady) {
        this.ady = ady;
    }

    public String getProfil_surat() {
        return profil_surat;
    }

    public void setProfil_surat(String profil_surat) {
        this.profil_surat = profil_surat;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }



    public BlokAdapter(){}

    public BlokAdapter(String id,String ady, String profil_surat, String user_id, String number) {
        this.ady = ady;
        this.profil_surat = profil_surat;
        this.user_id = user_id;
        this.number = number;
        this.id=id;
    }


}
