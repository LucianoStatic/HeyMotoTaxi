package paginas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.br.hey.mototaxi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import model.Requisicao;
import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

public class Requisicoes extends AppCompatActivity {
    private static TextView nomeCondutor, mensagemAguardandoCorrida;
    private ImageView imagemViagemRequisicao;
    private Drawable imagemViagemNaoDisponivel;
    private Drawable imagemViagemDisponivel;
    private DatabaseReference firebaseRef;
    private static final String Canal_ID = "default";
    private static String valorIdUsuario;
    private static Usuarios dadosUser;
    private static String imageUri;
    private static ImageView ivBasicImage;
    private static DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes);
        nomeCondutor = findViewById(R.id.txtNomeDoCondutor);

        mensagemAguardandoCorrida = findViewById(R.id.txtAguardandoMensagem);
        imagemViagemRequisicao = findViewById(R.id.imagemAlertaMotorista);

        //configuracoes iniciais do firebase
        firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();

        //setar imagens
        imagemViagemNaoDisponivel = getResources().getDrawable(R.drawable.zeroviagem);
        imagemViagemDisponivel = getResources().getDrawable(R.drawable.bolha);
        ivBasicImage = findViewById(R.id.imgPrincipal);

        //recuperar chamado das corridas para o moto taxi
        recuperarCorrida();

        //metodo mostrar nome do condutor da moto
        mostrarNomeCondutor();

    }

    @Override
    protected void onStart() {
        super.onStart();
        buscador();
    }

    private void recuperarCorrida() {
        final DatabaseReference requisicoes = firebaseRef.child("RequisicoesPassageiro"); //entra na tabela requisicoes de passageiro no banco de dados
        final Query pesquisaRequisicao = requisicoes.orderByChild("status") //ordenar ou seja ir pelo status
                .equalTo(Requisicao.STATUS_AGUARDANDO); // sendo agora o status aguardando ele só ira fazer algo se o estatus tiver como aguardando
        pesquisaRequisicao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // aqui vc coloca suas regras de como vc vai querer fazer se o status for AGUARDANDO...
                if (dataSnapshot.getChildrenCount() > 0) { //um if pra saber se tem requisicoes no dataSnapshot e ai fazer alguma coisa
                    AlertaNotificacaoViagem();
                    imagemViagemRequisicao.setImageDrawable(imagemViagemDisponivel); //seta a imagem informando que tem requisicao de viagem
                    // recyclerCorridasMotoTaxi.setVisibility(View.VISIBLE);
                    Log.d("App", "DATASNAPSHOT: " + dataSnapshot);
                    mensagemAguardandoCorrida.setText("Você possui uma viagem disponível!"); //seta o texto informando que tem uma viagem disponivel

                } else {

                    imagemViagemRequisicao.setImageDrawable(imagemViagemNaoDisponivel);
                    // recyclerCorridasMotoTaxi.setVisibility(View.GONE);
                    //Toast.makeText(Requisicoes.this, "O data Snapshot nao achou o Status Igual Aguardando", Toast.LENGTH_LONG).show();
                    mensagemAguardandoCorrida.setText("Nenhuma Viagem Disponível!");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void mostrarNomeCondutor() {
        final DatabaseReference usuarioRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase()
                .child("usuarios")
                .child(pegarIdentificadorUsuario());
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                buscarUrlUsuario(); // metodo que faz a busca do URL e Mostra a Imagem do Condutor no painel de acesso do mesmo
                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);
                String recuperarNomeUsuario = usuarios.getNome();

                Log.i("App", "DADOS: " + pegarIdentificadorUsuario());
                if (recuperarNomeUsuario == recuperarNomeUsuario) { // compara se marcio e o igual a marcio
                    nomeCondutor.setText(recuperarNomeUsuario);
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

    public void chamarTelaViagem(View view) {
        startActivity(new Intent(this, PassageirosRequisicoes.class));
    }

    public void chamarTelaHistorico(View view){
        startActivity(new Intent(this, HistoricoMotorista.class));
    }

    public void AlertaNotificacaoViagem() {
        Intent notificationIntent = new Intent(Requisicoes.this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity(Requisicoes.this, 0, notificationIntent, 0);


        Uri SomAlarme = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), SomAlarme);
        mp.start();


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Requisicoes.this, Canal_ID)
                .setSmallIcon(R.drawable.h)
                .setContentTitle("Hey! Moto Taxi")
                .setContentIntent(resultIntent)
                .setContentText("Olá, você tem uma viagem disponível");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }


    public void buscador() {
        valorIdUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static void buscarUrlUsuario() {
        DatabaseReference usuarios = referencia.child("usuarios");
        DatabaseReference usuarioPesquisa = usuarios.child(valorIdUsuario);
        usuarioPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dadosUser = dataSnapshot.getValue(Usuarios.class);
                imageUri = dadosUser.getUrl();
                Log.d("AQUI ESTA A URL IMAGEM", "URL AQUI " + imageUri);
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
