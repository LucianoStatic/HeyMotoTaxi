package paginas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.br.hey.mototaxi.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import helper.Local;
import helper.UsuarioFirebase;
import model.Destino;
import model.Requisicao;
import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

public class PagamentoPassageiro extends AppCompatActivity {
    private RatingBar barraAvaliacao;
    private Usuarios Dadosmotorista;
    private Requisicao atributoRequisicaoPassageiro;
    private DatabaseReference firebaseRef;
    private TextView nomeMotoristaPagamento, valorPassagem;
    private ImageView ivBasicImage;
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private Usuarios dadosUser;

    private static String imageUri;
    public Requisicao Dmotorista;
    public LatLng localizacaoPassageiro;
    public Destino destino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento_passageiro);
        barraAvaliacao = findViewById(R.id.ratingBar);
        nomeMotoristaPagamento = findViewById(R.id.textoNomeMotoristPagamento);
        valorPassagem = findViewById(R.id.textoPrecoAtualViagem);
        ivBasicImage = findViewById(R.id.imagemPagamento);
        barraAvaliacao.setMax(5);
        //configuracoes firebase
        firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();


    }

    @Override
    protected void onStart() {
        super.onStart();

        validarStatusRequisicaoPassageiro();

    }

    public void botaoRating(View view) {
        startActivity(new Intent(PagamentoPassageiro.this, TelaPassageiro.class));


    }


    private void validarStatusRequisicaoPassageiro() {

        //recupera o id do usuario que fez o login e esta conectado no app
        final Usuarios usuarioLogadoNoAplicativo = UsuarioFirebase.pegarDadosPassageiroLogado();
        final DatabaseReference requisicoes = firebaseRef.child("RequisicoesPassageiro");
        Query queryRequisicaoPesquisa = requisicoes.orderByChild("passageiro/id")
                .equalTo(usuarioLogadoNoAplicativo.getId());
        //metodo da query de pesquisa
        queryRequisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //recuperar os dados pesquisados no banco requisicao do passageiro e na tabela passageiro
                List<Requisicao> lista = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    lista.add(ds.getValue(Requisicao.class)); //coisas da requisicao passageiro que estao na lista: id,status,passageiro,destino
                }
                Collections.reverse(lista);
                atributoRequisicaoPassageiro = lista.get(0); //pega a lista que tiver atual cadastro do id no banco
                Dmotorista = atributoRequisicaoPassageiro;
                destino = atributoRequisicaoPassageiro.getDestino();
                nomeMotoristaPagamento.setText(Dmotorista.getMotorista().getNome().toString());
                valorPassagem.setText("R$: "+Dmotorista.getPrecoViagem());
                adicionarFotoMotorista();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void adicionarFotoMotorista() {
        DatabaseReference usuarios = referencia.child("usuarios");
        DatabaseReference usuarioPesquisa = usuarios.child(Dmotorista.getMotorista().getId());
        usuarioPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dadosUser = dataSnapshot.getValue(Usuarios.class);

                dadosUser = dataSnapshot.getValue(Usuarios.class);
                imageUri = dadosUser.getUrl();
                // Log.d("AQUI ESTA A URL IMAGEM", "URL AQUI " + imageUri);
                Picasso.get().load(imageUri).fit().centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.aviso)
                        .into(ivBasicImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }






}
