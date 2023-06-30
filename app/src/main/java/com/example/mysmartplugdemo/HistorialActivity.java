package com.example.mysmartplugdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaDataCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.LogRecord;

public class HistorialActivity extends AppCompatActivity {
    private TextView tvDeviceHistory;
    //private static final long DURACION_MAXIMA = 7 * 24 * 60 * 60 * 1000; // 1 semana en milisegundos
    private static final long DURACION_MAXIMA = 60 * 1000; // 1 min en ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_history);
        initViews();

        //Tiempo en ms del rato que ha estado encendido
        ArrayList listaTiempos = getIntent().getIntegerArrayListExtra("listaTiempos");
        List<Date> listaEncendidos = (List<Date>) getIntent().getSerializableExtra("listaEncendidos");

        mostrarHistorial(listaTiempos, listaEncendidos);
    }

    private void mostrarHistorial(ArrayList<Long> listaTiempos, List<Date> listaEncendidos) {
        if (listaEncendidos != null && !listaEncendidos.isEmpty()) {
            StringBuilder sb = new StringBuilder();

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

                    sb.append("Encendido ").append(registrosMostrados + 1).append(": ").append(encendido).append("\n");
                    sb.append("Duración: ").append(horas).append(" horas, ").append(minutos).append(" minutos, ").append(segundos).append(" segundos, ").append(ms).append(" milisegundos").append("\n\n");

                    registrosMostrados++;
                }
            }
            tvDeviceHistory.setText(sb.toString());
        } else {
            tvDeviceHistory.setText("No hay registros de encendido previos.");
        }
    }



    private void initViews() {
        tvDeviceHistory = findViewById(R.id.tvDeviceHistory);
    }

}
