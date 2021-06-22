package com.example.myapplication.models;

public class Upload {
    private String id;
    private String nomeImage;
    private String url;

    public Upload(String id, String nomeImage, String url) {
        this.id = id;
        this.nomeImage = nomeImage;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeImage() {
        return nomeImage;
    }

    public void setNomeImage(String nomeImage) {
        this.nomeImage = nomeImage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //Firebase utilizará esse construtor para enviar dados
    public Upload(){

    }
}
