package com.example.mysmartplugdemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaDataCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;

public class HistorialActivity extends AppCompatActivity {
    private TextView tvDeviceHistory;

    private static final String TAG = "MySmartPlug";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_history);
        initViews();

        //Obtener el devId de la actividad anterior
        String devId = getIntent().getStringExtra("devId");
        String dpIds= getIntent().getStringExtra("dpIds");

        // Crear el mapa de parámetros para la solicitud del historial
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("devId", devId);
        requestParams.put("dpIds", dpIds);
        requestParams.put("offset", 0);//valor de ejemplo
        requestParams.put("limit", 10);//valor de ejemplo

        TuyaHomeSdk.getRequestInstance().requestWithApiName("tuya.m.smart.operate.all.log", "1.0",
                requestParams, String.class, new ITuyaDataCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        processDeviceHistory(result);
                        tvDeviceHistory.setText(result);
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Log.e(TAG, "Error requesting device history: " + errorCode + " - " + errorMessage);
                        Toast.makeText(HistorialActivity.this, "Error requesting device history. ", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void processDeviceHistory(String result){
        try {
            // Dividir la cadena de resultado en registros individuales
            String[] records = result.split("\\r?\\n");

            for (String record : records) {
                // Dividir cada registro en campos individuales
                String[] fields = record.split(",");

                // Verificar si hay suficientes campos
                if (fields.length >= 3) {
                    String recordId = fields[0];
                    long timestamp = Long.parseLong(fields[1]);
                    boolean powerOn = Boolean.parseBoolean(fields[2]);

                    // Realizar la operación deseada con los datos obtenidos
                    // Mostrarlos en la interfaz de usuario
                    Log.d(TAG, "Record ID: " + recordId);
                    //Log.d(TAG, "Timestamp: " + timestamp);
                    Log.d(TAG, "Power On: " + powerOn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initViews(){

        tvDeviceHistory = findViewById(R.id.tvDeviceHistory);
    }
}
