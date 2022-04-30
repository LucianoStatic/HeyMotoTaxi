package paginas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.br.hey.mototaxi.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import helper.Local;
import helper.UsuarioFirebase;
import model.Destino;
import model.Requisicao;
import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

public class Mapassageiro extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private EditText campoTextoMeuDestino, campoTextoMeuLocal;

    private boolean cancelarUber = false;
    private Button BotaoChamadaMotoTaxi;
    private ConstraintLayout layoutLocalPassageiro;
    private DatabaseReference firebaseRef;
    private Requisicao atributoRequisicaoPassageiro;
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private String valorIdUsuario;
    private Usuarios dadosUser;
    public Usuarios dadosPassageiro;
    public Destino destino;
    public Marker marcadorMotoTaxi;
    public Marker marcadorPassageiro;
    public Marker marcadorDestinoViagem;
    public LatLng localizacaoPassageiro;

    private LatLng localizacaoMotorista;
    private Usuarios Dadosmotorista;
    public String statusRequisicao;
    private ConstraintLayout layoutInformacoesMotorista;

    private static TextView nomeMotoristaInfo, kilometragemFinal;
    private static String imageUri;
    private ImageView ivBasicImage;

    public float distancia;
    private Requisicao dadosReq;
    public Usuarios buscarDadosRequisicao;
    private String id;

    public int mapaStiloUber = R.raw.uber_style;
    public LatLng localDestino;
    public Requisicao requisicao;
    private static final String Canal_ID = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapassageiro);

        campoTextoMeuDestino = findViewById(R.id.txtMeuDestino);
        campoTextoMeuLocal = findViewById(R.id.txtMeulocal);
        BotaoChamadaMotoTaxi = findViewById(R.id.btChamarUmTaxi);
        layoutLocalPassageiro = findViewById(R.id.layoutLocal);
        layoutInformacoesMotorista = findViewById(R.id.layoutInfo);
        nomeMotoristaInfo = findViewById(R.id.textNomeInfoMotorista);
        ivBasicImage = findViewById(R.id.imagemInfoCondutor);
        kilometragemFinal = findViewById(R.id.txtInfoDistancia);

        layoutInformacoesMotorista.setVisibility(View.GONE);
        //configuracoes firebase
        firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buscador();
        //metodo alterar status da requisicao feita pelo passageiro
        validarStatusRequisicaoPassageiro();


    }

    @Override
    protected void onStart() {
        super.onStart();
        buscador();
    }

    public void chamarMotoTaxi(View view) {
        if (cancelarUber) { //Uber nao foi chamado

            //cancelar requisicao
            atributoRequisicaoPassageiro.setStatus(Requisicao.STATUS_CANCELADA);
            atributoRequisicaoPassageiro.atualizarStatus();


        } else {
            String meuDestino = campoTextoMeuDestino.getText().toString();
            if (!meuDestino.equals("")) { //meu destino nao for vazio
                Address addressDestino = recuperarEndereco(meuDestino); // rua otavio mangabeira

                final Destino destino = new Destino();
                destino.setCidade(addressDestino.getSubAdminArea());
                destino.setCep(addressDestino.getPostalCode());
                destino.setBairro(addressDestino.getSubThoroughfare());
                destino.setRua(addressDestino.getThoroughfare());
                destino.setNumero(addressDestino.getFeatureName());
                //latitude e longitude
                destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                StringBuilder mensagem = new StringBuilder();
                mensagem.append("Cidade: " + destino.getCidade());
                mensagem.append("Rua: " + destino.getRua());
                mensagem.append("Bairro" + destino.getBairro());
                mensagem.append("Numero " + destino.getNumero());
                mensagem.append("Cep " + destino.getCep());
                mensagem.append("Latitude " + destino.getLatitude());
                mensagem.append("Longitude " + destino);
                if (destino.getLatitude().equals(destino.getLatitude())) {
                    Toast.makeText(Mapassageiro.this, "A latitude é gual a ela mesma", Toast.LENGTH_LONG).show();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Confirme o seu endereco")
                        .setMessage(mensagem)
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //salvar requisicao

                                salvarRequisicaoPassageiro(destino);
                            }
                        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog dialogo = builder.create();
                dialogo.show();

            } else {
                Toast.makeText(this, "Informe um endereço de destino", Toast.LENGTH_LONG).show();
            }
            Toast.makeText(Mapassageiro.this, "Tem que cancelar", Toast.LENGTH_LONG).show();
        }

    }


    private Address recuperarEndereco(String meuEndereco) { //recebe rua otavio mangabeira
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEndereco = geocoder.getFromLocationName(meuEndereco, 1);
            Address address = listaEndereco.get(0);
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void salvarRequisicaoPassageiro(Destino destino) {
        requisicao = new Requisicao();
        requisicao.setDestino(destino); //rua joaquim macedo, 90, 4752000


        Usuarios DadosdoPassageiroLogado = UsuarioFirebase.pegarDadosPassageiroLogado(); // luciano drop, id:3948237492, luciano@gmail.com


        DadosdoPassageiroLogado.setLatitude(String.valueOf(localizacaoPassageiro.latitude)); //Minha localizacao no momento
        DadosdoPassageiroLogado.setLongitude(String.valueOf(localizacaoPassageiro.longitude));//Minha localizacao no momento



        requisicao.setPassageiro(DadosdoPassageiroLogado); // luciano drop, id:3948237492, luciano@gmail.com
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);

        requisicao.setUrlImagem(dadosUser.getUrl()); //Salva a url da imagem trazendo o valor do metodo buscar Url que pega via ID do usuario logado
        requisicao.setPrecoViagem("5,00");


        requisicao.salvar();
        layoutLocalPassageiro.setVisibility(View.GONE);
        BotaoChamadaMotoTaxi.setText("Cancelar Moto Taxi");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        buscarUrlUsuario();
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                //recuperar latitude e longitude
                double latitude = location.getLatitude(); //43.844464
                double longitude = location.getLongitude(); //42.764674
                localizacaoPassageiro = new LatLng(latitude, longitude); // meu local recebe latitude e longitude
                //Atualizar geofire com mudança de lugar
                UsuarioFirebase.atualizaDadosLocalizacaoGeoFire(latitude, longitude);

                //Altera a interface de acordo com os status que vierem tipo: AGUARDANDO, ACAMINHO, EM VIAGEM, FINALIZADA
                alteraInterfaceStatusRequisicao(statusRequisicao);
                if (statusRequisicao != null && !statusRequisicao.isEmpty()) {
                    if (statusRequisicao.equals(Requisicao.STATUS_VIAGEM)
                            || statusRequisicao.equals(Requisicao.STATUS_FINALIZADA)) {

                        locationManager.removeUpdates(locationListener);
                    } else { // caso seja uma outra requisicao de status tipo ENCERRADA OU AGUARDANDO EXIBE O MAPA

                        //solicitar atualizacoes de localizacao
                        if (ActivityCompat.checkSelfPermission(Mapassageiro.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
                            return;
                        }
                    }
                }

                //Geocoder ---
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listaEndereco = geocoder.getFromLocation(latitude, longitude, 1);
                    Address enderecoRecebe = listaEndereco.get(0);
                    campoTextoMeuLocal.setText(enderecoRecebe.getSubAdminArea() + "," + enderecoRecebe.getThoroughfare() + " nº: " + enderecoRecebe.getSubThoroughfare());


                } catch (IOException e) {
                    e.printStackTrace();
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

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, mapaStiloUber));

        //solicitar atualizacoes de localizacao
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            return;
        }

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
                //verificar se a lista nao e nula e tem requisicao
                if (lista != null && lista.size() > 0) {
                    atributoRequisicaoPassageiro = lista.get(0); //pega a lista que tiver atual cadastro do id no banco

                    if (atributoRequisicaoPassageiro != null) {


                        if (!atributoRequisicaoPassageiro.getStatus().equals(Requisicao.STATUS_ENCERRADA)) {

                            dadosPassageiro = atributoRequisicaoPassageiro.getPassageiro(); // recebendo os dados do passageiro via requisicao que pega pelo id do usuario logado
                            localizacaoPassageiro = new LatLng(
                                    Double.parseDouble(dadosPassageiro.getLatitude()), //converte pra double e pega do passageiro logado a latitude
                                    Double.parseDouble(dadosPassageiro.getLongitude()) // converte pra double e pega do passageiro logado a longitude
                            );
                            statusRequisicao = atributoRequisicaoPassageiro.getStatus(); //status requisicao recebe atraves do getStatus o status do banco de dados

                            destino = atributoRequisicaoPassageiro.getDestino();
                            if (atributoRequisicaoPassageiro.getMotorista() != null) {
                                Dadosmotorista = atributoRequisicaoPassageiro.getMotorista();

                                localizacaoMotorista = new LatLng( // faz uma busca na latitude e longitude pra evitar da dados Null, nulos
                                        Double.parseDouble(Dadosmotorista.getLatitude()),
                                        Double.parseDouble(Dadosmotorista.getLongitude())

                                );
                            }


                            //chamando metodo do status
                            alteraInterfaceStatusRequisicao(statusRequisicao);
                            //

                        }
                    }


                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void alteraInterfaceStatusRequisicao(String status) {

        if (status != null && !status.isEmpty()) {
            cancelarUber = false;
            //switch de caso de cada status
            switch (status) {

                case Requisicao.STATUS_AGUARDANDO:
                    meuStatusAguardando();

                    break;

                case Requisicao.STATUS_A_CAMINHO: //caso a requisicao do estatus for igual a A CAMINHO faca isso ai

                    layoutLocalPassageiro.setVisibility(View.GONE);
                    layoutInformacoesMotorista.setVisibility(View.VISIBLE);

                    BotaoChamadaMotoTaxi.setText("Moto Taxi a caminho");
                    BotaoChamadaMotoTaxi.setEnabled(false);


                    adicionarMarcadorPassageiro(localizacaoPassageiro, dadosPassageiro.getNome());
                    adicionarMarcadorMotoTaxi(localizacaoMotorista, Dadosmotorista.getNome());
                    centralizarDoisMarcadores(marcadorMotoTaxi, marcadorPassageiro);

                    nomeMotoristaInfo.setText(Dadosmotorista.getNome()); //Aqui pega o nome do moto taxi e joga no painel de informacao do Passageiro

                    //Log.d("AQUI ESTA A URL IMAGEM", "URL AQUI "+Dadosmotorista.getId());
                    recuperarFotoMotorista(); //metodo tras a foto do motorista para o painel de informacao

                    distancia = Local.calcularDistanciaApp(localizacaoMotorista, localizacaoPassageiro);

                    String distanciaFormatada = Local.formatarDistancia(distancia);
                    kilometragemFinal.setText(distanciaFormatada + " - aproximadamente");

                    break;

                case Requisicao.STATUS_VIAGEM:
                    requisicaoStatusEmViagem();
                    break;

                case Requisicao.STATUS_FINALIZADA:
                    requisicaoStatusFinalizada();
                    break;


                case Requisicao.STATUS_CANCELADA:
                    requisicaoStatusCancelada();
                    break;
            }
        } else {
            //Adicionar marcador de passageiro
            adicionarMarcadorPassageiro(localizacaoPassageiro, "Seu Local");
            centralizarUmMarcador(localizacaoPassageiro);
        }


    }

    public void meuStatusAguardando() {
        layoutLocalPassageiro.setVisibility(View.GONE);
        BotaoChamadaMotoTaxi.setText("Cancelar Moto Taxi");
        cancelarUber = true;
        adicionarMarcadorPassageiro(localizacaoPassageiro, dadosPassageiro.getNome());
        centralizarUmMarcador(localizacaoPassageiro);
    }

    public void requisicaoStatusEmViagem() {
        layoutLocalPassageiro.setVisibility(View.GONE);
        layoutInformacoesMotorista.setVisibility(View.VISIBLE);
        BotaoChamadaMotoTaxi.setText("Moto Taxi Acaminho do Destino");
        BotaoChamadaMotoTaxi.setEnabled(false);
        //adicionar marcador moto taxi
        adicionarMarcadorMotoTaxi(localizacaoMotorista, Dadosmotorista.getNome());
        AlertaNotificacaoViagem();
        nomeMotoristaInfo.setText(Dadosmotorista.getNome()); //Aqui pega o nome do moto taxi e joga no painel de informacao do Passageiro

        //Log.d("AQUI ESTA A URL IMAGEM", "URL AQUI "+Dadosmotorista.getId());
        recuperarFotoMotorista(); //metodo tras a foto do motorista para o painel de informacao

        localDestino = new LatLng( //atribuir uma nova variavel com a localizacao do destino pra onde o passageiro quer ficr
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );

        //adicionar marcador de viagem
        adicionarMarcadorViagem(localDestino, "Destino");

        //centralizar mdois marcadores
        centralizarDoisMarcadores(marcadorMotoTaxi, marcadorDestinoViagem);
        distancia = Local.calcularDistanciaApp(localizacaoMotorista, localDestino);

        String distanciaFormatada = Local.formatarDistancia(distancia);
        kilometragemFinal.setText(distanciaFormatada + " - Distância");


    }


    //Metodo requisicao finalizada
    public void requisicaoStatusFinalizada() {
        //layoutLocalPassageiro.setVisibility(View.GONE);
        startActivity(new Intent(Mapassageiro.this, PagamentoPassageiro.class));

    }


    //Metodo exibir o MotoTaxi no mapa
    public void adicionarMarcadorMotoTaxi(LatLng trazendoLocalizacaoMotorista, String nomeMotoTaxi) {
        if (marcadorMotoTaxi != null)  // se o marcador for diferente de NULO
            marcadorMotoTaxi.remove();
        marcadorMotoTaxi = mMap.addMarker(new MarkerOptions().position(trazendoLocalizacaoMotorista).title(nomeMotoTaxi)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.lp)));


    }


    //Metodo exibir o Passageiro no mapa junto com o MotoTaxi
    public void adicionarMarcadorPassageiro(LatLng trazendoLocalizacaoPassageiro, String nomePassageiro) {
        if (marcadorPassageiro != null)
            marcadorPassageiro.remove();
        marcadorPassageiro = mMap.addMarker(new MarkerOptions().position(trazendoLocalizacaoPassageiro).title(nomePassageiro)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.lp)));


    }


    //Metodo Centralizar Dois Marcadores
    public void centralizarDoisMarcadores(Marker marcador1, Marker marcador2) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(marcador1.getPosition());
        builder.include(marcador2.getPosition());
        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacoInterno)
        );
    }


    //Metodo adicionar um marcador como estatus de viagem
    private void adicionarMarcadorViagem(LatLng trazendoLocalizacao, String nomeMoto) {
        if (marcadorPassageiro != null)
            marcadorPassageiro.remove();

        if (marcadorDestinoViagem != null)  // se o marcador for diferente de NULO
            marcadorDestinoViagem.remove();
        marcadorDestinoViagem = mMap.addMarker(new MarkerOptions().position(trazendoLocalizacao).title(nomeMoto)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pdestino64)));

    }


    //Metodo centralizar apenas um marcador
    public void centralizarUmMarcador(LatLng local) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 15));
    }

    private void requisicaoStatusCancelada() {
        layoutLocalPassageiro.setVisibility(View.VISIBLE);
        BotaoChamadaMotoTaxi.setText("Chamar Moto Taxi");
        cancelarUber = false;
    }


    public static String pegarIdentificadorUsuario() {
        return pegarAutenticacaoUsuario().getUid();
    }

    public static FirebaseUser pegarAutenticacaoUsuario() {
        FirebaseAuth usuarioAuth = ConfiguracoesFirebase.metodopegarAutenticacaoFirebaseAuth();
        return usuarioAuth.getCurrentUser();
    }

    public void buscador() {
        valorIdUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void recuperarFotoMotorista() {

        DatabaseReference usuarios = referencia.child("usuarios");
        DatabaseReference usuarioPesquisa = usuarios.child(Dadosmotorista.getId());
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


    public void buscarUrlUsuario() {
        DatabaseReference usuarios = referencia.child("usuarios");
        DatabaseReference usuarioPesquisa = usuarios.child(valorIdUsuario);
        usuarioPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dadosUser = dataSnapshot.getValue(Usuarios.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void AlertaNotificacaoViagem() {
        Intent notificationIntent = new Intent(Mapassageiro.this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity(Mapassageiro.this, 0, notificationIntent, 0);


        Uri SomAlarme = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), SomAlarme);
        mp.start();


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Mapassageiro.this, Canal_ID)
                .setSmallIcon(R.drawable.h)
                .setContentTitle("Hey! Moto Taxi")
                .setContentIntent(resultIntent)
                .setContentText("Olá, o seu Moto Taxi está chegando, aguarde alguns instantes...");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }

}
