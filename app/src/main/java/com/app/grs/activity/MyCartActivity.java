package com.app.grs.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.adapter.CartAdapter;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import com.app.grs.helper.OnDataChangeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyCartActivity extends AppCompatActivity implements OnDataChangeListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    public static ArrayList<HashMap<String,String>> cartList=new ArrayList<HashMap<String, String>>();
    RecyclerView.LayoutManager mLayoutManager;
    Button btnCheckout;
    public static ImageView empty;
    public static TextView subtotalview, nodata;
    public static int total;
    private String cartid;
    SimpleDateFormat sdf;
    Date now;
    static ScrollView scrollLayout;
    static LinearLayout btnLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("My Cart");
        setContentView(R.layout.activity_my_cart);

        Constants.pref = getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        final String cusid = Constants.pref.getString("mobileno", "");
        recyclerView = findViewById(R.id.rv_cart);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        btnCheckout = findViewById(R.id.btn_cart_checkout);
        subtotalview = findViewById(R.id.subtotal);
        nodata = findViewById(R.id.no_data);
        empty = findViewById(R.id.no_cart);
        scrollLayout = findViewById(R.id.scroll_layout);
        btnLayout = findViewById(R.id.btn_layout);

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);

        new fetchCart(MyCartActivity.this, cusid, timestamp).execute();

        subtotalview.setText("₹\t" +String.valueOf(grandTotal()));

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new fetchCheckout(MyCartActivity.this, cusid).execute();

            }
        });

        /*if (cartList.size()==0){
            nodata.setVisibility(View.VISIBLE);
            empty.setVisibility(View.VISIBLE);
            scrollLayout.setVisibility(View.GONE);
            btnLayout.setVisibility(View.GONE);
        }*/
    }

    public static int grandTotal(){

        total = 0;
        for(int i = 0 ; i < cartList.size(); i++) {
         //   if(from.equalsIgnoreCase("activity"))
            total += Integer.parseInt(cartList.get(i).get("totalprice"));
           // else total += Integer.parseInt(cartList.get(i).get("totalprice"));
        }
        return total;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        Constants.pref = getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        String cusid = Constants.pref.getString("mobileno", "");
        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);

        new fetchCart(MyCartActivity.this, cusid, timestamp).execute();
        onDataChanged(total);
        GRS.registerReceiver(MyCartActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet disconnect checking
        Constants.pref = getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        String cusid = Constants.pref.getString("mobileno", "");
        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);

        new fetchCart(MyCartActivity.this, cusid, timestamp).execute();
        GRS.unregisterReceiver(MyCartActivity.this);
    }

    @Override
    public void onDataChanged(int total) {
        subtotalview.setText("₹\t" + String.valueOf(total));
    }

    public static void cartList(){
        if (cartList.size()==0){
            nodata.setVisibility(View.VISIBLE);
            empty.setVisibility(View.VISIBLE);
            scrollLayout.setVisibility(View.GONE);
            btnLayout.setVisibility(View.GONE);
        }else {
            nodata.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
            scrollLayout.setVisibility(View.VISIBLE);
            btnLayout.setVisibility(View.VISIBLE);

        }
    }


    private class fetchCart extends AsyncTask<String, Integer, String>{

        public Context context;
        String date;
        public String cusid, proid, proname, proimage, proprice, prodesc, flag, did, dmobile;
        public String url = Constants.BASE_URL + Constants.GET_CART;
        ProgressDialog progress;
        HashMap<String,String> map;
        String data;

        public fetchCart(Context context, String cusid, String date) {
            this.context = context;
            this.cusid = cusid;
            this.date = date;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context);
            progress.setMessage("Please wait ....");
            progress.setTitle("Loading");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String jsonData = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("customer_id", cusid)
                    .add("date", date)
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
            cartList.clear();
            progress.dismiss();
            Log.v("result", "" + jsonData);
            JSONObject jonj = null;
            try {
                if (jsonData != null) {
                    jonj = new JSONObject(jsonData);
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        data = jonj.getString("data");
                        JSONArray array = new JSONArray(data);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jcat = array.getJSONObject(i);
                            map = new HashMap<String, String>();

                            cartid = jcat.getString("cart_id");
                            proid = jcat.getString("id");
                            proimage = jcat.getString("image");
                            proname = jcat.getString("product");
                            proprice = jcat.getString("price");
                            prodesc = jcat.getString("pro_desc");
                            flag = jcat.getString("flag");
                            did = jcat.getString("b_id");
                            dmobile = jcat.getString("b_mobile");

                            map.put("cart_id", cartid);
                            map.put("id", proid);
                            map.put("product", proname);
                            map.put("price", proprice);
                            map.put("image", proimage);
                            map.put("pro_desc", prodesc);
                            map.put("flag", flag);
                            map.put("qty", "1");
                            map.put("totalprice", proprice);
                            map.put("b_id", proprice);
                            map.put("b_mobile", proprice);

                            cartList.add(map);

                        }

                        cartAdapter = new CartAdapter(MyCartActivity.this, cartList);
                        recyclerView.setAdapter(cartAdapter);
                        cartAdapter.setOnDataChangeListener(MyCartActivity.this,MyCartActivity.this);

                        nodata.setVisibility(View.GONE);
                        empty.setVisibility(View.GONE);
                        scrollLayout.setVisibility(View.VISIBLE);
                        btnLayout.setVisibility(View.VISIBLE);

                    } else if (jonj.getString("status").equalsIgnoreCase(
                            "failed")){

                        nodata.setVisibility(View.VISIBLE);
                        empty.setVisibility(View.VISIBLE);
                        scrollLayout.setVisibility(View.GONE);
                        btnLayout.setVisibility(View.GONE);

                        //Toast.makeText(getApplicationContext(), jonj.getString("data"), Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(MyCartActivity.this, HomeActivity.class));
    }

    private class fetchCheckout extends AsyncTask<String, Integer, String> {

        private Context context;
        private String id, cusid, dname, bname, dphone, bphone, demail, bemail, dstate, bstate, dcity, bcity, dadd, badd1, badd2;
        private String url = Constants.BASE_URL + Constants.CHECKOUT_DETAILS;
        ProgressDialog progress;

        public fetchCheckout(Context context, String cusid) {
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
            progress.setCancelable(false);
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
                        JSONObject jcat = array.getJSONObject(0);

                            id = jcat.getString("cus_id");
                            bname = jcat.getString("name");
                            bphone = jcat.getString("mobile_no");
                            bemail = jcat.getString("email");
                            bstate = jcat.getString("state");
                            bcity = jcat.getString("city");
                            badd1 = jcat.getString("post_code");
                            badd2 = jcat.getString("address1");


                            Bundle bundle = new Bundle();
                            bundle.putString("cus_id", id);
                            bundle.putString("name", bname);
                            bundle.putString("mobile_no", bphone);
                            bundle.putString("email", bemail);
                            bundle.putString("state", bstate);
                            bundle.putString("city", bcity);
                            bundle.putString("post_code", badd1);
                            bundle.putString("address1", badd2);
                            bundle.putString("cartid", cartid);
                            bundle.putString("subtotal", String.valueOf(grandTotal()));

                           Intent intent = new Intent(context, CheckoutActivity.class);
                           intent.putExtras(bundle);
                           startActivity(intent);
                           finish();

                    } else {
                        Toast.makeText(getApplicationContext(), jonj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GRS.freeMemory();
    }

}
