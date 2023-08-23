package com.example.mysmartplugdemo;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {
    private TextView tvDeviceHistory;
    //private static final long DURACION_MAXIMA = 7 * 24 * 60 * 60 * 1000; // 1 semana en milisegundos
    //private static final long DURACION_MAXIMA = 60 * 1000; // 1 min en ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_history);
        initViews();

        //Tiempo en ms del rato que ha estado encendido
        ArrayList listaTiempos = getIntent().getIntegerArrayListExtra("listaTiempos");
        List<Date> listaEncendidos = (List<Date>) getIntent().getSerializableExtra("listaEncendidos");

        List<Double> powerList = (List<Double>) getIntent().getSerializableExtra("powerList");
        List<Double> costList = (List<Double>) getIntent().getSerializableExtra("costList");
        //double powerConsumedValue = getIntent().getDoubleExtra("powerConsumedValue", 0);

        // Verificar si las listas no son nulas antes de usarlas
        mostrarHistorial(listaTiempos, listaEncendidos, powerList, costList);

    }

    private void mostrarHistorial(ArrayList<Long> listaTiempos, List<Date> listaEncendidos, List<Double> powerList, List<Double> costList) {
        if (listaEncendidos != null && !listaEncendidos.isEmpty() && powerList != null && !costList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            //SpannableStringBuilder sb = new SpannableStringBuilder();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.WEEK_OF_YEAR, -1); // Restar 1 semana a la fecha actual
            Date fechaLimite = calendar.getTime(); //la fecha límite será no más de 1 semana

            int registrosMostrados = 0;
            int size = Math.min(listaTiempos.size(), listaEncendidos.size());
            for (int i = size - 1; i >= 0; i--) {
                Date encendido = listaEncendidos.get(i);

                // Comparar la fecha de encendido con la fecha límite
                if (encendido.after(fechaLimite)) {
                    long tiempoEncendido = listaTiempos.get(i);

                    long duracionSegundos = tiempoEncendido / 1000;

                    // Calcular las horas, minutos y segundos de la duración
                    long horas = duracionSegundos / 3600;
                    long minutos = (duracionSegundos % 3600) / 60;
                    long segundos = duracionSegundos % 60;
                    long ms = tiempoEncendido % 1000;

                    // Crear un formato para los números con 6 decimales
                    DecimalFormat decimalFormat = new DecimalFormat("#.######");

                    sb.append("<b>Encendido ").append(registrosMostrados + 1).append(": </b>")
                            .append(encendido).append("<br>");
                    sb.append("Duración: ").append(horas).append(" horas, ").append(minutos)
                            .append(" minutos, ").append(segundos).append(" segundos, ")
                            .append(ms).append(" milisegundos").append("<br><br>");

                    // Obtener el valor de consumo de powerList para el encendido actual
                    if (i < powerList.size() && i < costList.size()) {
                        double powerValue = powerList.get(i);
                        double costValue = costList.get(i);
                        double costeFinal = getFinalCost(powerValue, costValue);

                        String formattedPowerValue = decimalFormat.format(powerValue);
                        String formattedCosteFinal = decimalFormat.format(costeFinal);

                        sb.append("<i>Consumo de potencia estimado: </i>").append(formattedPowerValue).append(" kWh <br><br>");
                        sb.append("<i>Coste estimado del consumo: </i>").append(formattedCosteFinal).append(" € <br><br><br>");

                    } else {
                        sb.append("Consumo de Potencia: Desconocido <br><br>");
                    }

                    registrosMostrados++;
                }
            }
            //tvDeviceHistory.setText(sb.toString());
            tvDeviceHistory.setText(HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            tvDeviceHistory.setText("No hay registros de encendido previos.");
        }
    }

    private double getFinalCost (double powerValue, double costValue){
        return powerValue * costValue;
    }

    private void initViews() {
        tvDeviceHistory = findViewById(R.id.tvDeviceHistory);
    }

}
