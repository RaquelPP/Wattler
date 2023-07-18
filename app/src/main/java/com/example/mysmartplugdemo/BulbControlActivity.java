package com.example.mysmartplugdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.centralcontrol.TuyaLightDevice;
import com.tuya.smart.sdk.api.IDeviceListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.centralcontrol.api.ITuyaLightDevice;
import com.tuya.smart.sdk.centralcontrol.api.constants.LightMode;
import com.tuya.smart.sdk.centralcontrol.api.constants.LightScene;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BulbControlActivity extends AppCompatActivity {

    private TextView tvDeviceName;
    private Switch swStatus;
    private Spinner spWorkMode, spScene;
    private SeekBar sbBrightness;

    private Button btnHistorial;
    private Date fechaEncendido, fechaApagado;
    private long tiempoTranscurrido;
    private List<Date> listaEncendidos;
    private ArrayList listaTiempos;

    String devId, devName, prodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        Bundle bundle = getIntent().getExtras();

        initViews();

        String[] scenes = new String[]{"Goodnight", "Casual", "Work", "Read"};//light mode
        String [] workModes = new String[]{"Scene", "White", "Color"};

        //????
        ArrayAdapter<String> sceneAdapter = new ArrayAdapter<>(this, com.tuya.sdk.log.R.layout.support_simple_spinner_dropdown_item, scenes);
        ArrayAdapter<String> workModeAdapter = new ArrayAdapter<>(this, com.tuya.sdk.log.R.layout.support_simple_spinner_dropdown_item, workModes);
        spScene.setAdapter(sceneAdapter);
        spWorkMode.setAdapter(workModeAdapter);

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

        ITuyaLightDevice controlDevice = new TuyaLightDevice(devId);

        controlDevice.registerDeviceListener(new IDeviceListener() {
            @Override
            public void onDpUpdate(String devId, Map<String, Object> dpStr) {

            }

            @Override
            public void onRemoved(String devId) {

            }

            @Override
            public void onStatusChanged(String devId, boolean online) {

            }

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }

            @Override
            public void onDevInfoUpdate(String devId) {

            }
        });


        swStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                controlDevice.powerSwitch(isChecked, new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Toast.makeText(BulbControlActivity.this, "Light Change Failed.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess() {
                        if(isChecked){
                            Toast.makeText(BulbControlActivity.this, "Se ha encendido.", Toast.LENGTH_LONG).show();
                            fechaEncendido = new Date();
                            listaEncendidos.add(fechaEncendido);//se agrega la fecha de encendido a la lista

                        }else {
                            Toast.makeText(BulbControlActivity.this, "Se ha apagado.", Toast.LENGTH_LONG).show();
                            fechaApagado = new Date();
                            tiempoTranscurrido = fechaApagado.getTime() - fechaEncendido.getTime();
                            listaTiempos.add(tiempoTranscurrido);
                        }                    }
                });
            }
        });
        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BulbControlActivity.this, HistorialActivity.class);
                intent.putExtra("devId", devId);
                intent.putExtra("listaTiempos", listaTiempos);
                intent.putExtra("listaEncendidos", (Serializable) listaEncendidos);
                startActivity(intent);
            }
        });
        //Aquí pondríamos el ajuste del brillo también

        sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                controlDevice.brightness(progress, new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {

                    }

                    @Override
                    public void onSuccess() {

                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Elegir el modo de luz

        spWorkMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LightMode selectedLightMode = LightMode.MODE_WHITE;
                String selectedWorkMode = workModeAdapter.getItem(position);

                switch (selectedWorkMode){
                    case "Scene":
                        selectedLightMode = LightMode.MODE_SCENE;
                        break;
                    case "White":
                        selectedLightMode = LightMode.MODE_WHITE;
                        break;
                    case "Color":
                        selectedLightMode = LightMode.MODE_COLOUR;
                        break;
                }

                controlDevice.workMode(selectedLightMode, new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Toast.makeText(BulbControlActivity.this, "Work Mode Change Failed.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(BulbControlActivity.this, "Work Mode Change Successful.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spScene.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LightScene selectedLightScene = LightScene.SCENE_CASUAL;
                String selectedScene = sceneAdapter.getItem(position);

                switch (selectedScene){
                    case "Goodnight":
                        selectedLightScene = LightScene.SCENE_GOODNIGHT;
                        break;
                    case "Work":
                        selectedLightScene = LightScene.SCENE_WORK;
                        break;
                    case "Read":
                        selectedLightScene = LightScene.SCENE_READ;
                        break;
                    case "Casual":
                        selectedLightScene = LightScene.SCENE_CASUAL;
                        break;
                }

                controlDevice.scene(selectedLightScene, new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Toast.makeText(BulbControlActivity.this, "Scene Change Failed.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(BulbControlActivity.this, "Scene Change Successful.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initViews(){
        tvDeviceName = findViewById(R.id.tvDeviceControlName);
        swStatus = findViewById(R.id.swStatus);
        spWorkMode = findViewById(R.id.spWorkMode);
        spScene = findViewById(R.id.spScene);
        sbBrightness = findViewById(R.id.sbBrightness);
    }

}
