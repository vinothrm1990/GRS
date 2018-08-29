package com.app.grs.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;
import com.app.grs.R;
import com.app.grs.adapter.OrderAdapter;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    public static ArrayList<HashMap<String,String>> orderList=new ArrayList<HashMap<String, String>>();
    RecyclerView.LayoutManager mLayoutManager;
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("My Orders");
        setContentView(R.layout.activity_my_order);

        Constants.pref = getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        recyclerView = findViewById(R.id.rv_order);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        String cusid = Constants.pref.getString("mobileno", "");
        new fetchOrder(MyOrderActivity.this, cusid).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        GRS.registerReceiver(MyOrderActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(MyOrderActivity.this);
    }

    private class fetchOrder extends AsyncTask<String, Integer, String> {

        private Context context;
        private String orderimage, ordername, orderqty, orderprice, cusid;
        private String url = Constants.BASE_URL + Constants.ORDER_DETAILS;
        ProgressDialog progress;
        HashMap<String,String> map;

        public fetchOrder(Context context, String cusid) {
            this.context = context;
            this.cusid = cusid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context);
            progress.setMessage("Please wait ....");
            progress.setTitle("Loading");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String jsonData = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("customer_id", cusid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Call call = client.newCall(request);

            try {
                response = call.execute();

                if (response.isSuccessful()) {
                    jsonData = response.body().string();
                } else {
                    jsonData = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonData;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            super.onPostExecute(jsonData);
            orderList.clear();
            progress.dismiss();
            Log.v("result", "" + jsonData);
            JSONObject jonj = null;
            try {
                if (jsonData != null) {
                    jonj = new JSONObject(jsonData);
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        String data = jonj.getString("message");
                        JSONArray array = new JSONArray(data);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jcat = array.getJSONObject(i);
                            map = new HashMap<String, String>();

                            orderimage = jcat.getString("pic");
                            ordername = jcat.getString("pname");
                            orderqty = jcat.getString("qty");
                            orderprice = jcat.getString("price");

                            map.put("pic", orderimage);
                            map.put("pname", ordername);
                            map.put("qty", orderqty);
                            map.put("price", orderprice);

                            orderList.add(map);

                        }

                        orderAdapter = new OrderAdapter(MyOrderActivity.this, orderList);
                        recyclerView.setAdapter(orderAdapter);

                    } else {
                        Toast.makeText(getApplicationContext(), jonj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
