package helper;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

public class Local {
    public static float calcularDistanciaApp(LatLng latLngInicial, LatLng latLngFinal) {
        Location localInicial = new Location("Local Inicial");

        localInicial.setLatitude(latLngInicial.latitude);
        localInicial.setLongitude(latLngInicial.longitude);

        Location localFinal = new Location("Local Final");

        localFinal.setLatitude(latLngFinal.latitude);
        localFinal.setLongitude(latLngFinal.longitude);

        //formula pra calcular a distancia / dividir por 1000 converte em km
        float distancia = localInicial.distanceTo(localFinal) / 1000;

        return distancia;
    }


    public static String formatarDistancia(float distancia) {
        String distanciaFormatada;
        if (distancia < 1) { //se a distancia for menor que 1 estamos trabalhando com metros e se for mais que um estamos trabalhando com km
            distancia = distancia * 1000;
                distanciaFormatada =  Math.round(distancia) + "Metros";

        }else{
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            distanciaFormatada = decimalFormat.format(distancia) +"Kilometros";
        }
        return distanciaFormatada;
    }


}
