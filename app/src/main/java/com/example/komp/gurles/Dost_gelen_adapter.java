package com.example.komp.gurles;

public class Dost_gelen_adapter {
    public String ady;
    public String profil_surat;
    public String user_id;
    public String id;

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

    public String getUser_id() { return user_id;
    }

    public void setUser_id(String userid) {
        this.user_id = userid;
    }

    public Dost_gelen_adapter(String ady, String profil_surat, String user_id,String id) {
        this.ady = ady;
        this.profil_surat = profil_surat;
        this.user_id = user_id;
        this.id = id;
    }

    public Dost_gelen_adapter() {

    }
}

