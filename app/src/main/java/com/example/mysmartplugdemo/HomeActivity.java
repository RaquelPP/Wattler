package com.example.mysmartplugdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private CardView cvDevice;
    private Button btnSearch, btnPrice;
    private TextView tvDeviceName, tvDeviceId, tvProductId;

    String homeName = "MyHome";
    String[] rooms = {"Kitchen", "Bedroom", "Living room"};
    ArrayList<String> roomList;

    //CAMBIAR Y PONER LA DEL WIFI, ESTA NO SIRVE
    private String ssid = "DIGIFIBRA-Kx7d";
    private String password = "PYTdHXSDeu";
    //private String ssid = "lowi66E0";
    //private String password = "A4FXAY6QQLZGZB";

    private HomeBean currentHomeBean;
    private DeviceBean currentDeviceBean;

    ITuyaActivator tuyaActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Detectar los dispositivos que están en la red

        initViews();

        cvDevice.setClickable(false);
        cvDevice.setBackgroundColor(Color.LTGRAY);

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

        cvDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("DeviceId", currentDeviceBean.devId);
                bundle.putString("DeviceName", currentDeviceBean.name);
                bundle.putString("ProductId", currentDeviceBean.productId);
                Intent intent = new Intent(HomeActivity.this, DeviceControlActivity.class);
                intent.putExtras(bundle);
                //startActivity(new Intent(HomeActivity.this, DeviceControlActivity.class));
                startActivity(intent);
            }
        });

    }

    /**
     * Crea Home en la nube y una vez creado, la nube te devuelve todos los valores del Home que
     * se ha creado.
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
     * @param @token
     */
    private void searchDevices(String token){
        //Inicializa parámetros de emparejamiento
        tuyaActivator = TuyaHomeSdk.getActivatorInstance().newMultiActivator(new ActivatorBuilder()
                .setSsid(ssid)
                .setPassword(password)
                .setContext(this)
                .setActivatorModel(ActivatorModelEnum.TY_EZ)
                .setTimeOut(1000)
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
                        Toast.makeText(HomeActivity.this, "Device Detection Successful.", Toast.LENGTH_LONG).show();
                        currentDeviceBean = devResp;
                        cvDevice.setClickable(true);
                        cvDevice.setBackgroundColor(Color.WHITE);
                        tvDeviceId.setText("Device ID: " + currentDeviceBean.devId);
                        tvDeviceName.setText("Device Name: " + currentDeviceBean.name);
                        tvProductId.setText("Product ID: " + currentDeviceBean.productId);
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
     * Función para obtener el token de emparejamiento del dispositivo
     */
    private void getRegistrationToken(){

        long homeId = currentHomeBean.getHomeId();

        TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId, new ITuyaActivatorGetToken() {
            @Override
            public void onSuccess(String token) {
                searchDevices(token);

            }

            @Override
            public void onFailure(String errorCode, String errorMsg) {

            }
        });
    }

    private void initViews(){
        cvDevice = findViewById(R.id.cvDevice);
        btnSearch = findViewById(R.id.btnSearch);
        btnPrice = findViewById(R.id.btnPrice);
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceId = findViewById(R.id.tvDeviceId);
        tvProductId = findViewById(R.id.tvProductId);
    }

}