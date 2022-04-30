package paginas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.br.hey.mototaxi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

public class TelaPassageiro extends AppCompatActivity {
    private static TextView nomePassageiro;
    private static TextView mensagemValor;
    private static DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private static String valorIdUsuario;
    private static Usuarios dadosUser;
    private static String imageUri;
    private static ImageView ivBasicImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_passageiro);

       nomePassageiro = findViewById(R.id.txtNomeDoCondutor);
       // mensagemValor = findViewById(R.id.textVale);
//        mensagemValor.setVisibility(View.INVISIBLE);
        ivBasicImage = findViewById(R.id.img);
        redirecionaUsarioLogado();


    }

    @Override
    protected void onStart() {
        super.onStart();
        buscador();

    }


    public static void redirecionaUsarioLogado() {

        final DatabaseReference usuarioRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase()
                .child("usuarios")
                .child(pegarIdentificadorUsuario());
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                buscarUrlUsuario(); //Metodo que traz a url pra subir a imagem no metodo abaixo

                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);
                String recuperarNomeUsuario = usuarios.getNome();
                String recuperarValePassagem = usuarios.getValePassagem();
                Log.i("App", "DADOS: " + pegarIdentificadorUsuario());
                //TesteFoto();
                if (recuperarNomeUsuario == recuperarNomeUsuario) { // compara se marcio e o igual a marcio
                    nomePassageiro.setText(recuperarNomeUsuario.toString());

                    if (recuperarValePassagem.equals("10")) {
                    //    mensagemValor.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static String pegarIdentificadorUsuario() {
        return pegarAutenticacaoUsuario().getUid();
    }

    public static FirebaseUser pegarAutenticacaoUsuario() {
        FirebaseAuth usuarioAuth = ConfiguracoesFirebase.metodopegarAutenticacaoFirebaseAuth();
        return usuarioAuth.getCurrentUser();
    }

    public void abrirMotoTaxi(View view) {

        startActivity(new Intent(this, Mapassageiro.class));
    }


    public static void buscarUrlUsuario() {
        DatabaseReference usuarios = referencia.child("usuarios");
        DatabaseReference usuarioPesquisa = usuarios.child(valorIdUsuario);
        usuarioPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

    public void buscador() {
        valorIdUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


}
