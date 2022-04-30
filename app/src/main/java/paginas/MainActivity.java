package paginas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.br.hey.mototaxi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import helper.Permissoes;
import helper.UsuarioFirebase;
import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

public class MainActivity extends AppCompatActivity {
    private EditText campoEmail, campoSenha;
    private FirebaseAuth autenticacaoFireb;
    public ProgressDialog carregando;

    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        carregando = new ProgressDialog(this);
        campoEmail = findViewById(R.id.txtEmailLogin);
        campoSenha = findViewById(R.id.txtSenhaLogin);
        //Validar permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);
    }

    public void abrirPaginaLogin(View view) {
        startActivity(new Intent(this, TelaLogin.class));
    }

    public void abrirPaginaCadastro(View view) {
        startActivity(new Intent(this, Cadastro.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
                            UsuarioFirebase.redirecionaUsarioLogado(MainActivity.this);
                        } else {
                            Toast.makeText(MainActivity.this, "Ocorreu um erro ao tentar autenticar", Toast.LENGTH_LONG).show();
                        }
                    }
                });

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
                Toast.makeText(MainActivity.this, "Preencha o campo senha", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Preencha o campo de email", Toast.LENGTH_LONG).show();
        }

    }
}
