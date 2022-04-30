package model;

import android.net.Uri;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.Serializable;

import pconfiguracaofirebase.ConfiguracoesFirebase;

public class Usuarios implements Serializable {
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String tipo;
    private String valePassagem;
    private StorageReference mStorageRef;
    private Uri imagemUri;
    private String Latitude;
    private String Longitude;


    private String url;
    public Usuarios() {
    }

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();
        DatabaseReference usuarios = firebaseRef.child("usuarios").child(getId());
        usuarios.setValue(this);


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getValePassagem() {
        return valePassagem;
    }

    public void setValePassagem(String valePassagem) {
        this.valePassagem = valePassagem;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Uri getImagemUri() {
        return imagemUri;
    }

    public void setImagemUri(Uri imagemUri) {
        this.imagemUri = imagemUri;
    }
}
