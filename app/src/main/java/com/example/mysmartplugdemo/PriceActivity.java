package com.example.mysmartplugdemo;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class PriceActivity extends AppCompatActivity {
    private TextView tvPrice;

    private RequestQueue queue;

    private static final String TAG = "MySmartPlug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price);
        initViews();

        queue = Volley.newRequestQueue(this);
        getDataVoley();
    }

    private void getDataVoley(){
        String url = "https://api.preciodelaluz.org/v1/prices/all?zone=PCB";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Iterator<String> keys = response.keys();
                    StringBuilder priceBuilder = new StringBuilder();
                    //JSONObject data = response.getJSONObject("PCB");
                    //JSONArray keys = data.names();

                    while (keys.hasNext()){
                        String key = keys.next();
                        JSONObject hourData = response.getJSONObject(key);

                        String hour = hourData.getString("hour");
                        String price = hourData.getString("price");

                        priceBuilder.append("Hora: ").append(hour).append(", Precio: ").append(price).append(" €/MWh\n");
                        //Toast.makeText(PriceActivity.this, "Hora: "+hour+", Precio: "+ price + " €/MWh", Toast.LENGTH_SHORT).show();
                    }
                    tvPrice.post(new Runnable() {
                        @Override
                        public void run() {
                            tvPrice.setText(priceBuilder.toString());
                        }
                    });



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }



    private void initViews(){
        tvPrice = findViewById(R.id.tvPrice);
    }
}
