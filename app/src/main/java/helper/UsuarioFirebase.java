package helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;
import paginas.Requisicoes;
import paginas.TelaPassageiro;

public class UsuarioFirebase {
    public static FirebaseUser pegarAutenticacaoUsuario() {
        FirebaseAuth usuarioAuth = ConfiguracoesFirebase.metodopegarAutenticacaoFirebaseAuth();
        return usuarioAuth.getCurrentUser();
    }

    public static boolean atualizarNomeUsuario(String nomeUsuario) {
        try {
            FirebaseUser user = pegarAutenticacaoUsuario();
            UserProfileChangeRequest perfilUSuario = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nomeUsuario).build();
            user.updateProfile(perfilUSuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Log.d("Perfil", "Erro ao atualizar nome de perfil");
                    }

                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void redirecionaUsarioLogado(final Activity activity) {
        DatabaseReference usuarioRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase()
                .child("usuarios")
                .child(pegarIdentificadorUsuario());
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);
                String recuperarTipoUsuario = usuarios.getTipo();// recebe  marcio, tipo: condutor
                String recuperarNomeUsuario = usuarios.getNome();
                Log.i("App", "DADOS: " + pegarIdentificadorUsuario());
                if (recuperarTipoUsuario.equals("Condutor")) { // compara que marcio e do tipo : condutor
                    activity.startActivity(new Intent(activity, Requisicoes.class)); //abrindo a tela do condutor
                } else {
                    activity.startActivity(new Intent(activity, TelaPassageiro.class)); //abrindo a tela do passageiro
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


    public static Usuarios pegarDadosPassageiroLogado() {
        FirebaseUser firebaseUser = pegarAutenticacaoUsuario();
        Usuarios usuarios = new Usuarios();
        usuarios.setId(firebaseUser.getUid()); // traz o meu id:7947294hfsdf8u8
        usuarios.setEmail(firebaseUser.getEmail()); // traz o meu email: luciano@gmail.com
        usuarios.setNome(firebaseUser.getDisplayName());// traz o meu nome Luciano Drop
        return usuarios;
    }


    public static void atualizaDadosLocalizacaoGeoFire(double latitude, double longitude) {
        //Define um nome para a tabela no banco de dados Firebase
        DatabaseReference localUsuario = ConfiguracoesFirebase.metodoPegarFirebaseDataBase().child("Endereco_Local_Usuario");

        //Instancia o GeoFire
        GeoFire geoFire = new GeoFire(localUsuario); //recebe a tabela pra ele salvar as latitudes e longitudes de acordo ao andar

        //Recupera os dados de quem ta logado no aplicativo
        Usuarios usuarioLogado = UsuarioFirebase.pegarDadosPassageiroLogado();

        //Configura localizacao do usuario
        geoFire.setLocation(usuarioLogado.getId(),
                new GeoLocation(latitude, longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            Log.d("APP", "Erro ao salvar localizacao geofire ");
                        } else {
                            Log.d("APP", "QUAL E O ID DO PASSAGEIRO LOGADO " + key);

                        }
                    }
                });
    }

}
