package paginas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.br.hey.mototaxi.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import model.Requisicao;
import model.Usuarios;

public class HistoricoMotorista extends AppCompatActivity {

    private DatabaseReference referencia;
    private FirebaseRecyclerOptions<Requisicao> options;
    private FirebaseRecyclerAdapter<Requisicao, MyViewHolder> adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_motorista);
        referencia = FirebaseDatabase.getInstance().getReference("RequisicoesPassageiro");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        options = new FirebaseRecyclerOptions.Builder<Requisicao>().setQuery(referencia, Requisicao.class).build();
        adapter = new FirebaseRecyclerAdapter<Requisicao, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Requisicao model) {

                holder.textViewNome.setText("Nome: "+model.getPassageiro().getNome());
                holder.textViewRua.setText("Destino: "+model.getDestino().getRua());
                holder.textViewDinheiro.setText("Valor: "+model.getPrecoViagem());
                holder.textViewStatus.setText("Viagem: "+model.getStatus());
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layoutadapter, parent, false);
                return new MyViewHolder(v);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}
