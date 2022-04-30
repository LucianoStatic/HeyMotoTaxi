package paginas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.br.hey.mototaxi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import adapter.RequisicaoPassageiroAdapter;
import helper.RecyclerItemClickListener;
import helper.UsuarioFirebase;
import model.Requisicao;
import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

import static java.lang.Thread.sleep;

public class PassageirosRequisicoes extends AppCompatActivity {
    private RecyclerView recyclerCorridasMotoTaxi;
    private RequisicaoPassageiroAdapter adapterRequisicao;
    private List<Requisicao> listaRequisicoesCorrida = new ArrayList<>();
    private Usuarios motorista;
    private DatabaseReference firebaseRef;
    private FirebaseAuth autenticacao;
    private LocationListener locationListener;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiros_requisicoes);

        inicializarComponentes();
        recuperarLocalizacao();

    }

    private void recuperarViagem() {
        final DatabaseReference requisicoes = firebaseRef.child("RequisicoesPassageiro"); //entra na tabela requisicoes de passageiro no banco de dados
        final Query pesquisaRequisicao = requisicoes.orderByChild("status") //ordenar ou seja ir pelo status
                .equalTo(Requisicao.STATUS_AGUARDANDO); // sendo agora o status aguardando ele só ira fazer algo se o estatus tiver como aguardando
        pesquisaRequisicao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // aqui vc coloca suas regras de como vc vai querer fazer se o status for AGUARDANDO...
                if (dataSnapshot.getChildrenCount() > 0) { //um if pra saber se tem requisicoes no dataSnapshot e ai fazer alguma coisa
                    // recyclerCorridasMotoTaxi.setVisibility(View.VISIBLE);
                    Log.d("App", "DATASNAPSHOT: " + dataSnapshot);
                } else {
                    // recyclerCorridasMotoTaxi.setVisibility(View.GONE);
                    Toast.makeText(PassageirosRequisicoes.this, "O data Snapshot nao achou o Status Igual Aguardando", Toast.LENGTH_LONG).show();
                }
                //limpar lista de requisicoes
                listaRequisicoesCorrida.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) { //utiliza o for pra pegar os dados da viagem e jogar pra uma lista de forma organizada
                    Requisicao requisicaoPassageiro = ds.getValue(Requisicao.class); // pega as requisicoes que tem no firebase pra depois jogar na lista
                    listaRequisicoesCorrida.add(requisicaoPassageiro);   //aqui esta os dados dentro da lista de requisicoes de viagens

                }

                adapterRequisicao.notifyDataSetChanged(); //notifica pra aparecer os dados da viagem no recycleview
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarStatusRequisicao();
    }

    //Metodo verificar em que pé esta a resultante deste cadastro
    private void verificarStatusRequisicao() {
        Usuarios usuarioLogado = UsuarioFirebase.pegarDadosPassageiroLogado();
        DatabaseReference firebaserREF = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();

        DatabaseReference requisicoes = firebaserREF.child("RequisicoesPassageiro");

        Query requisicoesPesquisa = requisicoes.orderByChild("motorista/id")
                .equalTo(usuarioLogado.getId()); //pesquisa agora via ID

        requisicoesPesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Requisicao requisicao = ds.getValue(Requisicao.class);
                    if (requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO) ||
                            requisicao.getStatus().equals(Requisicao.STATUS_VIAGEM) ||
                            requisicao.getStatus().equals(Requisicao.STATUS_FINALIZADA)) {
                        motorista = requisicao.getMotorista(); //pra recuperar os dados do motorista antes que de null
                        abrirTelaCorrida(requisicao.getId(), motorista, true); //Metodo pra pegar id de quem logou, compara se o status e igual o do if
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void abrirTelaCorrida(String idRequisicao, Usuarios motorista, boolean requisicaoAtiva) {
        Intent i = new Intent(PassageirosRequisicoes.this, PainelViagensMotorista.class);
        i.putExtra("idRequisicao", idRequisicao);
        i.putExtra("motorista", motorista);
        i.putExtra("requisicaoAtiva", requisicaoAtiva);
        startActivity(i);
    }

    private void adicionarEventoCliqueRecicle() {

        //adiciona evento de clique no RECYCLEVIEW
        recyclerCorridasMotoTaxi.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerCorridasMotoTaxi,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (listaRequisicoesCorrida != null) { //For diferente de Null pode execultar
                            Requisicao requisicao = listaRequisicoesCorrida.get(position); // atribui uma nova requisicao e essa requisicao recebe uma listaRequisicoes
                            abrirTelaCorrida(requisicao.getId(), motorista, false);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }));
    }

    private void recuperarLocalizacao() {
        //metodo recuperar ultima atualizacao de localizacao do motorista
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //recuperar latitude e longitude
                String latitude_convertida = String.valueOf(location.getLatitude());
                String longitude_convertida = String.valueOf(location.getLongitude());
                // double latitude = location.getLatitude(); //43.844464
                // double longitude = location.getLongitude(); //42.764674

                //Atualizar geofire com mudança de lugar
                UsuarioFirebase.atualizaDadosLocalizacaoGeoFire(location.getLatitude(), location.getLongitude());

                if (!latitude_convertida.isEmpty() && !longitude_convertida.isEmpty()) { //testa se a latitude e longitude nao estao vazias e ai executa
                    motorista.setLatitude(latitude_convertida); //seta o valor da latitude que ele pegou la do getLatitude
                    motorista.setLongitude(longitude_convertida); //seta o valor da longitude que ele pegou la do getLongitude

                    adicionarEventoCliqueRecicle(); //chama o metodo do recycle clique pra abrir a requisicao do passageiro

                    locationManager.removeUpdates(locationListener); //aqui ele para de atualizar a sua localizacao, depois que o locationListener identifica
                    adapterRequisicao.notifyDataSetChanged(); //chama o adapter pra notificar e passar valor do motorista latitude e longitude


                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };
        //solicitar atualizacoes de localizacao
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            return;
        }
    }

    private void inicializarComponentes() {

        recyclerCorridasMotoTaxi = findViewById(R.id.RecyListaCorridas);

        //configuracoes iniciais do firebase
        firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();
        autenticacao = ConfiguracoesFirebase.metodopegarAutenticacaoFirebaseAuth();


        //configuracao pra pegar os dados do MOTORISTA que esta usando essa conta logada no aplicativo
        motorista = UsuarioFirebase.pegarDadosPassageiroLogado(); //AQUI ELE TA PEGANDO DADOS DO MOTORISTA E NAO DO PASSAGEIRO

        //configurar RecycleView
        adapterRequisicao = new RequisicaoPassageiroAdapter(listaRequisicoesCorrida, getApplicationContext(), motorista); //Aqui ta indo, lista das requisicoes,email,tel,email, NOME O MOTORISTA
        RecyclerView.LayoutManager layoutManagerRequisicaoRecycle = new LinearLayoutManager(getApplicationContext());
        recyclerCorridasMotoTaxi.setLayoutManager(layoutManagerRequisicaoRecycle);
        recyclerCorridasMotoTaxi.setHasFixedSize(true);
        recyclerCorridasMotoTaxi.setAdapter(adapterRequisicao);
        //metodo recuperar uma viagem
        recuperarViagem();
    }
}
