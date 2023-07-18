package com.example.mysmartplugdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.centralcontrol.TuyaLightDevice;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IDeviceListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.centralcontrol.api.ITuyaLightDevice;
import com.tuya.smart.sdk.centralcontrol.api.constants.LightMode;
import com.tuya.smart.sdk.centralcontrol.api.constants.LightScene;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DeviceControlActivity extends AppCompatActivity {

    private TextView tvDeviceName;
    private Switch swStatus;
    private Button btnHistorial;
    private Date fechaEncendido, fechaApagado;
    private long tiempoTranscurrido;
    private List<Date> listaEncendidos;
    private ArrayList listaTiempos;

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

        //Inicializar la lista de encendidos:
        listaEncendidos = new ArrayList<>();
        listaTiempos = new ArrayList<>();

        //CONTROL DEL DISPOSITIVO

        ITuyaDevice controlDevice = TuyaHomeSdk.newDeviceInstance(devId);

        controlDevice.registerDeviceListener(new IDeviceListener() {
            @Override
            public void onDpUpdate(String devId, Map<String, Object> dpStr) {}

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

                        }else {
                            Toast.makeText(DeviceControlActivity.this, "Se ha apagado.", Toast.LENGTH_LONG).show();
                            fechaApagado = new Date();
                            tiempoTranscurrido = fechaApagado.getTime() - fechaEncendido.getTime();
                            listaTiempos.add(tiempoTranscurrido);
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
                startActivity(intent);
            }
        });
    }

    private void initViews(){
        tvDeviceName = findViewById(R.id.tvDeviceControlName);
        swStatus = findViewById(R.id.swStatus);
        btnHistorial = findViewById(R.id.btnHistorial);
    }

}

