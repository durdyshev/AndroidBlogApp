package com.example.komp.gurles;

import java.util.Date;
import java.util.List;

public class SaklananAdapter {
    private String informasiya;
    private List<String> surat_url;
    private String user_id;
    private String wagt;

public SaklananAdapter(){}

    public SaklananAdapter(String informasiya, List<String> surat_url, String user_id, String wagt) {
        this.informasiya = informasiya;
        this.surat_url = surat_url;
        this.user_id = user_id;
        this.wagt = wagt;
    }

    public String getInformasiya() {
        return informasiya;
    }

    public void setInformasiya(String informasiya) {
        this.informasiya = informasiya;
    }

    public List<String> getSurat_url() {
        return surat_url;
    }

    public void setSurat_url(List<String> surat_url) {
        this.surat_url = surat_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getWagt() {
        return wagt;
    }

    public void setWagt(String wagt) {
        this.wagt = wagt;
    }
}
