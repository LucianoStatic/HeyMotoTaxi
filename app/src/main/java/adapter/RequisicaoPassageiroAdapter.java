package adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.br.hey.mototaxi.R;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.List;

import helper.Local;
import model.Requisicao;
import model.Usuarios;

public class RequisicaoPassageiroAdapter extends RecyclerView.Adapter<RequisicaoPassageiroAdapter.MinhaViewHolder> {
    private List<Requisicao> requisicaoPassageiro;
    private Context context;
    private Usuarios motorista;
    private Requisicao requisicao;

    public RequisicaoPassageiroAdapter(List<Requisicao> requisicaoPassageiro, Context context, Usuarios motorista) {
        this.requisicaoPassageiro = requisicaoPassageiro;
        this.context = context;
        this.motorista = motorista;

    }

    public class MinhaViewHolder extends RecyclerView.ViewHolder {
        TextView nomePassageiro, distancia, endereco;
        ImageView imagemProfile;

        public MinhaViewHolder(View itemView) { // aqui e o construtor inicializador tipo as paginas normais
            super(itemView);

            nomePassageiro = itemView.findViewById(R.id.txtMensagemNomePassageiro);
            distancia = itemView.findViewById(R.id.txtKmPassageiro);
            endereco = itemView.findViewById(R.id.txtEnderecoRequisicao);
            imagemProfile = itemView.findViewById(R.id.imagemAdapterRequisicoes);
        }
    }

    @NonNull
    @Override
    public MinhaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //recebe uma tela que tu faz como layout
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.tela_adapter_requisicoes, parent, false);//Aqui ele pega um Layout que tu tem que criar pra pow os elementos na tela tipo dois textos ou qlq coisa.
        return new MinhaViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MinhaViewHolder holder, int position) {
        requisicao = requisicaoPassageiro.get(position); //pega a posicao da requisicao na lista tipo rua paratinga 89
        Usuarios passageiro = requisicao.getPassageiro(); //vai na requisicao e pega uma instancia do objeto passageiro

        holder.nomePassageiro.setText(passageiro.getNome()); //seta no textview o nome do passageiro
        holder.endereco.setText("Destino: " + requisicao.getDestino().getRua() + " Nº: " + requisicao.getDestino().getNumero()); //seta no textview o nome da rua

        if( (motorista.getLatitude() !=null)&& (motorista.getLongitude() !=null) ){

            LatLng localPassageiro = new LatLng(
                    Double.parseDouble(passageiro.getLatitude()),
                    Double.parseDouble(passageiro.getLongitude())
            );

            LatLng localMotorista  = new LatLng(
                    Double.parseDouble(motorista.getLatitude()),
                    Double.parseDouble(motorista.getLongitude())
            );
            float distancia = Local.calcularDistanciaApp(localPassageiro,localMotorista);
            String distanciaFormatada = Local.formatarDistancia(distancia);
            holder.distancia.setText(distanciaFormatada +"- aproximadamente");
        }else{
            holder.distancia.setText("Aguarde...buscando localizacao ");
        }

        Picasso.get()
                .load(requisicao.getUrlImagem())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imagemProfile);
    }

    @Override
    public int getItemCount() {

        return requisicaoPassageiro.size();
    }
}
