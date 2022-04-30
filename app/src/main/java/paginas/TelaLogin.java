package paginas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.br.hey.mototaxi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import helper.UsuarioFirebase;
import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

public class TelaLogin extends AppCompatActivity {
    private EditText campoEmail, campoSenha;
    private FirebaseAuth autenticacaoFireb;
    public ProgressDialog carregando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_login);

        carregando = new ProgressDialog(this);
        campoEmail = findViewById(R.id.txtEmailLogin);
        campoSenha = findViewById(R.id.txtSenhaLogin);

    }



    public void validarLoginUsuario(View view) {

        carregando.setTitle("Registrando Usuario");
        carregando.setMessage("Aguarde enquanto o sistema insere os dados...");
        carregando.show();
        //recuperar email e senha dos componentes
        String emailLogar = campoEmail.getText().toString();
        String senhaLogar = campoSenha.getText().toString();
        if (!emailLogar.isEmpty()) {
            if (!senhaLogar.isEmpty()) {
                Usuarios usuarios = new Usuarios();
                usuarios.setEmail(emailLogar);
                usuarios.setSenha(senhaLogar);
                //metodo executar pra logar com usuario salvo
                metodoExecultarAutenticacaoLogin(usuarios);
            } else {
                Toast.makeText(TelaLogin.this, "Preencha o campo senha", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(TelaLogin.this, "Preencha o campo de email", Toast.LENGTH_LONG).show();
        }

    }

    private void metodoExecultarAutenticacaoLogin(final Usuarios recebendo) {
        autenticacaoFireb = ConfiguracoesFirebase.metodopegarAutenticacaoFirebaseAuth();
        autenticacaoFireb.signInWithEmailAndPassword(recebendo.getEmail(), recebendo.getSenha())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verificar tipo de usuario
                            carregando.dismiss();
                            UsuarioFirebase.redirecionaUsarioLogado(TelaLogin.this);
                        } else {
                            Toast.makeText(TelaLogin.this, "Ocorreu um erro ao tentar autenticar", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}
