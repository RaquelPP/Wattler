package com.example.mysmartplugdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.builder.ActivatorBuilder;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.enums.ActivatorEZStepCode;
import com.tuya.smart.sdk.enums.ActivatorModelEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Button btnSearch, btnPrice;
    private TextView tvDeviceName;
    private ImageView deviceImageView;

    private List<DeviceBean> deviceList = new ArrayList<>(); //lista de dispositivos encontrados

    String homeName = "MyHome";
    String[] rooms = {"Kitchen", "Bedroom", "Living room"};
    ArrayList<String> roomList;

    //Cambiar y poner la de casa
    private String ssid = "minombre";
    private String password = "micontrasenia";

    private HomeBean currentHomeBean;

    ITuyaActivator tuyaActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();

        roomList = new ArrayList<>();
        roomList.addAll(Arrays.asList(rooms));
        createHome(homeName, roomList);

        btnPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PriceActivity.class);
                startActivity(intent);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRegistrationToken();
                String currentText = btnSearch.getText().toString();
                if(tuyaActivator == null){
                    Toast.makeText(HomeActivity.this, "Wi-Fi config in progress.", Toast.LENGTH_LONG).show();
                }else{
                    if(currentText.equalsIgnoreCase("Search Devices")){
                        tuyaActivator.start();
                        btnSearch.setText("Stop Search");
                    }else{
                        btnSearch.setText("Search Devices");
                        tuyaActivator.stop();
                    }
                }
            }
        });
    }

    /**
     * Crea Home en la nube y una vez creado, la nube te devuelve todos los valores del Home que
     * se ha creado.
     * @param homeName: nombre de la casa
     * @param roomList: lista de habitaciones
     */
    private void createHome(String homeName, List<String> roomList){
        TuyaHomeSdk.getHomeManagerInstance().createHome(homeName, 0, 0, "", roomList, new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                currentHomeBean = bean;
                Toast.makeText(HomeActivity.this, "Home Creation Successful.", Toast.LENGTH_LONG).show();
                getRegistrationToken();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(HomeActivity.this, "Home Creation failed.", Toast.LENGTH_LONG).show();

            }
        });
    }

    /**
     * Buscar dispositivos según un token de emparejamiento.
     * @param token: token de emparejamiento
     */
    private void searchDevices(String token){
        //Inicializa parámetros de emparejamiento
        tuyaActivator = TuyaHomeSdk.getActivatorInstance().newMultiActivator(new ActivatorBuilder()
                .setSsid(ssid)
                .setPassword(password)
                .setContext(this)
                .setActivatorModel(ActivatorModelEnum.TY_EZ)
                .setTimeOut(100)
                .setToken(token)
                .setListener(new ITuyaSmartActivatorListener() {
                    //Token = pairing token
                    //Context = The context to be set in activity
                    // ssid = name of the wifi network
                    // password = psswrd of the wifi network
                    // activator model = the pairing mode
                    //timeout = timeout in sec. timeout vaule of a pairing task.
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Toast.makeText(HomeActivity.this, "Device Detection failed.", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onActiveSuccess(DeviceBean devResp) {
                        // Multiple callbacks are required to pair multiple devices i
                        Toast.makeText(HomeActivity.this, "Device Detection Successful.", Toast.LENGTH_LONG).show();
                        addDeviceView(devResp);
                        btnSearch.setText("Search Devices");
                        tuyaActivator.stop();
                    }

                    @Override
                    public void onStep(String step, Object data) {
                        switch(step){
                            case ActivatorEZStepCode.DEVICE_BIND_SUCCESS:
                                Toast.makeText(HomeActivity.this, "Device Bind Successful.", Toast.LENGTH_LONG).show();
                                break;
                            case ActivatorEZStepCode.DEVICE_FIND:
                                Toast.makeText(HomeActivity.this, "New Device Found.", Toast.LENGTH_LONG).show();
                                break;
                        }

                    }
                })
        );
    }

    /**
     * Función para obtener un token de emparejamiento
     */
    private void getRegistrationToken(){

        long homeId = currentHomeBean.getHomeId();

        TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId, new ITuyaActivatorGetToken() {
            @Override
            public void onSuccess(String token) {
                searchDevices(token);
            }

            @Override
            public void onFailure(String errorCode, String errorMsg) {}
        });
    }

    /**
     * Añade una nueva vista de dispositivo al contenedor
     * @param device: dispositivo que se añade a la vista de dispositivos
     */
    private void addDeviceView(DeviceBean device) {
        LinearLayout llDeviceContainer = findViewById(R.id.llDeviceContainer);

        View deviceView = createDeviceView(device);
        llDeviceContainer.addView(deviceView);
        deviceView.setClickable(true);
    }

    /**
     * Crea una nueva vista de dispositivos, añade el dispositivo y establece el acceso al mismo
     * @param device: dispositivo para el que se crea la vista
     * @return : la vista del dispositivo
     */
    private View createDeviceView(DeviceBean device) {
        View deviceView = getLayoutInflater().inflate(R.layout.device_layout, null);

        ImageView deviceImageView = deviceView.findViewById(R.id.deviceImageView);
        TextView deviceNameText = deviceView.findViewById(R.id.tvDeviceName);

        loadDeviceIcon(device, deviceImageView);
        deviceNameText.setText(device.getName());

        deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceType(device);
            }
        });
        return deviceView;
    }

    /**
     * Determina el tipo de dispositivo y comienza una actividad en función de este.
     * @param device: dispositivo
     */
    private void deviceType(DeviceBean device){
        Intent intent = new Intent();
        String deviceName = device.getName().toLowerCase(); // Convertir a minúsculas para facilitar la comparación
        if (deviceName.contains("enchufe")) {
            // Es un enchufe, lanzar la actividad para controlar el enchufe
            intent = new Intent(HomeActivity.this, DeviceControlActivity.class);
        } else if (deviceName.contains("Bombilla") || deviceName.contains("Bulb")) {
            // Es una bombilla, lanzar la actividad para controlar la bombilla
            intent = new Intent(HomeActivity.this, BulbControlActivity.class);
        } else {
            // No se reconoce el tipo de dispositivo, mostrar un mensaje de error o realizar alguna acción predeterminada
            Toast.makeText(HomeActivity.this, "Tipo de dispositivo no reconocido", Toast.LENGTH_SHORT).show();
        }
        intent.putExtra("DeviceId", device.getDevId());
        intent.putExtra("DeviceName", device.getName());
        startActivity(intent);
    }

    /**
     * Carga una imagen según el tipo de dispositivo que sea
     * @param device: dispositivo que se compara
     * @param button: imagen del dispositivo a modo de botón
     */
    private void loadDeviceIcon(DeviceBean device, ImageView button) {
        String deviceName = device.getName().toLowerCase(); // Convertir a minúsculas para facilitar la comparación

        if (deviceName.contains("enchufe")) {
            button.setImageResource(R.drawable.plug_icon);
            button.setClickable(true);
        } else if (deviceName.contains("bombilla") || deviceName.contains("bulb")) {
            button.setImageResource(R.drawable.light_icon);
            button.setClickable(true);
        } else {
            // Si no se reconoce el tipo de dispositivo, imagen predeterminada
            button.setImageResource(R.drawable.rayo_);
        }
    }

    /**
     * Inicializa las vistas
     */
    private void initViews(){
        btnSearch = findViewById(R.id.btnSearch);
        btnPrice = findViewById(R.id.btnPrice);
        tvDeviceName = findViewById(R.id.tvDeviceName);
        deviceImageView = findViewById(R.id.deviceImageView);
    }

}