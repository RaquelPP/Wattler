package com.example.mysmartplugdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.android.blemesh.api.IResultWithDataCallback;
import com.tuya.smart.android.device.api.IPropertyCallback;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IDeviceListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.enums.TYDevicePublishModeEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DeviceControlActivity extends AppCompatActivity {

    private TextView tvDeviceName, labelScene, labelWorkMode;
    private Switch swStatus;
    private Button btnHistorial;
    private SeekBar sbBrightness;
    private Spinner spScene, spWorkMode;

    private Date fechaEncendido, fechaApagado;
    private long tiempoTranscurrido;
    private List<Date> listaEncendidos;
    private ArrayList listaTiempos;

    private List<Double> powerList;
    private List<String> dpList;
    private double current_power;

    String devId, devName, prodId;
    String dpIds = "1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        Bundle bundle = getIntent().getExtras();

        initViews();

        if(bundle != null){
            devId = bundle.getString("DeviceId");
            devName = bundle.getString("DeviceName");
            prodId = bundle.getString("ProductId");
            tvDeviceName.setText(devName);
        }

        //Inicializar las listas:
        listaEncendidos = new ArrayList<>();
        listaTiempos = new ArrayList<>();
        powerList = new ArrayList<>();
        dpList = new ArrayList<>();
        dpList.add("19");


        sbBrightness.setVisibility(View.INVISIBLE);
        spWorkMode.setVisibility(View.INVISIBLE);
        spScene.setVisibility(View.INVISIBLE);
        labelWorkMode.setVisibility(View.INVISIBLE);
        labelScene.setVisibility(View.INVISIBLE);

        //CONTROL DEL DISPOSITIVO

        ITuyaDevice controlDevice = TuyaHomeSdk.newDeviceInstance(devId);

        controlDevice.registerDeviceListener(new IDeviceListener() {
            @Override
            public void onDpUpdate(String devId, Map<String, Object> dpStr) {
                try {
                    JSONObject dpJson = new JSONObject(dpStr);

                    if (dpJson.has("cur_power")) {
                        double curPowerValue = dpJson.getDouble("cur_power");

                        if (curPowerValue != 0) {
                            current_power = curPowerValue;
                            //Toast.makeText(DeviceControlActivity.this, "curPowerValue es: " + curPowerValue + "w", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
// D/Tuya: AbsTuyaDevice onDpUpdate dpCodes: {cur_voltage=2182, cur_power=0, relay_status=last, switch_1=false, cur_current=0, add_ele=0, countdown_1=0}
            @Override
            public void onRemoved(String devId) {}

            @Override
            public void onStatusChanged(String devId, boolean online) {}

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {}

            @Override
            public void onDevInfoUpdate(String devId) {}
        });

        swStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controlDevice.publishDps("{\"1\":" + (isChecked ? "true" : "false") + "}", new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Toast.makeText(DeviceControlActivity.this, "Plug Status Change Failed.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess() {
                        //Toast.makeText(DeviceControlActivity.this, "Plug Status Change Success.", Toast.LENGTH_LONG).show();
                        if(isChecked){
                            Toast.makeText(DeviceControlActivity.this, "Se ha encendido.", Toast.LENGTH_LONG).show();
                            fechaEncendido = new Date();
                            listaEncendidos.add(fechaEncendido);//se agrega la fecha de encendido a la lista
                            controlDevice.getDp("19", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {}

                                @Override
                                public void onSuccess() {
                                    //Toast.makeText(DeviceControlActivity.this, "Se ha obtenido getDp 19 en el encendido.", Toast.LENGTH_LONG).show();
                                }
                            });

                        }else {
                            Toast.makeText(DeviceControlActivity.this, "Se ha apagado.", Toast.LENGTH_LONG).show();
                            fechaApagado = new Date();
                            tiempoTranscurrido = fechaApagado.getTime() - fechaEncendido.getTime();
                            listaTiempos.add(tiempoTranscurrido);

                            controlDevice.getDp("19", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {}

                                @Override
                                public void onSuccess() {
                                    //Toast.makeText(DeviceControlActivity.this, "Se ha obtenido getDp 19 en el apagado.", Toast.LENGTH_LONG).show();
                                }
                            });

                            if (current_power != 0) {
                                double consumoEstimado = (current_power/1000) * (tiempoTranscurrido / 3600000.0); // Convertir de w a kw y de ms a horas
                                Toast.makeText(DeviceControlActivity.this, "consumo estimado es: " + consumoEstimado, Toast.LENGTH_LONG).show();

                                powerList.add(consumoEstimado);
                            }
                        }
                    }
                });
            }

        });

        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceControlActivity.this, HistorialActivity.class);
                intent.putExtra("devId", devId);
                intent.putExtra("dpIds", dpIds);
                intent.putExtra("listaTiempos", listaTiempos);
                intent.putExtra("listaEncendidos", (Serializable) listaEncendidos);
                intent.putExtra("powerList", (Serializable) powerList);
                startActivity(intent);
            }
        });
    }

    private void initViews(){
        tvDeviceName = findViewById(R.id.tvDeviceControlName);
        swStatus = findViewById(R.id.swStatus);
        btnHistorial = findViewById(R.id.btnHistorial);
        sbBrightness = findViewById(R.id.sbBrightness);
        spScene = findViewById(R.id.spScene);
        spWorkMode = findViewById(R.id.spWorkMode);
        labelScene = findViewById(R.id.labelScene);
        labelWorkMode = findViewById(R.id.labelWorkMode);
    }

}

