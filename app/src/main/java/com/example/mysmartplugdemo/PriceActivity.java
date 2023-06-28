package com.example.mysmartplugdemo;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
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
    private TableLayout tvPriceTable;

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
                /*try {
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

                 */
                try {
                    Iterator<String> keys = response.keys();

                    while (keys.hasNext()){
                        String key = keys.next();
                        JSONObject hourData = response.getJSONObject(key);

                        String hour = hourData.getString("hour");
                        String price = hourData.getString("price");

                        addRowToTable(hour, price);
                    }

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

    private void addRowToTable(String hour, String price) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView tvHour = new TextView(this);
        tvHour.setText(hour);
        tvHour.setPadding(10, 10, 10, 10);
        tvHour.setBackgroundResource(R.color.white);
        tableRow.addView(tvHour);

        TextView tvPrice = new TextView(this);
        tvPrice.setText(price + " €/MWh");
        tvPrice.setPadding(10, 10, 10, 10);
        tvPrice.setBackgroundResource(R.color.white);
        tableRow.addView(tvPrice);

        tvPriceTable.addView(tableRow);
    }

    private void initViews(){
        tvPriceTable = findViewById(R.id.tvPriceTable);
    }
}
