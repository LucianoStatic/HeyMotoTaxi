package paginas;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.br.hey.mototaxi.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.text.DecimalFormat;
import java.util.List;

import helper.Local;
import helper.UsuarioFirebase;
import model.Destino;
import model.Requisicao;
import model.Usuarios;
import pconfiguracaofirebase.ConfiguracoesFirebase;

public class PainelViagensMotorista extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private LatLng localizacaoMotorista;
    private Usuarios Dadosmotorista;
    private String idRequisicao;
    private Requisicao requisicao;
    private static final String Canal_ID = "default";
    private DatabaseReference firebaseRef;
    private Button botaoAceitar, botaoEncerrarViagem, botaoTestar;
    public Marker marcadorMotoTaxi;
    public Marker marcadorPassageiro;
    public Marker marcadorDestinoViagem;
    public LatLng localizacaoPassageiro;
    public Usuarios dadosPassageiro;
    public String statusRequisicao;
    private FloatingActionButton floatBotaoRotas;
    public Destino destino;
    private boolean requisicaoAtiva;
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private static String imageUri;
    public Requisicao Dmotorista;
    private Usuarios dadosUser;
    private ImageView ivBasicImage;
    private ConstraintLayout layoutInformacoesPassageiro;
    private TextView textNomeInfoPassageiro, textNomeRuaPassageiro, textKilometragemFinal;
    public float distancia;
    public int mapaStiloUber = R.raw.uber_style;
    public LatLng localDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painel_viagens_motorista);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Botao aceitar
        textNomeInfoPassageiro = findViewById(R.id.txNomeInfoPassageiro);
        textNomeRuaPassageiro = findViewById(R.id.textDestinoPassageiro);
        textKilometragemFinal = findViewById(R.id.textDistanciaAtePassageiro);
        botaoAceitar = findViewById(R.id.botaoAceitarUmaViagem);
        botaoEncerrarViagem = findViewById(R.id.botaoEncerrarViagemAgora);
        ivBasicImage = findViewById(R.id.imagemInfoPassageiro);
        layoutInformacoesPassageiro = findViewById(R.id.layoutInfo);
        floatBotaoRotas = findViewById(R.id.floatRota);

        layoutInformacoesPassageiro.setVisibility(View.GONE);
        botaoEncerrarViagem.setVisibility(View.GONE);
        botaoEncerrarViagem.setEnabled(false);

        //configuracoes iniciais do firebase
        firebaseRef = ConfiguracoesFirebase.metodoPegarFirebaseDataBase();

        //recupera dados do usuario la da requisicao de viagens
        if (getIntent().getExtras().containsKey("idRequisicao") && getIntent().getExtras().containsKey("motorista")) {
            //so vai funcionar aqui se tiver encontrado o ID da Requisicao e o Motorista
            // ja trazendo o IDREQUISICAO  e o nome do MOTORISTA
            Bundle extras = getIntent().getExtras();
            Dadosmotorista = (Usuarios) extras.getSerializable("motorista");

            localizacaoMotorista = new LatLng( // faz uma busca na latitude e longitude pra evitar da dados Null, nulos
                    Double.parseDouble(Dadosmotorista.getLatitude()),
                    Double.parseDouble(Dadosmotorista.getLongitude())

            );

            idRequisicao = extras.getString("idRequisicao");

            //metodo pra saber qual estado ta requisicao ta se e AGUARDANDO, A CAMINHO, CHEGOU
            //esse metodo fica rodando pra saber qual e a requisicao do passageiro
            saberStatusRequisicaoPassageiro();
        }

        //adicionar evento de clique abrir rotas
        floatBotaoRotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String varStatus = statusRequisicao;
                if (varStatus != null && !varStatus.isEmpty()) {
                    String varLatitude = "";
                    String varLongitude = "";
                    switch (varStatus) {
                        case Requisicao.STATUS_A_CAMINHO:
                            varLatitude = String.valueOf(localizacaoPassageiro.latitude);
                            varLongitude = String.valueOf(localizacaoPassageiro.longitude);
                            break;
                        case Requisicao.STATUS_VIAGEM:
                            varLatitude = destino.getLatitude();
                            varLongitude = destino.getLongitude();
                            break;
                    }
                    //abrir uma rota
                    String pegarValoresLatitudeLogitude = varLatitude + "," + varLongitude;
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + pegarValoresLatitudeLogitude + "&mode=d");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            }

        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //recuperar latitude e longitude
                double latitude = location.getLatitude(); //43.844464
                double longitude = location.getLongitude(); //42.764674
                localizacaoMotorista = new LatLng(latitude, longitude); // meu local recebe latitude e longitude
                //Atualizar geofire com mudança de lugar
                UsuarioFirebase.atualizaDadosLocalizacaoGeoFire(latitude, longitude);

                Dadosmotorista.setLatitude(String.valueOf(latitude));
                Dadosmotorista.setLongitude(String.valueOf(longitude));

                requisicao.setMotorista(Dadosmotorista); // recebendo a latitude e longitude convertida da variavel DadosMotorista

                //Atualizar localizacao do motorista no firebase
                requisicao.metodoAtualizarLocalizacaoMotorista();


                //chama o metodo alterar interface status
                alteraInterfaceStatusRequisicao(statusRequisicao);


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

    public void AceitarViagem(View view) {

        //configurar a requisicao pra aceitar uma viagem, onde tem que pegar os dados recuperados da requisicao do passageiro
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao); //pega o ID la no Oncreate -mu347yhfusdf
        requisicao.setMotorista(Dadosmotorista); // pega o nome do Motorista la no Oncreate -  NOME MOTORISTA
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO); // seta o valor do estatus onde ta como A CAMINHO
        //chamando o metodo atualizar
        requisicao.metodoAtualizar(); //chamando metodo atualizar da classe REQUISICAO
        AlertaNotificacaoViagem();

    }

    public void AlertaNotificacaoViagem() {
        Intent notificationIntent = new Intent(PainelViagensMotorista.this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity(PainelViagensMotorista.this, 0, notificationIntent, 0);


        Uri SomAlarme = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), SomAlarme);
        mp.start();


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(PainelViagensMotorista.this, Canal_ID)
                .setSmallIcon(R.drawable.h)
                .setContentTitle("Hey! Moto Taxi")
                .setContentIntent(resultIntent)
                .setContentText("Olá, o seu Moto Taxi está chegando, aguarde alguns instantes...");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }


    public void saberStatusRequisicaoPassageiro() {

        DatabaseReference requisicoesPassageiro = firebaseRef.child("RequisicoesPassageiro")
                .child(idRequisicao); // vai la no banco de dados e pega a tabela REQUISICOES PASSAGEIRO via o ID da TABELA REQUISICOES PASSAGEIRO
        requisicoesPassageiro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Recupera requisição
                requisicao = dataSnapshot.getValue(Requisicao.class); //com os dados da requisicao passageiro recuperado a requisicao recebe o snap com os dados
                if (requisicao != null) {
                    dadosPassageiro = requisicao.getPassageiro(); // recebendo os dados do passageiro via requisicao que pega pelo id do usuario logado
                    localizacaoPassageiro = new LatLng(
                            Double.parseDouble(dadosPassageiro.getLatitude()), //converte pra double e pega do passageiro logado a latitude
                            Double.parseDouble(dadosPassageiro.getLongitude()) // converte pra double e pega do passageiro logado a longitude
                    );
                    statusRequisicao = requisicao.getStatus(); //status requisicao recebe atraves do getStatus o status do banco de dados

                    destino = requisicao.getDestino();
                    //chamando metodo do status
                    alteraInterfaceStatusRequisicao(statusRequisicao);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Metodo exibir o MotoTaxi no mapa
    public void adicionarMarcadorMotoTaxi(final LatLng trazendoLocalizacaoMotorista, final String nomeMotoTaxi) {
        if (marcadorMotoTaxi != null)  // se o marcador for diferente de NULO

            marcadorMotoTaxi.remove();
        marcadorMotoTaxi = mMap.addMarker(new MarkerOptions()
                .position(trazendoLocalizacaoMotorista)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.lp))
                .title(nomeMotoTaxi));


        Log.d("BORA VER NE MOT", "DEU FOI BOM");

    }


    public void animarMarcador() {

    }


    //Metodo exibir o Passageiro no mapa junto com o MotoTaxi
    public void adicionarMarcadorPassageiro(LatLng trazendoLocalizacaoPassageiro, String nomePassageiro) {
        if (marcadorPassageiro != null)
            marcadorPassageiro.remove();
        marcadorPassageiro = mMap.addMarker(new MarkerOptions().position(trazendoLocalizacaoPassageiro).title(nomePassageiro)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.lp)));

        //Adicionar alguma coisa aqui no ELSE
        Log.d("BORA VER NE MOT", "DEU FOI BOM");

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


    private void alteraInterfaceStatusRequisicao(String status) {
        switch (status) {
            case Requisicao.STATUS_AGUARDANDO:
                meuStatusAguardando();
                break;

            case Requisicao.STATUS_A_CAMINHO: //caso a requisicao do estatus for igual a A CAMINHO faca isso ai

                botaoAceitar.setText("Moto Taxi A Caminho");

                //Exibe o MotoTaxi
                adicionarMarcadorMotoTaxi(localizacaoMotorista, Dadosmotorista.getNome());

                //Exibe o Passageiro
                adicionarMarcadorPassageiro(localizacaoPassageiro, dadosPassageiro.getNome());

                //Centralizar dois marcadores
                centralizarDoisMarcadores(marcadorMotoTaxi, marcadorPassageiro);

                //Iniciar monitoramento do Motorista/Passageiro
                iniciarMonitoramento(Dadosmotorista, localizacaoPassageiro, Requisicao.STATUS_VIAGEM);


                botaoAceitar.setEnabled(false);


                break;


            case Requisicao.STATUS_VIAGEM:
                requisicaoStatusEmViagem();
                break;


            case Requisicao.STATUS_FINALIZADA:
                requisicaoStatusFinalizada();

                break;

            case Requisicao.STATUS_ENCERRADA:


                break;


            case Requisicao.STATUS_CANCELADA:
                requisicaoStatusCancelada();

                break;


        }
    }


    private void meuStatusAguardando() {
        botaoAceitar.setText("Aguardando");
        adicionarMarcadorMotoTaxi(localizacaoMotorista, Dadosmotorista.getNome());
        centralizarUmMarcador(localizacaoMotorista);
    }


    private void iniciarMonitoramento(final Usuarios usuarioOrigem, LatLng localdestino, final String status) {

        DatabaseReference localUsuario = ConfiguracoesFirebase.metodoPegarFirebaseDataBase().child("Endereco_Local_Usuario"); //Define um nome para a tabela no banco de dados Firebase

        GeoFire geoFire = new GeoFire(localUsuario); //recebe a tabela pra ele salvar as latitudes e longitudes de acordo ao andar

        final Circle circulo = mMap.addCircle(   //adicionar um circulo pra idenficiar o passageiro para o motorista
                new CircleOptions()
                        .center(localdestino)
                        .radius(50)//em metros
                        .fillColor(Color.argb(90, 255, 153, 0))
                        .strokeColor(Color.argb(190, 255, 152, 0))
        );
        final GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(localdestino.latitude, localdestino.longitude),
                0.05 //em km(0.05 50 metros)
        );
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) { //vai retornar uma chave dentro dos criterios ou seja uma localizacao de 50m
                if (key.equals(usuarioOrigem.getId())) {
                    requisicao.setStatus(status); //seta a porra do status pra como viajando
                    requisicao.atualizarStatus();

                    geoQuery.removeAllListeners(); //remove o geo query
                    circulo.remove(); // remove o circulo

                    Log.i("APP", "Motorista Esta dentro da area");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    public void requisicaoStatusEmViagem() {

        floatBotaoRotas.setVisibility(View.VISIBLE);
        botaoAceitar.setText("Moto Taxi A Caminho do Destino");
        adicionarMarcadorMotoTaxi(localizacaoMotorista, Dadosmotorista.getNome());
        //exibe marcador motorista
        LatLng localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );

        adicionarMarcadorViagem(localDestino, "Destino");
        //centralizar dois marcadores
        centralizarDoisMarcadores(marcadorMotoTaxi, marcadorDestinoViagem);
        //Inicia monitoramento do motorista / passageiro
        iniciarMonitoramento(Dadosmotorista, localDestino, Requisicao.STATUS_FINALIZADA);
        botaoAceitar.setEnabled(false);
        layoutInformacoesPassageiro.setVisibility(View.VISIBLE);



        adicionarFotoPassageiro();
    }


    public void requisicaoStatusFinalizada() {
        //ocultar botao rota

        floatBotaoRotas.setVisibility(View.GONE);
        botaoAceitar.setVisibility(View.GONE);
        botaoAceitar.setEnabled(false);
        requisicaoAtiva = false;

        if (marcadorMotoTaxi != null)
            marcadorMotoTaxi.remove();

        if (marcadorDestinoViagem != null)
            marcadorDestinoViagem.remove();

        //Exibe marcador destino
         localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );
        adicionarMarcadorViagem(localDestino, "Destino");
        centralizarUmMarcador(localDestino);


        //Calcular distancia

        botaoEncerrarViagem.setVisibility(View.VISIBLE);
        botaoEncerrarViagem.setEnabled(true);
        botaoEncerrarViagem.setText("Viagem finalizada - Total:R$ 5,00 ");
        requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
        requisicao.atualizarStatus();


    }


    public void centralizarUmMarcador(LatLng local) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 27));
    }


    public void requisicaoStatusCancelada() {
        Toast.makeText(this, "Requisição foi cancelada pelo Passageiro", Toast.LENGTH_LONG).show();
        //startActivity(new Intent(PainelViagensMotorista.this,PassageirosRequisicoes.class));
        finish();
    }

    public void metodoEncerramentoRequisicao(View view) {
        startActivity(new Intent(PainelViagensMotorista.this, Requisicoes.class));
        finish();
    }


    public void adicionarFotoPassageiro() {
        DatabaseReference usuarios = referencia.child("usuarios");
        DatabaseReference usuarioPesquisa = usuarios.child(dadosPassageiro.getId());
        usuarioPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dadosUser = dataSnapshot.getValue(Usuarios.class);

                dadosUser = dataSnapshot.getValue(Usuarios.class);
                textNomeInfoPassageiro.setText(dadosPassageiro.getNome()+" - P");
                distancia = Local.calcularDistanciaApp(localizacaoMotorista,localizacaoPassageiro);
                String distanciaFormatada = Local.formatarDistancia(distancia);
                textKilometragemFinal.setText(distanciaFormatada + " - Distância ");
                textNomeRuaPassageiro.setText(destino.getRua()+"-"+destino.getNumero());
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
