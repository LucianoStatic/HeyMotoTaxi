package paginas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.br.hey.mototaxi.R;

public class CobrancaTaxi extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranca_taxi);
        getSupportActionBar().hide();
    }
}
