package paginas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.br.hey.mototaxi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import helper.UsuarioFirebase;
import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

public class Cadastro extends AppCompatActivity {
    private EditText campoNome, campoEmail, campoSenha;
    private Switch switchEscolher;
    private FirebaseAuth autenticacao;

    DatabaseReference usuarios;

    //SOLICITACAO DA IMAGEM
    private static final int SOLICITACAO_IMAGEM_CELULAR = 1;
    //URL DA IMAGEM
    private Uri imagemUri;
    private ImageView imagemPerfil;
    private StorageTask mUploadTask;
    private StorageReference mStorageRef;
    public String idUsuario;
    private DatabaseReference mDatabaseRef;

    private Uri downloadUrl;
    Uri caminhoUrl;
    private Button botaoSalvarHabilitado;
    private TextView botaoCarregarimg;
    private DatabaseReference firebaseRef;
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    public String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);


        //atributos

        imagemPerfil = findViewById(R.id.imagemCirculada2);
        campoNome = findViewById(R.id.txtCadastrarNome);
        campoEmail = findViewById(R.id.txtCadastrarEmail);
        campoSenha = findViewById(R.id.txtCadastrarSenha);
        switchEscolher = findViewById(R.id.switchEscolha);
        botaoSalvarHabilitado = findViewById(R.id.botaoFinalizarCadastro);
        botaoSalvarHabilitado.setVisibility(View.GONE);
        mStorageRef = FirebaseStorage.getInstance().getReference("MeusUploads"); //TABELA DO STORAGE ONDE FICA ARMAZENADO AS IMAGENS


    }

    //metodo validar os dados do usuario
    public void metodoValidarUsuario() {

        String valorNome = campoNome.getText().toString();
        String valorEmail = campoEmail.getText().toString();
        String valorSenha = campoSenha.getText().toString();

        String valUrl = downloadUrl.toString();

        if (!valorNome.isEmpty()) {
            if (!valorEmail.isEmpty()) {
                if (!valorSenha.isEmpty()) {
                    Usuarios classeUsuarios = new Usuarios();
                    classeUsuarios.setNome(valorNome);

                    classeUsuarios.setUrl(valUrl);

                    classeUsuarios.setEmail(valorEmail);
                    classeUsuarios.setSenha(valorSenha);
                    classeUsuarios.setTipo(metodoDefinirTipoUsuario());
                    classeUsuarios.setValePassagem("11");

                    //metodo salvr usr

                    metodoSalvarUsuario(classeUsuarios);


                } else {
                    Toast.makeText(this, "Preencha o campo de senha!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Preencha o campo email!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Preencha o campo nome!", Toast.LENGTH_LONG).show();
        }
    }

    //metodo definir o tipo de usuario
    public String metodoDefinirTipoUsuario() {
        if (switchEscolher.isChecked()) {
            return "Condutor";
        } else {
            return "Passageiro";
        }
    }

    //metodo salvar usuario no firebase
    private void metodoSalvarUsuario(final Usuarios recebendoValoresUsuario) {
        autenticacao = ConfiguracoesFirebase.metodopegarAutenticacaoFirebaseAuth();
        autenticacao.createUserWithEmailAndPassword(recebendoValoresUsuario.getEmail(), recebendoValoresUsuario.getSenha())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            idUsuario = task.getResult().getUser().getUid();
                            recebendoValoresUsuario.setId(idUsuario);

                            recebendoValoresUsuario.salvar();

                            //atualizar nome do usuario
                            UsuarioFirebase.atualizarNomeUsuario(recebendoValoresUsuario.getNome());
                            if (metodoDefinirTipoUsuario() == "Passageiro") {

                                startActivity(new Intent(Cadastro.this, TelaPassageiro.class));

                                finish();
                                Toast.makeText(Cadastro.this, "Usuário salvo com sucesso", Toast.LENGTH_LONG).show();
                            } else {
                                startActivity(new Intent(Cadastro.this, Requisicoes.class));
                                finish();
                                Toast.makeText(Cadastro.this, "Condutor salvo com sucesso!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }


    //metodo botao criar conta
    public void metodoBotaoCriarConta(View view) {


        metodoValidarUsuario(); // joga neste metodo e salva


    }

    //metodo selecionar foto para o perfil
    public void metodoSelecionarFotoPerfil(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SOLICITACAO_IMAGEM_CELULAR);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SOLICITACAO_IMAGEM_CELULAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imagemUri = data.getData(); //IMAGEM URI RECEBE A DATA NO CASO O CAMINHO DA FOTO
            Picasso.get().load(imagemUri).into(imagemPerfil); //PICASSO CARREGA A URL ENDERECO DA IMAGEM E JOGA PRA IMAGEVIEW
            imagemPerfil.setImageURI(imagemUri); //SETA A IMAGEM RENDERIZADA PELO PICASSO PRA MOSTRAR AO USUARIO A FOTO
            Log.i("444444444444444440", "TESTANDO CAMINHO DA IMAGEM " + imagemUri);
            salvarFoto();
        }
    }

    private String getFileExtension(Uri uri) { //responsavel por alguma coisa da URl
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void salvarFoto() {

        if (imagemUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imagemUri));
            mUploadTask = fileReference.putFile(imagemUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(Cadastro.this, "upload", Toast.LENGTH_SHORT).show();

                            //*************************************************************************************************************
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful()) ;
                            downloadUrl = urlTask.getResult();
                            Log.i("TESTANDO SE PEGA O URL", "URL DA IMAGEM: " + downloadUrl);

                            botaoSalvarHabilitado.setVisibility(View.VISIBLE);

                            //************************************************************************************************************
                        }
                    });
        }

    }


}
