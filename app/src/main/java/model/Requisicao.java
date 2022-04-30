package model;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import pconfiguracaofirebase.ConfiguracoesFirebase;

public class Requisicao {
    private String id;
    private String Status;
    private Usuarios Passageiro;
    private Usuarios Motorista;
    private Destino Destino;
    private String urlImagem;
    private String precoViagem;


    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO = "acaminho";
    public static final String STATUS_VIAGEM = "viagem";
    public static final String STATUS_FINALIZADA = "finalizada";
    public static final String STATUS_ENCERRADA = "encerrada";
    public static final String STATUS_CANCELADA = "cancelada";

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();
        DatabaseReference requisicoes = firebaseRef.child("RequisicoesPassageiro");
        String idRequisicaoPassageiro = requisicoes.push().getKey(); //recuperou o id da requisicao acionada
        setId(idRequisicaoPassageiro);
        requisicoes.child(getId()).setValue(this);
    }


        public void metodoAtualizar() { // metodo feito pra atualizar os dados la da requisicao de passageito para o motorista aceitar
            DatabaseReference firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();
            DatabaseReference requisicoes = firebaseRef.child("RequisicoesPassageiro");
            DatabaseReference requisicao = requisicoes.child(getId());
            Map objeto = new HashMap();
            objeto.put("motorista", getMotorista()); //quero atualizar somente a isntancia motorista no banco de dados do firebase
            objeto.put("status", getStatus()); //quero atualizar o status que ja tem o valor na classe Painel viagens do motorista
            requisicao.updateChildren(objeto);
        }


    public void atualizarStatus() {
        DatabaseReference firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();
        DatabaseReference requisicoes = firebaseRef.child("RequisicoesPassageiro");
        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("status", getStatus());
        //quero atualizar o status que ja tem o valor na classe Painel viagens do motorista
        requisicao.updateChildren(objeto);
    }

    public void metodoAtualizarLocalizacaoMotorista() { // metodo feito pra atualizar os dados la da requisicao de passageito para o motorista aceitar
        DatabaseReference firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();
        DatabaseReference requisicoes = firebaseRef.child("RequisicoesPassageiro");
        DatabaseReference requisicao = requisicoes.child(getId())
                .child("motorista");
        Map objeto = new HashMap();
        objeto.put("latitude", getMotorista().getLatitude()); //quero atualizar somente a isntancia motorista.LATITUDE no banco de dados do firebase
        objeto.put("longitude", getMotorista().getLongitude()); //quero atualizar a LONGITUDE que ja tem o valor na classe Painel viagens do motorista
        requisicao.updateChildren(objeto);
    }
    //construtor
    public Requisicao() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Usuarios getPassageiro() {
        return Passageiro;
    }

    public void setPassageiro(Usuarios passageiro) {
        Passageiro = passageiro;
    }

    public Usuarios getMotorista() {
        return Motorista;
    }

    public void setMotorista(Usuarios motorista) {
        Motorista = motorista;
    }

    public model.Destino getDestino() {
        return Destino;
    }

    public void setDestino(model.Destino destino) {
        Destino = destino;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getPrecoViagem() {
        return precoViagem;
    }

    public void setPrecoViagem(String precoViagem) {
        this.precoViagem = precoViagem;
    }
}
