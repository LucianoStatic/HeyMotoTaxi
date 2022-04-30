package paginas;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.br.hey.mototaxi.R;

class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView textViewNome,textViewRua, textViewDinheiro,textViewStatus;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewNome = itemView.findViewById(R.id.tNomeAdapter);
        textViewRua = itemView.findViewById(R.id.tRuaAdapter);
        textViewDinheiro = itemView.findViewById(R.id.tDinheiroAdapter);
        textViewStatus = itemView.findViewById(R.id.tStatusAdapter);


    }
}
