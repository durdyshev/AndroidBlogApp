package com.example.komp.gurles;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class Postadapter extends BlogPostId{
    public String informasiya;
    public String tipi;
    public List<String>surat_url;
    public String user_id;
    public java.util.Date wagt;
    public String kici_surat;




    public Postadapter(String informasiya, List<String> surat_url, java.util.Date wagt, String user_id, String kici_surat, String tipi) {
        this.informasiya = informasiya;

        this.surat_url = surat_url;
        this.wagt = wagt;
        this.user_id=user_id;
        this.kici_surat=kici_surat;
        this.tipi=tipi;
    }

    public Postadapter(){}
    public String getKici_surat() {
        return kici_surat;
    }

    public void setKici_surat(String kici_surat) {
        this.kici_surat = kici_surat;
    }

    // public Timestamp wagt;


    public List<String> getSurat_url() {
        return surat_url;
    }

    public void setSurat_url(List<String> surat_url) {
        this.surat_url = surat_url;
    }


    public java.util.Date getWagt() {
        return wagt;
    }

    public void setWagt(java.util.Date wagt) {
        this.wagt = wagt;
    }

    public String getInformasiya() {
        return informasiya;
    }

    public void setInformasiya(String informasiya) {
        this.informasiya = informasiya;
    }



    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTipi() {
        return tipi;
    }

    public void setTipi(String tipi) {
        this.tipi = tipi;
    }

    /* public Timestamp getWagt() {
        return wagt;
    }

    public void setWagt(Timestamp wagt) {
        this.wagt = wagt;
    }*/
}
